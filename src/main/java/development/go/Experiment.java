package development.go;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.classifiers.ee.abcdef.generators.*;
import timeseriesweka.classifiers.ee.iteration.ElementIterator;
import timeseriesweka.classifiers.ee.iteration.RandomIndexIterator;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;
import timeseriesweka.measures.euclidean.Euclidean;
import timeseriesweka.measures.wdtw.Wdtw;
import utilities.ClassifierResults;
import utilities.Utilities;
import utilities.instances.Folds;
import weka.core.Instances;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        System.out.println("configuration:");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(this));
        System.out.println();
        System.out.println("benchmarking...");
        long benchmark = ClassifierResults.benchmark();
        System.out.println("running experiments...");
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
            1, // stratified sample // todo change to 2 to do non-strat'd as well
            generators.length,
            100, // num distance measure params
            datasets.size(),
            100 // num sample percentages
        };
        final int maxCombination = Utilities.toCombination(parameterBins, parameterBins);
        RandomIndexIterator randomIndexIterator = new RandomIndexIterator();
        randomIndexIterator.getRange().add(0, maxCombination - 1);
        randomIndexIterator.reset();
        while (randomIndexIterator.hasNext()) {
            int combination = randomIndexIterator.next();
            randomIndexIterator.remove();
            int[] parameters = Utilities.fromCombination(combination, parameterBins);
            boolean stratifiedSample = parameters[0] == 0;
            NnGenerator nnGenerator = generators[parameters[1]];
            int distanceMeasureParameter = parameters[2];
            File datasetFile = datasets.get(parameters[3]);
            double samplePercentage = (double) parameters[4] / 100;

            String datasetName = datasetFile.getName();
            File experimentResultDir = new File(globalResultsDir, datasetName);
            experimentResultDir.mkdirs();
            Instances dataset = Utilities.loadDataset(datasetFile);
            Folds folds = new Folds.Builder(dataset, numFolds)
                .stratify(true)
                .build();
            for(int sampleIndex = 0; sampleIndex < numSamples; sampleIndex++) {
                for(int foldIndex = 0; foldIndex < numFolds; foldIndex++) {
                    Instances trainInstances = folds.getTrain(foldIndex);
                    nnGenerator.setParameterRanges(trainInstances);
                    NearestNeighbour nearestNeighbour = nnGenerator.get(distanceMeasureParameter);
                    nearestNeighbour.setSamplePercentage(samplePercentage);
                    nearestNeighbour.setSeed(sampleIndex);
                    String resultsFileName = "m=" + nearestNeighbour.getDistanceMeasure().toString() + ",p=" + distanceMeasureParameter + ",f=" + foldIndex + ",s=" + sampleIndex + ",p=" + samplePercentage + ",d=" + stratifiedSample + ".csv";
                    File resultsFile = new File(resultsFileName);
                    if(resultsFile.exists()) {
                        continue;
                    }
                    nearestNeighbour.buildClassifier(trainInstances);
                    Instances testInstances = folds.getTest(foldIndex);
                    ClassifierResults results = nearestNeighbour.predict(testInstances);
                    results.findAllStatsOnce();
                    results.setBenchmark(benchmark);
                    BufferedWriter writer = new BufferedWriter(new FileWriter(resultsFile));
                    writer.write(results.toString());
                    System.out.println(results);
                    System.out.println();
                    writer.close();
                }
            }
        }
    }


}
