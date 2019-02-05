package development.go;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.classifiers.ee.constituents.generators.*;
import timeseriesweka.classifiers.ee.iteration.RandomIndexIterator;
import timeseriesweka.measures.DistanceMeasure;
import utilities.ClassifierResults;
import utilities.ClassifierStats;
import utilities.Utilities;
import utilities.instances.Folds;
import weka.core.Instances;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    @Parameter(names={"-k"}, description="kill switch file path", required=true)
    private String killSwitchFilePath;

    public static void main(String[] args) {
        SamplingExperiment samplingExperiment = new SamplingExperiment();
        new JCommander(samplingExperiment).parse(args);
        samplingExperiment.run();
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
        long benchmark = -1; // todo change ClassifierResults.benchmark();
        List<ParameterisedSupplier<? extends DistanceMeasure>> parameterisedSuppliers = new ArrayList<>();
        parameterisedSuppliers.add(new DtwParameterisedSupplier());
//        parameterisedSuppliers.add(new DdtwParameterisedSupplier());
//        parameterisedSuppliers.add(new WdtwParameterisedSupplier());
//        parameterisedSuppliers.add(new WddtwParameterisedSupplier());
//        parameterisedSuppliers.add(new LcssParameterisedSupplier());
//        parameterisedSuppliers.add(new MsmParameterisedSupplier());
//        parameterisedSuppliers.add(new TweParameterisedSupplier());
//        parameterisedSuppliers.add(new ErpParameterisedSupplier());
//        parameterisedSuppliers.add(new EuclideanParameterisedSupplier());
        final int[] parameterBins = new int[] {
            parameterisedSuppliers.size(),
            datasets.size()
        };
        final int numCombinations = Utilities.numCombinations(parameterBins);
        globalResultsDir.mkdirs();
        globalResultsDir.setReadable(true, false);
        globalResultsDir.setWritable(true, false);
        globalResultsDir.setExecutable(true, false);
        // todo fold and resample rand iter and list param for both
        RandomIndexIterator combinationIndexIterator = new RandomIndexIterator();
        combinationIndexIterator.getRange().add(0, numCombinations - 1);
        combinationIndexIterator.reset();
        while (combinationIndexIterator.hasNext() && !stop[0]) {
            int combination = combinationIndexIterator.next();
            combinationIndexIterator.remove();
            int[] parameters = Utilities.fromCombination(combination, parameterBins);
            int parameterIndex = 0;
            ParameterisedSupplier<? extends DistanceMeasure> parameterisedSupplier = parameterisedSuppliers.get(parameters[parameterIndex++]);
            File datasetFile = datasets.get(parameters[parameterIndex++]);
            String datasetName = datasetFile.getName();
            File experimentResultDir = new File(globalResultsDir, datasetName);
            experimentResultDir.mkdirs();
            experimentResultDir.setReadable(true, false);
            experimentResultDir.setWritable(true, false);
            experimentResultDir.setExecutable(true, false);
            try {
                Instances dataset = Utilities.loadDataset(datasetFile);
                Folds folds = new Folds.Builder(dataset, numFolds)
                    .stratify(true)
                    .build();
                Instances trainInstances = folds.getTrain(foldIndex);
                parameterisedSupplier.setParameterRanges(trainInstances);
                Instances testInstances = folds.getTest(foldIndex);
                RandomIndexIterator distanceMeasureParameterIterator = new RandomIndexIterator();
                distanceMeasureParameterIterator.getRange().add(0, parameterisedSupplier.size() - 1);
                distanceMeasureParameterIterator.reset();
                while(distanceMeasureParameterIterator.hasNext()) {
                    int distanceMeasureParameter = distanceMeasureParameterIterator.next();
                    distanceMeasureParameterIterator.remove();
                    DistanceMeasure distanceMeasure = parameterisedSupplier.get(distanceMeasureParameter);
                    NearestNeighbour nearestNeighbour = new NearestNeighbour();
                    nearestNeighbour.setDistanceMeasure(distanceMeasure);
                    nearestNeighbour.setTrainInstances(trainInstances);
                    nearestNeighbour.setTestInstances(testInstances);
                    nearestNeighbour.train();
                    while (nearestNeighbour.remainingTestTicks()) {
                        nearestNeighbour.testTick();
                        double[][] predictions = nearestNeighbour.predict();
                        for(double[] prediction : predictions) {
                            for(int i = 0; i < prediction.length - 1; i++) {
                                System.out.print(prediction[i]);
                                System.out.print(", ");
                            }
                            System.out.println(prediction[prediction.length - 1]);
                        }
                        System.out.println("-----");
                    }

//                    String resultsFileName = "m=" + nearestNeighbour.getDistanceMeasure().toString() + ",n=" + distanceMeasureParameter + ",f=" + foldIndex + ",s=" + sampleIndex + ",p=" + samplePercentage;// + ",d=" + stratifiedSample;
////                        System.out.println(resultsFileName);
//                    nearestNeighbour.setSeed(sampleIndex);
//                    File resultsFile = new File(experimentResultDir, resultsFileName + ".gzip");
////                        System.out.println("checking existing results");
//                    ClassifierResults results = null;
//                    boolean run = false;
//                    try {
//                        if(resultsFile.exists()) {
////                            System.out.println("results exist");
//                            // results exist so attempt to load them in to ensure they're not corrupt (due to bkill :( )
//                            try {
//                                ObjectInputStream in = new ObjectInputStream(
//                                    new GZIPInputStream(
//                                        new BufferedInputStream(
//                                            new FileInputStream(resultsFile)
//                                        )
//                                    )
//                                );
//                                Object object = in.readObject();
//                                if(object instanceof ClassifierResults) {
//                                    results = (ClassifierResults) object;
//                                } else {
//                                    continue; // success, so continue
//                                }
//                            } catch (ClassNotFoundException e) {
//                                e.printStackTrace(); // fail, need the be re-run
//                                run = true;
//                            }
//                        } else {
//                            run = true;
//                        }
//                        if(run) {
////                        System.out.println("training");
//                            nearestNeighbour.buildClassifier(trainInstances);
////                        System.out.println("testing");
//                            results = nearestNeighbour.predict(testInstances);
//                            results.setBenchmark(benchmark);
//                        }
//                        results.findAllStatsOnce();
////                        System.out.println("writing results");
//                        ObjectOutputStream out = new ObjectOutputStream(
//                            new GZIPOutputStream(
//                                new BufferedOutputStream(
//                                    new FileOutputStream(resultsFile))));
//                        out.writeObject(new ClassifierStats(results));
//                        out.close();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        killSwitch.interrupt();
    }


}
