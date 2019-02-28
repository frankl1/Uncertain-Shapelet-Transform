package timeseriesweka.classifiers;

import net.sourceforge.sizeof.SizeOf;
import timeseriesweka.classifiers.ensembles.elastic_ensemble.Dtw1Nn2;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;
import utilities.*;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static utilities.Utilities.argMax;

public class NearestNeighbour extends AbstractClassifier implements Serializable, Reproducible, SaveParameterInfo, CompressedCheckpointClassifier, ContractClassifier {

    // todo implement checkpointing and contracting

    public static final NeighbourWeighter WEIGHT_BY_DISTANCE = distance -> 1 / (1 + distance);
    public static final NeighbourWeighter WEIGHT_UNIFORM = distance -> 1;
    private static final String CHECKPOINT_FILE_NAME = "checkpoint.ser.gzip";
    private String checkpointFilePath;
    private Instances originalTrainInstances = null;
    private double sampleSizePercentage = 1;
    private Instances sampledTrainInstances;
    private Random random = new Random();
    private double[] classSamplingProbabilities;
    private double[] classDistribution;
    private Instances[] instancesByClass; // class value by instances of that class
    private List<NearestNeighbourFinder> trainNearestNeighbourFinders;
    private double kPercentage = 0;
    private long trainDuration = 0;
    private long testDuration = 0;
    private long predictDuration = 0;
    private boolean cvTrain = false;
    private Instances originalSampledTrainInstances;
    private boolean useRandomTieBreak = false;
    private Instances originalTestInstances;
    private List<NearestNeighbourFinder> testNearestNeighbourFinders;
    private DistanceMeasure distanceMeasure;
    private boolean useEarlyAbandon = false;
    private NeighbourWeighter neighbourWeighter = WEIGHT_BY_DISTANCE;
    private long minCheckpointInterval = TimeUnit.NANOSECONDS.convert(10, TimeUnit.MINUTES); // todo put this in the checkpoint interface
    private long lastCheckpointTimeStamp = System.nanoTime() - minCheckpointInterval;
    private long trainDurationLimit = -1;
    private boolean hasLoadedFromCheckpoint = false;

    public boolean usesAbsoluteProbability() {
        return useAbsoluteProbability;
    }

    public void setUseAbsoluteProbability(final boolean useAbsoluteProbability) {
        this.useAbsoluteProbability = useAbsoluteProbability;
    }

    private boolean useAbsoluteProbability = false;

    public NearestNeighbour() {
        setDistanceMeasure(new Dtw());
    }


    private static ClassifierResults trainAndTest(Classifier classifier, Instances trainInstances, Instances testInstances) throws Exception {
        classifier.buildClassifier(trainInstances);
        return test(classifier, testInstances);
    }

    private static ClassifierResults trainAndTest(Classifier classifier, Instances trainInstances, Instances testInstances, ClassifierResults results) throws Exception {
        classifier.buildClassifier(trainInstances);
        return test(classifier, testInstances, results);
    }

    private static ClassifierResults test(Classifier classifier, Instances testInstances) throws Exception {
        ClassifierResults results = test(classifier, testInstances, new ClassifierResults());
        results.setNumInstances(testInstances.numInstances());
        results.setNumClasses(testInstances.numClasses());
        results.findAllStatsOnce();
        return results;
    }

    private static ClassifierResults test(Classifier classifier, Instances testInstances, ClassifierResults results) throws Exception {
        for(Instance testInstance : testInstances) {
            double classValue = testInstance.classValue();
            double[] predictions = classifier.distributionForInstance(testInstance);
            int[] maxIndices = argMax(predictions);
            double prediction;
            if(maxIndices.length > 1) {
                boolean b = true;
            }
            prediction = maxIndices[0];
            if(prediction != classValue) {
                System.out.println(testInstance.weight());
            }
            results.storeSingleResult(classValue, predictions);
        }
        return results;
    }

    public static void main(String[] args) throws Exception {
        ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        executorService.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        String datasetsPath = "/scratch/Datasets/TSCProblems2015/";
        System.out.println(datasetsPath);
        List<String> datasetNames = new ArrayList<>();
        datasetNames.add("WormsTwoClass");
//        for(File file : new File(datasetsPath).listFiles(new FileFilter() {
//            @Override
//            public boolean accept(final File file) {
//                return file.isDirectory();
//            }
//        })) {
//            datasetNames.add(file.getName());
//        }
//        BufferedReader reader = new BufferedReader(new FileReader("/scratch/datasetList.txt"));
//        String line;
//        while ((line = reader.readLine()) != null) {
//            datasetNames.add(line);
//        }
//        reader.close();
        int foldIndex = 0;
        String type = "DTW";
        for(String datasetName : datasetNames) {
            String datasetPath = datasetsPath + datasetName;
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    try {
//                        System.out.println("loading " + datasetName);
                        Instances[] split = Utilities.loadSplitInstances(new File(datasetPath));
                        Instances trainInstances = split[0];
                        Instances testInstances = split[1];
                        int weight = 0;
                        for(Instance instance : trainInstances) {
                            instance.setWeight(weight++);
                        }
                        for(Instance instance : testInstances) {
                            instance.setWeight(weight++);
                        }
//                        DTW1NN orig = new DTW1NN();
                        Dtw1Nn2 orig2 = new Dtw1Nn2();
                        NearestNeighbour nn = new NearestNeighbour();
//                        nn.setSavePath("/scratch/checkpoints/" + datasetName);
                        nn.setCvTrain(true);
                        Dtw dtw = new Dtw();
                        nn.setDistanceMeasure(dtw);
                        String previousTestResult = "/run/user/33190/gvfs/smb-share:server=cmptscsvr.cmp.uea.ac.uk,share=ueatsc/Results_7_2_19/JayMovingInProgress/EEConstituentResults/" + type + "_Rn_1NN/Predictions/" + datasetName + "/testFold" + foldIndex + ".csv";
                        String previousTrainResult = "/run/user/33190/gvfs/smb-share:server=cmptscsvr.cmp.uea.ac.uk,share=ueatsc/Results_7_2_19/JayMovingInProgress/EEConstituentResults/" + type + "_Rn_1NN/Predictions/" + datasetName + "/trainFold" + foldIndex + ".csv";
                        BufferedReader testReader = new BufferedReader(new FileReader(previousTestResult));
                        testReader.readLine();
                        int testParam = Integer.parseInt(testReader.readLine());
//                        double testAcc = Double.parseDouble(testReader.readLine());
                        testReader.close();
                        BufferedReader trainReader = new BufferedReader(new FileReader(previousTrainResult));
                        trainReader.readLine();
                        int trainParam = Integer.parseInt(trainReader.readLine());
//                        double trainAcc = Double.parseDouble(trainReader.readLine());
                        trainReader.close();
                        dtw.setWarpingWindow((double) trainParam / 100);
//                        orig.setParamsFromParamId(trainInstances, testParam);
                        orig2.setParamsFromParamId(trainInstances, testParam);
//                        ClassifierResults origTestResults = trainAndTest(orig, trainInstances, testInstances);
//                        ClassifierResults orig2TestResults = trainAndTest(orig2, trainInstances, testInstances);
//                        nn.buildClassifier(trainInstances);
//                        ClassifierResults nnTestResults = nn.getTestPrediction(testInstances);
//                        nnTestResults.findAllStatsOnce();
                        dtw.setWarpingWindow((double) trainParam / 100);
//                        orig.setParamsFromParamId(trainInstances, trainParam);
                        orig2.setParamsFromParamId(trainInstances, trainParam);
                        ClassifierResults results = nn.getTrainPrediction(trainInstances);
                        results.findAllStatsOnce();
                        double nnLoocv = results.acc;
//                        double origLoocv = orig.loocvAccAndPreds(trainInstances,  trainParam)[0];
                        double orig2Loocv = orig2.loocvAccAndPreds(trainInstances,  trainParam)[0];
//                        System.out.println(orig2Loocv);
//                        System.out.println("---");
//                        System.out.println(nnLoocv);
//                        System.out.println("---");
                        System.out.println(datasetName
//                            + ", " + origLoocv
                            + ", " + orig2Loocv
                            + ", " + nnLoocv
//                            + ", " + trainAcc
//                            + ", " + origTestResults.acc
//                            + ", " + orig2TestResults.acc
//                            + ", " + nnTestResults.acc
//                            + ", " + testAcc
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        executorService.shutdown();
//        int seed = 3;
//        NearestNeighbour nearestNeighbour = new NearestNeighbour();
//        nearestNeighbour.setUseRandomTieBreak(false);
//        nearestNeighbour.setUseEarlyAbandon(false);
//        nearestNeighbour.setSampleSizePercentage(1);
//        nearestNeighbour.setCvTrain(true);
//        Dtw dtw = new Dtw();
//        dtw.setWarpingWindow(0.02);
//        nearestNeighbour.setDistanceMeasure(dtw);
//        nearestNeighbour.setNeighbourWeighter(WEIGHT_UNIFORM);
//        nearestNeighbour.setSeed(seed);
//        String datasetName = "GunPoint";
//        String checkpointDirPath = "/scratch/checkpoints/" + datasetName;
//        new File(checkpointDirPath).mkdirs();
////        nearestNeighbour.setSavePath(checkpointDirPath);
//        String datasetPath = "/scratch/Datasets/TSCProblems2015/" + datasetName;
//        Instances[] split = Utilities.loadSplitInstances(new File(datasetPath));
//        Instances trainInstances = split[0];
//        Instances testInstances = split[1];
//        split = InstanceTools.resampleTrainAndTestInstances(trainInstances, testInstances, seed);
//        trainInstances = split[0];
//        testInstances = split[0];
//        ClassifierResults trainResults = nearestNeighbour.getTrainPrediction(trainInstances);
//        trainResults.findAllStatsOnce();
//        ClassifierResults testResults = nearestNeighbour.getTestPrediction(testInstances);
//        testResults.findAllStatsOnce();
//        System.out.println(trainResults.acc);
//        System.out.println(testResults.acc);
    }

    public void setUseRandomTieBreak(final boolean useRandomTieBreak) {
        this.useRandomTieBreak = useRandomTieBreak;
    }

    public void setUseEarlyAbandon(final boolean useEarlyAbandon) {
        this.useEarlyAbandon = useEarlyAbandon;
    }

    public void setSeed(long seed) {
        random.setSeed(seed);
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    public ClassifierResults getTrainPrediction(Instances trainInstances) throws Exception {
        buildClassifier(trainInstances);
        ClassifierResults results = new ClassifierResults();
        results.setNumClasses(originalTrainInstances.numClasses());
        results.setNumInstances(trainNearestNeighbourFinders.size());
        for (NearestNeighbourFinder nearestNeighbourFinder : trainNearestNeighbourFinders) {
            double classValue = nearestNeighbourFinder.getInstance().classValue();
            double[] predictions = nearestNeighbourFinder.predict();
            int[] maxIndices = argMax(predictions);
            double prediction;
            if(maxIndices.length > 1) {
                boolean b = true;
            }
            prediction = maxIndices[0];
            if(prediction != classValue) {
                System.out.println(nearestNeighbourFinder.getInstance().weight());
            }
            results.storeSingleResult(classValue, predictions);
        }
        try {
            results.memory = SizeOf.deepSizeOf(this);
        } catch (Exception e) {

        }
        results.setName(toString());
        results.setParas(getParameters());
        results.setTrainTime(getTrainDuration());
        results.setTestTime(-1);
        return results;
    }

    public ClassifierResults getTestPrediction(Instances testInstances) throws Exception {
        double[][] allPredictions = distributionForInstances(testInstances);
        ClassifierResults results = new ClassifierResults();
        results.setNumClasses(testInstances.numClasses());
        results.setNumInstances(testInstances.numInstances());
        for (int i = 0; i < allPredictions.length; i++) {
            double classValue = testNearestNeighbourFinders.get(i).getInstance().classValue();
            double[] predictions = allPredictions[i];
            int[] maxIndices = argMax(predictions);
            double prediction;
            if(maxIndices.length > 1) {
                boolean b = true;
            }
            prediction = maxIndices[0];
            if(prediction != classValue) {
                System.out.println(testNearestNeighbourFinders.get(i).getInstance().weight());
            }
            results.storeSingleResult(classValue, predictions);
        }
        try {
            results.memory = SizeOf.deepSizeOf(this);
        } catch (Exception e) {

        }
        results.setName(toString());
        results.setParas(getParameters());
        results.setTrainTime(getTrainDuration());
        results.setTestTime(getTestDuration());
        return results;
    }

    @Override
    public void buildClassifier(final Instances trainInstances) throws Exception {
        resumeFromCheckpoint();
        long timeStamp = System.nanoTime();
        if (!trainInstances.equals(this.originalTrainInstances)) {
            // reset as train dataset has change
            this.originalTrainInstances = trainInstances;
            this.originalSampledTrainInstances = new Instances(trainInstances, 0);
            instancesByClass = Utilities.instancesByClass(trainInstances);
            classSamplingProbabilities = new double[trainInstances.numClasses()];
            for (int i = 0; i < classSamplingProbabilities.length; i++) {
                classSamplingProbabilities[i] = (double) instancesByClass[i].numInstances() / trainInstances.numInstances();
            }
            classDistribution = Utilities.classDistribution(trainInstances);
            trainNearestNeighbourFinders = new ArrayList<>();
            trainDuration = 0;
            testDuration = 0;
            trainDuration += System.nanoTime() - timeStamp; // todo make timestamp class var (but don't copy over when resotring from checkpoint) and use inside checkpoint func
            checkpoint();
            timeStamp = System.nanoTime();
        }
        int sampleSize = getSampleSize();
        while (originalSampledTrainInstances.numInstances() < sampleSize && withinContractTrainTime()) {
            Instance sampledInstance = sampleTrainInstance();
            originalSampledTrainInstances.add(sampledInstance);
//            if(sampledInstance.weight() == 1) {
//                boolean b = true;
//            }
            if (cvTrain) {
                NearestNeighbourFinder newNearestNeighbourFinder = new NearestNeighbourFinder(sampledInstance);
                for(NearestNeighbourFinder nearestNeighbourFinder : trainNearestNeighbourFinders) {
                    double distance = nearestNeighbourFinder.addNeighbour(sampledInstance);
                    newNearestNeighbourFinder.addNeighbour(nearestNeighbourFinder.getInstance(), distance);
                }
                trainNearestNeighbourFinders.add(newNearestNeighbourFinder);
            }
            trainDuration += System.nanoTime() - timeStamp;
            checkpoint();
            timeStamp = System.nanoTime();
        }
        trainDuration += System.nanoTime() - timeStamp;
        checkpoint(true); // todo round robin sampling vs stratified vs random
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public String getParameters() {
        return null; // todo delegate to getOptions
    }

    // todo classifierResults train time and productive train time to distinguish between timings with / without non-classifier stuff, e.g. file writing

    public long getTrainDuration() {
        return trainDuration;
    }

    public long getTestDuration() {
        return testDuration + predictDuration;
    }

    private int getSampleSize() {
        return (int) (sampleSizePercentage * originalTrainInstances.numInstances());
    }

    /**
     * can we iterate through another neighbour sample within the train time contract?
     * @return true if can do another iteration of looking for neighbours
     */
    private boolean withinContractTrainTime() {
        return true; // todo test if within contract
    }

    private int i = 0;

    private Instance sampleTrainInstance() {
        return originalTrainInstances.get(i++);
//        int sampleClass = (int) findSampleClass();
//        Instances homogeneousInstances = instancesByClass[sampleClass]; // instances of the class value
//        Instance sampledInstance = homogeneousInstances.remove(random.nextInt(homogeneousInstances.numInstances()));
//        classSamplingProbabilities[sampleClass]--;
//        ArrayUtilities.add(classSamplingProbabilities, classDistribution);
//        return sampledInstance;
    }

    private double findSampleClass() {
        int[] highestProbabilityClasses = argMax(classSamplingProbabilities);
        if(highestProbabilityClasses.length > 1) {
            return highestProbabilityClasses[random.nextInt(highestProbabilityClasses.length)];
        } else {
            return highestProbabilityClasses[0];
        }
    }

    public double getSampleSizePercentage() {
        return sampleSizePercentage;
    }

    public void setSampleSizePercentage(final double percentage) {
        Utilities.percentageCheck(percentage);
        this.sampleSizePercentage = percentage;
    }

    private int getK() {
        return 1 + (int) kPercentage * originalTrainInstances.numInstances();
    }

    public double getKPercentage() {
        return kPercentage;
    }

    // todo load from checkpoint

    public void setKPercentage(final double percentage) {
        Utilities.percentageCheck(percentage);
        this.kPercentage = percentage;
    }

    public boolean isCvTrain() {
        return cvTrain;
    }

    public void setCvTrain(final boolean cvTrain) {
        this.cvTrain = cvTrain;
    }

    public boolean usesRandomTieBreak() {
        return useRandomTieBreak;
    }

    @Override
    public double classifyInstance(final Instance instance) throws Exception {
        double[] prediction = distributionForInstance(instance);
        int[] maxIndices = argMax(prediction);
        if (useRandomTieBreak) {
            return prediction[maxIndices[random.nextInt(maxIndices.length)]];
        } else {
            return prediction[maxIndices[0]];
        }
    }

    public double[][] distributionForInstances(final Instances testInstances) throws Exception {
        resumeFromCheckpoint();
        long timeStamp = System.nanoTime();
        if (!testInstances.equals(originalTestInstances)) {
            originalTestInstances = testInstances;
            testNearestNeighbourFinders = new ArrayList<>();
            for (Instance testInstance : testInstances) {
                testNearestNeighbourFinders.add(new NearestNeighbourFinder(testInstance));
            }
            testDuration = 0;
            testDuration += System.nanoTime() - timeStamp;
            checkpoint();
            timeStamp = System.nanoTime();
        }
        sampledTrainInstances = new Instances(originalSampledTrainInstances);
        while (!sampledTrainInstances.isEmpty()) {
            Instance sampledTrainInstance = sampledTrainInstances.remove(0);//random.nextInt(sampledTrainInstances.numInstances()));
            for (NearestNeighbourFinder testNearestNeighbourFinder : testNearestNeighbourFinders) {
                testNearestNeighbourFinder.addNeighbour(sampledTrainInstance);
            }
            testDuration += System.nanoTime() - timeStamp;
            checkpoint();
            timeStamp = System.nanoTime();
        }
        testDuration += System.nanoTime() - timeStamp;
        timeStamp = System.nanoTime();
        double[][] predictions = new double[testNearestNeighbourFinders.size()][];
        for (int i = 0; i < predictions.length; i++) {
            double[] prediction = testNearestNeighbourFinders.get(i).predict();
            if(useAbsoluteProbability) {
                int[] maxIndices = argMax(prediction);
                int maxIndex = 0;
                if(maxIndices.length > 1 && useRandomTieBreak) {
                    maxIndex = maxIndices[random.nextInt(maxIndices.length)];
                }
                prediction = new double[prediction.length];
                prediction[maxIndex]++;
            }
            predictions[i] = prediction;
        }
        predictDuration = System.nanoTime() - timeStamp;
        return predictions;
    }

    private void resumeFromCheckpoint() throws Exception {
        if(!hasLoadedFromCheckpoint && checkpointFilePath != null && new File(checkpointFilePath).exists()) {
            loadFromFile(checkpointFilePath);
            hasLoadedFromCheckpoint = true;
        }
    }

    private void checkpoint() throws IOException {
        checkpoint(false);
    }

    private void checkpoint(boolean force) throws IOException {
        if(checkpointFilePath != null && (force || System.nanoTime() - lastCheckpointTimeStamp > minCheckpointInterval)) {
            saveToFile(checkpointFilePath);
            lastCheckpointTimeStamp = System.nanoTime();
        }
    }

    @Override
    public double[] distributionForInstance(final Instance testInstance) throws Exception {
        return distributionForInstances(Utilities.instanceToInstances(testInstance))[0];
    }

    public DistanceMeasure getDistanceMeasure() {
        return distanceMeasure;
    }

    // todo getOptions setOptions

    public void setDistanceMeasure(final DistanceMeasure distanceMeasure) {
        this.distanceMeasure = distanceMeasure;
    }

    public boolean usesEarlyAbandon() {
        return useEarlyAbandon;
    }

    @Override
    public void setSavePath(String path) {
        File file = new File(path);
        file.mkdirs();
        Utilities.setOpenPermissions(file);
        this.checkpointFilePath = file.getPath() + "/" + CHECKPOINT_FILE_NAME;
    }

    @Override
    public void copyFromSerObject(final Object obj) throws Exception {
        NearestNeighbour other = (NearestNeighbour) obj;
        originalTestInstances = other.originalTestInstances;
        originalTrainInstances = other.originalTrainInstances;
        sampledTrainInstances = other.sampledTrainInstances;
        originalSampledTrainInstances = other.originalSampledTrainInstances; // todo make sure we've copied all the fields
        random = other.random;
        classSamplingProbabilities = other.classSamplingProbabilities;
        classDistribution = other.classDistribution;
        instancesByClass = other.instancesByClass;
        trainNearestNeighbourFinders = other.trainNearestNeighbourFinders;
        kPercentage = other.kPercentage;
        trainDuration = other.trainDuration;
        testDuration = other.testDuration;
        predictDuration = other.predictDuration;
        cvTrain = other.cvTrain;
        useRandomTieBreak = other.useRandomTieBreak;
        testNearestNeighbourFinders = other.testNearestNeighbourFinders;
        distanceMeasure = other.distanceMeasure;
        useEarlyAbandon = other.useEarlyAbandon;
        neighbourWeighter = other.neighbourWeighter;
    }

    public NeighbourWeighter getNeighbourWeighter() {
        return neighbourWeighter;
    }

    public void setNeighbourWeighter(final NeighbourWeighter neighbourWeighter) {
        this.neighbourWeighter = neighbourWeighter;
    }

    @Override
    public void setTimeLimit(final long time) { // todo split to train time limit and test time limit
        trainDurationLimit = time;
    }

    public interface NeighbourWeighter extends Serializable {
        double weight(double distance);
    }

    private class NearestNeighbourFinder implements Serializable {
        private Instance instance;
        private TreeMap<Double, List<Instance>> nearestNeighbours = new TreeMap<>();
        private int neighbourCount = 0;

        public NearestNeighbourFinder(Instance instance) {
            this.instance = instance;
        }

        public Instance getInstance() {
            return instance;
        }

        public double addNeighbour(Instance neighbour) {
            double distance = distanceMeasure.distance(instance, neighbour, findCutOff());
            addNeighbour(neighbour, distance);
            return distance;
        }

        private double findCutOff() {
            if (useEarlyAbandon) {
                return nearestNeighbours.lastKey();
            } else {
                return Double.POSITIVE_INFINITY;
            }
        }

        public void addNeighbour(Instance neighbour, double distance) {
            int k = getK();
            List<Instance> neighbours = nearestNeighbours.computeIfAbsent(distance, key -> new ArrayList<>());
            neighbours.add(neighbour);
            neighbourCount++;
            if(neighbourCount > k) {
                Map.Entry<Double, List<Instance>> furthestNeighboursEntry = nearestNeighbours.lastEntry();
                if (neighbourCount - k >= furthestNeighboursEntry.getValue().size()) {
                    neighbourCount -= nearestNeighbours.pollLastEntry().getValue().size();
                }
            }
        }

        public double[] predict() {
            if(this.neighbourCount > 1) {
                boolean b = true;
            }
            int k = getK();
            double[] predictions = new double[instance.numClasses()];
            Iterator<Map.Entry<Double, List<Instance>>> iterator = nearestNeighbours.entrySet().iterator();
            Map.Entry<Double, List<Instance>> entry = iterator.next();
            neighbourCount = 0;
            double distance = entry.getKey();
            List<Instance> neighbours = entry.getValue();
            while(neighbourCount + neighbours.size() <= k) {
                for(Instance neighbour : neighbours) {
                    predictions[(int) neighbour.classValue()] += neighbourWeighter.weight(distance);
                }
                neighbourCount += neighbours.size();
                if(neighbourCount < k) {
                    entry = iterator.next();
                    neighbours = entry.getValue();
                    distance = entry.getKey();
                }
            }
            if(neighbourCount < k) {
                neighbours = new ArrayList<>(neighbours);
                while (neighbourCount < k) {
                    Instance neighbour = neighbours.remove(0);
                    predictions[(int) neighbour.classValue()] += neighbourWeighter.weight(distance);
                    neighbourCount++;
                }
            }
            ArrayUtilities.normalise(predictions);
            return predictions;
        }
    }
}
