package development.go;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.classifiers.ee.constituents.generators.*;
import timeseriesweka.classifiers.ee.iteration.RandomIndexIterator;
import utilities.ClassifierResults;
import utilities.Utilities;
import utilities.instances.Folds;
import weka.core.Instances;

import java.io.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SamplingExperiment {

    private SamplingExperiment() {}

    // todo param validation
    @Parameter(names={"-r"}, description="results dir", converter= FileConverter.class, required=true)
    private File globalResultsDir;
    @Parameter(names={"-f"}, description="number of dataset folds", required=true)
    private int numFolds;
    @Parameter(names={"-fi"}, description="dataset fold index", required=true)
    private int foldIndex;
    @Parameter(names={"-s"}, description="number of train samples", required=true)
    private int numSamples;
    @Parameter(names={"-si"}, description="train sample index", required=true)
    private int sampleIndex;
    @Parameter(names={"-d"}, description="datasets", required=true)
    private List<File> datasets;
    @Parameter(names={"-p"}, description="sample percentages", required=true)
    private List<Double> samplePercentages;
    @Parameter(names={"-k"}, description="kill switch file path", required=true)
    private String killSwitchFilePath;

    public static void main(String[] args) {
        SamplingExperiment samplingExperiment = new SamplingExperiment();
        new JCommander(samplingExperiment).parse(args);
        samplingExperiment.run();
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

    public void run() {
        final boolean[] stop = {false};
        File killSwitchFile = new File(killSwitchFilePath);
        Thread killSwitch = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if(!killSwitchFile.exists()) {
                    stop[0] = true;
                }
                try {
                    Thread.sleep(TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        killSwitch.start();
//        System.out.println("configuration:");
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        System.out.println(gson.toJson(this));
//        System.out.println();
//        System.out.println("benchmarking");
        long benchmark = ClassifierResults.benchmark();
//        System.out.println("running experiments");
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
//            1, // stratified sample, 1 == not, 0 == strat'd
            generators.length,
            datasets.size(),
            samplePercentages.size() // num sample percentages
        };
        final int numCombinations = Utilities.numCombinations(parameterBins);
        globalResultsDir.mkdirs();
        globalResultsDir.setReadable(true, false);
        globalResultsDir.setWritable(true, false);
        globalResultsDir.setExecutable(true, false);
//        for(int sampleIndex = 0; sampleIndex < numSamples && !stop[0]; sampleIndex++) {
//            for(int foldIndex = 0; foldIndex < numFolds && !stop[0]; foldIndex++) {
                RandomIndexIterator randomIndexIterator = new RandomIndexIterator();
                randomIndexIterator.getRange().add(0, numCombinations - 1);
                randomIndexIterator.reset();
                while (randomIndexIterator.hasNext() && !stop[0]) {
                    int combination = randomIndexIterator.next();
                    randomIndexIterator.remove();
                    int[] parameters = Utilities.fromCombination(combination, parameterBins);
                    int parameterIndex = 0;
//                    boolean stratifiedSample = parameters[parameterIndex++] == 0;
                    NnGenerator nnGenerator = generators[parameters[parameterIndex++]];
                    File datasetFile = datasets.get(parameters[parameterIndex++]);
                    double samplePercentage = samplePercentages.get(parameters[parameterIndex++]); //(double) parameters[parameterIndex++] / 100;
                    String datasetName = datasetFile.getName();
                    File experimentResultDir = new File(globalResultsDir, datasetName);
                    experimentResultDir.mkdirs();
                    experimentResultDir.setReadable(true, false);
                    experimentResultDir.setWritable(true, false);
                    experimentResultDir.setExecutable(true, false);
//                    System.out.println("loading " + datasetFile.getName() + " dataset");
                    try {
                        Instances dataset = Utilities.loadDataset(datasetFile);
//                    System.out.println("folding");
                        Folds folds = new Folds.Builder(dataset, numFolds)
                            .stratify(true)
                            .build();
                        Instances trainInstances = folds.getTrain(foldIndex);
                        nnGenerator.setParameterRanges(trainInstances);
                        Instances testInstances = folds.getTest(foldIndex);
                        RandomIndexIterator distanceMeasureParameterIterator = new RandomIndexIterator();
                        distanceMeasureParameterIterator.getRange().add(0, nnGenerator.size() - 1);
                        distanceMeasureParameterIterator.reset();
                        while(distanceMeasureParameterIterator.hasNext()) {
                            int distanceMeasureParameter = distanceMeasureParameterIterator.next();
                            distanceMeasureParameterIterator.remove();
                            NearestNeighbour nearestNeighbour = nnGenerator.get(distanceMeasureParameter);
                            String resultsFileName = "m=" + nearestNeighbour.getDistanceMeasure().toString() + ",n=" + distanceMeasureParameter + ",f=" + foldIndex + ",s=" + sampleIndex + ",p=" + samplePercentage;// + ",d=" + stratifiedSample;
//                        System.out.println(resultsFileName);
                            nearestNeighbour.setSamplePercentage(samplePercentage);
                            nearestNeighbour.setSeed(sampleIndex);
                            File resultsFile = new File(experimentResultDir, resultsFileName + ".gzip");
//                        System.out.println("checking existing results");
                            if(resultsFile.exists()) {
//                            System.out.println("results exist");
                                // results exist so attempt to load them in to ensure they're not corrupt (due to bkill :( )
                                try {
                                    ObjectInputStream in = new ObjectInputStream(
                                        new GZIPInputStream(
                                            new BufferedInputStream(
                                                new FileInputStream(resultsFile)
                                            )
                                        )
                                    );
                                    ClassifierResults results = (ClassifierResults) in.readObject();
                                    continue; // success, so continue
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace(); // fail, need the be re-run
                                }
                            }
                            try {
//                        System.out.println("training");
                                nearestNeighbour.buildClassifier(trainInstances);
//                        System.out.println("testing");
                                ClassifierResults results = nearestNeighbour.predict(testInstances);
                                results.findAllStatsOnce();
                                results.setBenchmark(benchmark);
//                        System.out.println("writing results");
                                ObjectOutputStream out = new ObjectOutputStream(
                                    new GZIPOutputStream(
                                        new BufferedOutputStream(
                                            new FileOutputStream(resultsFile))));
                                out.writeObject(results);
                                out.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
//                        System.out.println();
//                        System.out.println(results);
//                        System.out.println();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
//            }
//        }
        killSwitch.interrupt();
    }


}
