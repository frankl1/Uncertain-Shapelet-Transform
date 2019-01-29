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
import java.nio.ByteBuffer;
import java.util.TreeMap;
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
    private double[] distances;
    private double[] times;
    private DistanceMeasure distanceMeasure;

    public static void main(String[] args) {
        DistanceCalculator distanceCalculator = new DistanceCalculator();
        new JCommander(distanceCalculator).parse(args);
        distanceCalculator.run();
    }

    private DistanceMeasure getDistanceMeasure(Instances instances) {
        if(combination < 0 || combination > 802) {
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

    public void run() {
        try {
            Instances instances = Utilities.loadDataset(datasetDir);
            distanceMeasure = getDistanceMeasure(instances);
            resultsDir.mkdirs();
            resultsDir.setReadable(true, false);
            resultsDir.setWritable(true, false);
            resultsDir.setExecutable(true, false);
            File datasetResultsDir = new File(resultsDir, datasetDir.getName());
            datasetResultsDir.mkdirs();
            datasetResultsDir.setReadable(true, false);
            datasetResultsDir.setWritable(true, false);
            datasetResultsDir.setExecutable(true, false);
            File distanceMeasureResultsDir = new File(datasetResultsDir, distanceMeasure.toString());
            distanceMeasureResultsDir.mkdirs();
            distanceMeasureResultsDir.setReadable(true, false);
            distanceMeasureResultsDir.setWritable(true, false);
            distanceMeasureResultsDir.setExecutable(true, false);
            File distanceMeasureResultsFile = new File(distanceMeasureResultsDir, distanceMeasure.getParameters());
            if(distanceMeasureResultsFile.exists()) {
//                System.out.print("results exist");
            } else {
                FileOutputStream fos = new FileOutputStream(distanceMeasureResultsFile);
                GZIPOutputStream zos = new GZIPOutputStream(fos);
                ObjectOutputStream oos = new ObjectOutputStream(zos);
                oos.writeLong(benchmark);
                for(int i = 0; i < instances.size(); i++) {
                    Instance instanceA = instances.get(i);
                    for(int j = 0; j < i; j++) {
                        Instance instanceB = instances.get(j);
                        long time = System.nanoTime();
                        double distance = distanceMeasure.distance(instanceA, instanceB);
                        time = System.nanoTime() - time;
                        oos.writeDouble(distance);
                        oos.writeLong(time);
                    }
                }
                oos.close();
                zos.close();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println();
    }
}
