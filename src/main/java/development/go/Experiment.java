package development.go;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.measures.DistanceMeasure;
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
    @Parameter(names={"--distanceMeasure", "-m"}, description="foldIndex for reproducibility", required=true)
    private String distanceMeasureName;
    @Parameter(names={"--percentageTrain", "-p"}, description = "percentage of train set to use", required = true)
    private double percentageTrain;
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

    private Instances sampleInstances(Instances instances) {
        Random random = new Random();
        random.setSeed(foldIndex);
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
            File resultsFile = new File(datasetResultDir, String.valueOf(foldIndex));
            if(resultsFile.exists()) {
                throw new IllegalStateException("results exist");
            }
            DistanceMeasure distanceMeasure = DistanceMeasure.produce(distanceMeasureName);
            NearestNeighbour nearestNeighbour = new NearestNeighbour();
            nearestNeighbour.setSeed(foldIndex);
            nearestNeighbour.setDistanceMeasure(distanceMeasure);
            Instances instances = loadDataset(dataset);
            int numFolds = 30;
            instances.stratify(numFolds);
            Random random = new Random();
            random.setSeed(numFolds); // todo is this correct?
            Instances train = instances.trainCV(numFolds, foldIndex, random); // todo should we randomize first? or after? currently does it inside
            Instances sampledTrain = sampleInstances(train);
            nearestNeighbour.buildClassifier(sampledTrain);
            ClassifierResults results = new ClassifierResults();
            Instances test = instances.testCV(numFolds, foldIndex); // todo why the heck is this so small? Damn weka
            for(Instance testInstance : test) {
                results.storeSingleResult(testInstance.classValue(), nearestNeighbour.distributionForInstance(testInstance));
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(resultsFile));
            writer.write(results.toString());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Instances loadDataset(File datasetDir) throws IOException {
//        File datasetDir = new File(datasetDirPath);
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
