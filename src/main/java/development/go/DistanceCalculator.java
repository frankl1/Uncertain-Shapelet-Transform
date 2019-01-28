package development.go;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.classifiers.ee.constituents.generators.*;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.DistanceMeasurement;
import utilities.Utilities;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class DistanceCalculator {

    @Parameter(names={"-d"}, description="dataset dir", converter= FileConverter.class, required=true)
    private File datasetDir;
    @Parameter(names={"-r"}, description="results dir", converter= FileConverter.class, required=true)
    private File resultsDir;
    @Parameter(names={"-c"}, description="distance measure and parameter", required=true)
    private int combination;
    @Parameter(names={"-b"}, description="benchmark", required=true)
    private long benchmark;
    private HashMap<Integer, HashMap<Integer, DistanceMeasurement>> distanceMap = new HashMap<>();
    private DistanceMeasure distanceMeasure;

    public static void main(String[] args) {
        DistanceCalculator distanceCalculator = new DistanceCalculator();
        new JCommander(distanceCalculator).parse(args);
        try {
            distanceCalculator.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private DistanceMeasure getDistanceMeasure(Instances instances) {
        if(combination < 0 || combination >= 803) {
            throw new IllegalArgumentException("out of range: " + combination);
        }
        NnGenerator[] generators = new NnGenerator[]{
                new DtwGenerator(),
                new DdtwGenerator(),
                new WdtwGenerator(),
                new WddtwGenerator(),
                new LcssGenerator(),
                new MsmGenerator(),
                new TweGenerator(),
                new ErpGenerator(),
                new EuclideanGenerator()
        };
        for(NnGenerator generator : generators) {
            generator.setParameterRanges(instances);
        }
        int index = 0;
        while(combination >= generators[index].size()) {
            combination -= generators[index].size();
            index++;
        }
        NnGenerator generator = generators[index];
        NearestNeighbour nearestNeighbour = generator.get(combination);
        return nearestNeighbour.getDistanceMeasure();
    }

    private boolean distanceHasBeenRecordedOrdered(Instance instanceA, Instance instanceB) {
        Map<Integer, DistanceMeasurement> map = distanceMap.get((int) instanceA.weight());
        if(map == null) {
            return false;
        }
        if(map.containsKey((int) instanceB.weight())) {
            return true;
        }
        return false;
    }

    private boolean distanceHasBeenRecorded(Instance instanceA, Instance instanceB) {
        return distanceHasBeenRecordedOrdered(instanceA, instanceB) || distanceHasBeenRecordedOrdered(instanceB, instanceA);
    }

    private void recordDistance(int i, int j, DistanceMeasurement distance) {
        Map<Integer, DistanceMeasurement> map = distanceMap.computeIfAbsent(i, key -> new HashMap<>());
        map.put(j, distance);
    }

    private void recordDistance(Instance instanceA, Instance instanceB) {
        if(!distanceHasBeenRecorded(instanceA, instanceB)) {
//            System.out.println("Recording distance between " + (int) instanceA.weight() + "th and " + (int) instanceB.weight() + "th");
            long startTime = System.nanoTime();
            double distance = distanceMeasure.distance(instanceA, instanceB);
            long stopTime = System.nanoTime();
            long time = stopTime - startTime;
            recordDistance((int) instanceA.weight(), (int) instanceB.weight(), new DistanceMeasurement(distance, time));
        }
    }

    public void run() throws IOException {
        Instances instances = Utilities.loadDataset(datasetDir);
        distanceMeasure = getDistanceMeasure(instances);
        File distanceMeasureResultsDir = new File(resultsDir, distanceMeasure.toString());
        distanceMeasureResultsDir.mkdirs();
        File distanceMeasureResultsFile = new File(distanceMeasureResultsDir, distanceMeasure.getParameters());
        if(distanceMeasureResultsFile.exists()) {
            System.out.println("results exist");
        } else {
            for(int i = 0; i < instances.size(); i++) {
                instances.get(i).setWeight(i);
            }
            for(int i = 1; i < instances.size(); i++) {
                Instance instanceA = instances.get(i);
                for(int j = 0; j < i; j++) {
                    Instance instanceB = instances.get(j);
                    recordDistance(instanceA, instanceB);
                }
            }
            writeCalculatedDistance(distanceMeasureResultsFile);
        }
    }

    private void writeCalculatedDistance(File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        GZIPOutputStream zos = new GZIPOutputStream(fos);
        ObjectOutputStream ous = new ObjectOutputStream(zos);
        ous.writeObject(distanceMap);
        ous.writeDouble(benchmark);
        ous.close();
        zos.close();
        fos.close();
    }
}
