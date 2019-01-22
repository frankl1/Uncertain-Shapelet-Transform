package development.go;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.classifiers.ee.abcdef.generators.*;
import timeseriesweka.classifiers.ee.iteration.RandomIndexIterator;
import utilities.ClassifierResults;
import utilities.Utilities;
import utilities.instances.Folds;
import weka.core.Instances;

import java.io.*;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Experiment {

    private Experiment() {}

    // todo param validation
    @Parameter(names={"-r"}, description="results dir", converter= FileConverter.class, required=true)
    private File globalResultsDir;
    @Parameter(names={"-n"}, description="number of dataset folds", required=true)
    private int numFolds;
    @Parameter(names={"-s"}, description="number of train samples", required=true)
    private int numSamples;
    @Parameter(names={"-d"}, description="datasets", required=true)
    private List<File> datasets;
    @Parameter(names={"-p"}, description="sample percentages", required=true)
    private List<Double> samplePercentages;

    public static void main(String[] args) throws Exception {
        Experiment experiment = new Experiment();
        new JCommander(experiment).parse(args);
        experiment.run();
    }

    private static NnGenerator generatorFromString(String name) {
        name = name.toLowerCase();
        switch(name) {
            case "msm":
                return new MsmGenerator();
            case "dtw":
                return new DtwGenerator();
            case "ddtw":
                return new DdtwGenerator();
            case "erp":
                return new ErpGenerator();
            case "twe":
                return new TweGenerator();
            case "wdtw":
                return new WdtwGenerator();
            case "wddtw":
                return new WddtwGenerator();
            case "lcss":
                return new LcssGenerator();
            case "ed":
                return new EuclideanGenerator();
            default:
                throw new IllegalArgumentException();
        }
    }

    public void run() throws Exception {
        File killSwitchFile = new File("deleteToKill");
        Thread killSwitch = new Thread(new Runnable() {

            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    if(!killSwitchFile.exists()) {
                        System.out.println("killed");
                        System.exit(1);
                    }
                    try {
                        Thread.sleep(TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        killSwitch.start();
        System.out.println("configuration:");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(this));
        System.out.println();
        System.out.println("benchmarking");
        long benchmark = ClassifierResults.benchmark();
        System.out.println("running experiments");
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
        final int[] parameterBins = new int[] {
            2, // stratified sample, 1 == not, 0 == strat'd
            generators.length,
            datasets.size(),
            samplePercentages.size() // num sample percentages
        };
        final int numCombinations = Utilities.numCombinations(parameterBins);
        for(int sampleIndex = 0; sampleIndex < numSamples; sampleIndex++) {
            for(int foldIndex = 0; foldIndex < numFolds; foldIndex++) {
                RandomIndexIterator randomIndexIterator = new RandomIndexIterator();
                randomIndexIterator.getRange().add(0, numCombinations - 1);
                randomIndexIterator.reset();
                while (randomIndexIterator.hasNext()) {
                    int combination = randomIndexIterator.next();
                    randomIndexIterator.remove();
                    int[] parameters = Utilities.fromCombination(combination, parameterBins);
                    int parameterIndex = 0;
                    boolean stratifiedSample = parameters[parameterIndex++] == 0;
                    NnGenerator nnGenerator = generators[parameters[parameterIndex++]];
                    File datasetFile = datasets.get(parameters[parameterIndex++]);
                    double samplePercentage = samplePercentages.get(parameters[parameterIndex++]); //(double) parameters[parameterIndex++] / 100;
                    String datasetName = datasetFile.getName();
                    File experimentResultDir = new File(globalResultsDir, datasetName);
                    experimentResultDir.mkdirs();
                    System.out.println("loading " + datasetFile.getName() + " dataset");
                    Instances dataset = Utilities.loadDataset(datasetFile);
                    System.out.println("folding");
                    Folds folds = new Folds.Builder(dataset, numFolds)
                        .stratify(true)
                        .build();
                    Instances trainInstances = folds.getTrain(foldIndex);
                    nnGenerator.setParameterRanges(trainInstances);
                    Instances testInstances = folds.getTest(foldIndex);
                    for(int distanceMeasureParameter = 0; distanceMeasureParameter < nnGenerator.size(); distanceMeasureParameter++) { // todo make this random
                        trainInstances = new Instances(trainInstances); // deep copy just in case anything got modified from last iteration
                        testInstances = new Instances(testInstances);
                        NearestNeighbour nearestNeighbour = nnGenerator.get(distanceMeasureParameter);
                        String resultsFileName = "m=" + nearestNeighbour.getDistanceMeasure().toString() + ",n=" + distanceMeasureParameter + ",f=" + foldIndex + ",s=" + sampleIndex + ",p=" + samplePercentage + ",d=" + stratifiedSample + ".csv";
                        System.out.println(resultsFileName);
                        nearestNeighbour.setSamplePercentage(samplePercentage);
                        nearestNeighbour.setSeed(sampleIndex);
                        File resultsFile = new File(experimentResultDir, resultsFileName);
                        System.out.println("checking existing results");
                        if(resultsFile.exists()) {
                            System.out.println("results exist, skipping");
                            continue;
                        }
                        System.out.println("training");
                        nearestNeighbour.buildClassifier(trainInstances);
                        System.out.println("testing");
                        ClassifierResults results = nearestNeighbour.predict(testInstances);
                        results.findAllStatsOnce();
                        results.setBenchmark(benchmark);
                        System.out.println("writing results");
                        BufferedWriter writer = new BufferedWriter(new FileWriter(resultsFile));
                        writer.write(results.toString());
                        System.out.println();
                        System.out.println(results);
                        System.out.println();
                        writer.close();
                    }
                }
            }
        }
        killSwitch.interrupt();
    }


}
