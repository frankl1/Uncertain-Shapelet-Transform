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
import utilities.instances.Folds;
import utilities.instances.TrainTestSplit;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.util.Random;

public class Experiment implements Runnable {

    private Experiment() {}

    // todo param validation
    @Parameter(names={"--dataset", "-d"}, description="path to dataset arff", converter= FileConverter.class, required=true)
    private File dataset;
    @Parameter(names={"--foldIndex", "-f"}, description="foldIndex for reproducibility", required=true)
    private int foldIndex;
    @Parameter(names={"--combination", "-c"}, description="combination of parameters", required=true)
    private int combination;
    @Parameter(names={"--results", "-r"}, description="path to dataset arff", converter= FileConverter.class, required=true)
    private File resultsDir;

    public static void main(String[] args) {
        Experiment experiment = new Experiment();
        new JCommander(experiment).parse(args);
        experiment.run();
    }

    @Override
    public String toString() {
        return dataset + "," + foldIndex;
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
        System.out.println("Configuration:");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(this));
        try {
            String datasetName = dataset.getName();
            File datasetResultDir = new File(resultsDir, datasetName);
            datasetResultDir.mkdirs();
            File resultsFile = new File(datasetResultDir, foldIndex + "_" + combination + ".csv");
            if(resultsFile.exists()) {
                throw new IllegalStateException("results exist");
            }
            DistanceMeasure distanceMeasure = new Dtw();
            NearestNeighbour nearestNeighbour = new NearestNeighbour();

            nearestNeighbour.setSeed(foldIndex);
            nearestNeighbour.setDistanceMeasure(distanceMeasure);
            Instances instances = loadDataset(dataset);
            int numFolds = 10;
            Folds folds = new Folds.Builder(instances, numFolds)
                .stratify(true)
                .build();
            Instances train = folds.getTrain(foldIndex);
            Instances sampledTrain = sampleInstances(train, foldIndex, );
            nearestNeighbour.buildClassifier(sampledTrain);
            Instances test = folds.getTest(foldIndex);
            ClassifierResults results = nearestNeighbour.predict(test);
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
