package development.go;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;
import utilities.ClassifierResults;
import utilities.Utilities;
import utilities.instances.Folds;
import weka.core.Instances;

import java.io.*;
import java.util.Random;

public class Experiment implements Runnable {

    private Experiment() {}

    // todo param validation
    @Parameter(names={"-d"}, description="path to dataset arff", converter= FileConverter.class, required=true)
    private File dataset;
    @Parameter(names={"-c"}, description="combination of parameters", required=true)
    private int combination;
    @Parameter(names={"-r"}, description="path to dataset dir containing arffs", converter= FileConverter.class, required=true)
    private File globalResultsDir;
    @Parameter(names={"-f"}, description="number of dataset folds", required=true)
    private int numFolds;
    @Parameter(names={"-p"}, description="number of percentage intervals of train instances to sample", required=true)
    private int numPercentageIntervals;
    @Parameter(names={"-s"}, description="number of resamples", required=true)
    private int numResamples;

    public static void main(String[] args) {
        Experiment experiment = new Experiment();
        new JCommander(experiment).parse(args);
        experiment.run();
    }

    private static Instances sampleInstances(Instances instances, long seed, double percentageTrain) {
        Random random = new Random();
        random.setSeed(seed);
        Instances sampled = new Instances(instances, 0);
        int numToSample = (int) Math.floor(percentageTrain * instances.numInstances());
        for(int i = 0; i < numToSample; i++) {
            sampled.add(instances.remove(random.nextInt(instances.numInstances())));
        }
        return sampled;
    }

    @Override
    public void run() {

        for(int i = 0; i < 88; i++) {
            int[] parameters = Utilities.fromCombination(i, 5, 5, 5);
            for(int j : parameters) {
                System.out.print(j);
                System.out.print(", ");
            }
            System.out.println();
        }
        System.exit(0);

        System.out.println("Configuration:");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(this));
        System.out.println();
        try {
            // setup results file
            NearestNeighbour nearestNeighbour = new NearestNeighbour();
            String datasetName = dataset.getName();
            File experimentResultDir = new File(globalResultsDir, datasetName + "/" + nearestNeighbour.toString() + "/Predictions");
            experimentResultDir.mkdirs();
            File resultsFile = new File(experimentResultDir, combination + ".csv");
            if(resultsFile.exists()) {
                throw new IllegalStateException("results exist");
            }
            // setup experiment parameters
            int[] parameters = Utilities.fromCombination(combination, numPercentageIntervals, numResamples, numFolds);
            int datasetFoldIndex = parameters[2];
            int resampleIndex = parameters[1];
            int percentageIntervalIndex = parameters[0];
//            DistanceMeasure[] distanceMeasures = new DistanceMeasure[] {
//                    new Dtw(),
//                    new Ddtw(),
//                    new Msm(),
//                    new Wdtw(),
//                    new Wddtw(),
//                    new Erp(),
//                    new Lcss(),
//                    new Twe(),
//                    new Euclidean()
//            };
            // setup experiment
            DistanceMeasure distanceMeasure = new Dtw();
            nearestNeighbour.setSeed(resampleIndex);
            nearestNeighbour.setDistanceMeasure(distanceMeasure);
            Instances instances = loadDataset(dataset);
            Folds folds = new Folds.Builder(instances, numFolds)
                .stratify(true)
                .build();
            Instances train = folds.getTrain(datasetFoldIndex);
            double percentageToSample = (double) percentageIntervalIndex / (numPercentageIntervals - 1);
            nearestNeighbour.setSamplePercentage(percentageToSample);
            nearestNeighbour.buildClassifier(train);
            Instances test = folds.getTest(datasetFoldIndex);
            ClassifierResults results = nearestNeighbour.predict(test);
            results.findAllStatsOnce();
            System.out.println(results);
            BufferedWriter writer = new BufferedWriter(new FileWriter(resultsFile));
            writer.write(results.toString());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Instances loadDataset(File datasetDir) throws IOException {
        File datasetFile = new File(datasetDir, datasetDir.getName() + ".arff");
        if(datasetFile.exists()) {
            return instancesFromFile(datasetFile);
        }
        datasetFile = new File(datasetDir, datasetDir.getName() + "_TRAIN.arff");
        File testDatasetFile = new File(datasetDir, datasetDir.getName() + "_TEST.arff");
        if(datasetFile.exists() && testDatasetFile.exists()) {
            Instances instances = instancesFromFile(datasetFile);
            instances.addAll(instancesFromFile(testDatasetFile));
            return instances;
        }
        throw new IllegalArgumentException();
    }

    private static Instances instancesFromFile(File file) throws IOException {
        Instances instances = new Instances(new BufferedReader(new FileReader(file)));
        instances.setClassIndex(instances.numAttributes() - 1);
        return instances;
    }

    private static Instances instancesFromFile(String path) throws IOException {
        return instancesFromFile(new File(path));
    }
}
