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

public class Experiment implements Runnable {

    private Experiment() {}

    // todo param validation
    @Parameter(names={"-d"}, description="path to dataset arff", converter= FileConverter.class, required=true)
    private File dataset;
    @Parameter(names={"-r"}, description="path to dataset dir containing arffs", converter= FileConverter.class, required=true)
    private File globalResultsDir;
    @Parameter(names={"-f"}, description="fold index", required=true)
    private int foldIndex;
    @Parameter(names={"-n"}, description="number of folds", required=true)
    private int numFolds;
    @Parameter(names={"-p"}, description="percentage of train to use", required=true)
    private double samplePercentage;
    @Parameter(names={"-s"}, description="sample index", required=true)
    private int sampleIndex;

    public static void main(String[] args) {
        Experiment experiment = new Experiment();
        new JCommander(experiment).parse(args);
        experiment.run();
    }

    @Override
    public void run() {

//        for(int i = 0; i < 32; i++) {
//            int[] parameters = Utilities.fromCombination(i, 3, 4, 5);
//            for(int j : parameters) {
//                System.out.print(j);
//                System.out.print(", ");
//            }
//            System.out.println();
//        }
//        System.out.println();
//        System.out.println(Utilities.toCombination(1, 3, 2, 4, 2, 5));
//        System.exit(0);

        System.out.println("Configuration:");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(this));
        System.out.println();
        try {
            // setup results file
            NearestNeighbour nearestNeighbour = new NearestNeighbour();
            String datasetName = dataset.getName();
            File experimentResultDir = new File(globalResultsDir, datasetName);
            experimentResultDir.mkdirs();
            File resultsFile = new File(experimentResultDir, "f" + foldIndex + "_s" + sampleIndex + "_p" + samplePercentage + ".csv");
            if(resultsFile.exists()) {
                throw new IllegalStateException("results exist");
            }
            // setup experiment
            DistanceMeasure distanceMeasure = new Dtw();
            nearestNeighbour.setSeed(sampleIndex);
            nearestNeighbour.setDistanceMeasure(distanceMeasure);
            nearestNeighbour.setStratifiedSample(true);
            Instances instances = Utilities.loadDataset(dataset);
            Folds folds = new Folds.Builder(instances, numFolds)
                .stratify(true)
                .build();
            Instances train = folds.getTrain(foldIndex);
            nearestNeighbour.setSamplePercentage(samplePercentage);
            nearestNeighbour.buildClassifier(train);
            Instances test = folds.getTest(foldIndex);
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

}
