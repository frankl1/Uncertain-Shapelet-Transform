package timeseriesweka.classifiers.nn;

import net.sourceforge.sizeof.SizeOf;
import timeseriesweka.classifiers.CompressedCheckpointClassifier;
import timeseriesweka.classifiers.ContractClassifier;
import timeseriesweka.classifiers.SaveParameterInfo;
import timeseriesweka.classifiers.ensembles.elastic_ensemble.DTW1NN;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;
import utilities.*;
import weka.classifiers.AbstractClassifier;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static utilities.Utilities.argMax;
import static utilities.Utilities.trainAndTest;

public class Nn extends AbstractClassifier implements Serializable, Reproducible, SaveParameterInfo, CompressedCheckpointClassifier, ContractClassifier, OptionsSetter {

    // todo implement contracting

    public static final NeighbourWeighter WEIGHT_BY_DISTANCE = distance -> 1 / (1 + distance);
    public static final NeighbourWeighter WEIGHT_UNIFORM = distance -> 1;
    private static final String CHECKPOINT_FILE_NAME = "checkpoint.ser.gzip";
    private String checkpointFilePath;
    private Instances originalTrainInstances = null;
    private double sampleSizePercentage = 1;
    private Instances sampledTrainInstances;
    private Random random = new Random();
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
    private long trainDurationLimit = -1; // todo implement contract
    private boolean hasLoadedFromCheckpoint = false;
    private boolean trainInitialised = false;
    private boolean trainDone = false;
    private boolean testInitialised = false;
    private boolean testDone = false;
    private Sampler sampler = new RandomRoundRobinSampler();

    @Override
    public boolean setOption(final String key, final String value) {
        reset();
        return false; // todo + getOptions
    }

    public Nn() {
        setDistanceMeasure(new Dtw());
    }

    public static void main(String[] args) throws Exception {
        ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        executorService.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        String datasetsPath = "/scratch/Datasets/TSCProblems2015/";
        System.out.println(datasetsPath);
        List<String> datasetNames = new ArrayList<>();
//        datasetNames.add("WormsTwoClass");
//        for(File file : new File(datasetsPath).listFiles(new FileFilter() {
//            @Override
//            public boolean accept(final File file) {
//                return file.isDirectory();
//            }
//        })) {
//            datasetNames.add(file.getName());
//        }
        BufferedReader reader = new BufferedReader(new FileReader("/scratch/datasetList.txt"));
        String line;
        while ((line = reader.readLine()) != null) {
            datasetNames.add(line);
        }
        reader.close();
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
                        DTW1NN orig = new DTW1NN();
//                        Dtw1Nn2 orig2 = new Dtw1Nn2();
                        Nn nn = new Nn();
//                        nn.setSavePath("/scratch/checkpoints/" + datasetName);
                        nn.setCvTrain(true);
                        nn.setUseEarlyAbandon(false);
                        nn.setUseRandomTieBreak(false);
                        nn.setNeighbourWeighter(WEIGHT_UNIFORM);
                        Dtw dtw = new Dtw();
                        nn.setDistanceMeasure(dtw);
                        String previousTestResult = "/run/user/33190/gvfs/smb-share:server=cmptscsvr.cmp.uea.ac.uk,share=ueatsc/Results_7_2_19/JayMovingInProgress/EEConstituentResults/" + type + "_Rn_1NN/Predictions/" + datasetName + "/testFold" + foldIndex + ".csv";
                        String previousTrainResult = "/run/user/33190/gvfs/smb-share:server=cmptscsvr.cmp.uea.ac.uk,share=ueatsc/Results_7_2_19/JayMovingInProgress/EEConstituentResults/" + type + "_Rn_1NN/Predictions/" + datasetName + "/trainFold" + foldIndex + ".csv";
                        BufferedReader testReader = new BufferedReader(new FileReader(previousTestResult));
                        testReader.readLine();
                        int testParam = Integer.parseInt(testReader.readLine());
                        double testAcc = Double.parseDouble(testReader.readLine());
                        testReader.close();
                        BufferedReader trainReader = new BufferedReader(new FileReader(previousTrainResult));
                        trainReader.readLine();
                        int trainParam = Integer.parseInt(trainReader.readLine());
                        double trainAcc = Double.parseDouble(trainReader.readLine());
                        trainReader.close();
                        dtw.setWarpingWindow((double) trainParam / 100);
                        orig.setParamsFromParamId(trainInstances, testParam);
//                        orig2.setParamsFromParamId(trainInstances, testParam);
                        ClassifierResults origTestResults = trainAndTest(orig, trainInstances, testInstances);
//                        ClassifierResults orig2TestResults = trainAndTest(orig2, trainInstances, testInstances);
                        nn.buildClassifier(trainInstances);
                        ClassifierResults nnTestResults = nn.getTestPrediction(trainInstances, testInstances);
                        nnTestResults.findAllStatsOnce();
                        dtw.setWarpingWindow((double) trainParam / 100);
                        orig.setParamsFromParamId(trainInstances, trainParam);
//                        orig2.setParamsFromParamId(trainInstances, trainParam);
                        ClassifierResults results = nn.getTrainPrediction(trainInstances);
                        results.findAllStatsOnce();
                        double nnLoocv = results.acc;
                        double origLoocv = orig.loocvAccAndPreds(trainInstances,  trainParam)[0];
//                        double orig2Loocv = orig2.loocvAccAndPreds(trainInstances,  trainParam)[0];
//                        System.out.println(orig2Loocv);
//                        System.out.println("---");
//                        System.out.println(nnLoocv);
//                        System.out.println("---");
                        System.out.println(datasetName
                            + ", " + origLoocv
//                            + ", " + orig2Loocv
                            + ", " + nnLoocv
                            + ", " + trainAcc
                            + ", " + origTestResults.acc
//                            + ", " + orig2TestResults.acc
                            + ", " + nnTestResults.acc
                            + ", " + testAcc
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

    public Sampler getSampler() {
        return sampler;
    }

    public void setSampler(final Sampler sampler) {
        this.sampler = sampler;
        reset();
    }

    public void setUseRandomTieBreak(final boolean useRandomTieBreak) {
        this.useRandomTieBreak = useRandomTieBreak;
        reset();
    }

    public void setUseEarlyAbandon(final boolean useEarlyAbandon) {
        this.useEarlyAbandon = useEarlyAbandon;
        reset();
    }

    public void setSeed(long seed) {
        this.seed = seed;
        reset();
    }

    public void setRandom(Random random) {
        this.random = random;
        reset();
    }

    public ClassifierResults getTrainPrediction(Instances trainInstances) throws Exception {
        buildClassifier(trainInstances);
        return getPrediction(originalSampledTrainInstances, trainPredictions);
    }

    public ClassifierResults getTestPrediction(Instances trainInstances, Instances testInstances) throws Exception {
        buildClassifier(trainInstances);
        double[][] allPredictions = distributionForInstances(testInstances);
        return getPrediction(testInstances, allPredictions);
    }

    private ClassifierResults getPrediction(Instances instances, double[][] allPredictions) {
        ClassifierResults results = new ClassifierResults();
        results.setNumClasses(instances.numClasses());
        results.setNumInstances(instances.numInstances());
        for (int i = 0; i < allPredictions.length; i++) {
            double classValue = instances.get(i).classValue();
            double[] predictions = allPredictions[i];
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

    public void reset() {
        if(seed != null) {
            random.setSeed(seed);
        }
        trainDone = false;
        testDone = false;
        trainInitialised = false;
        testInitialised = false;
    }

    private Long seed = null;
    private double[][] trainPredictions;

    @Override
    public void buildClassifier(final Instances trainInstances) throws Exception {
        long timeStamp;
        resumeFromCheckpoint();
        if(!trainInitialised) {
             // reset as train dataset has change
            timeStamp = System.nanoTime();
            this.originalTrainInstances = trainInstances;
            this.originalSampledTrainInstances = new Instances(trainInstances, 0);
            sampler.setInstances(new Instances(trainInstances));
            trainNearestNeighbourFinders = new ArrayList<>();
            trainInitialised = true;
            trainDuration = 0;
            testDuration = 0;
            trainDuration += System.nanoTime() - timeStamp;
            checkpoint();
        }
        if(!trainDone) {
            timeStamp = System.nanoTime();
            int sampleSize = getSampleSize();
            while (originalSampledTrainInstances.numInstances() < sampleSize && withinContractTrainTime()) {
                Instance sampledInstance = sampler.next();
                originalSampledTrainInstances.add(sampledInstance);
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
            if(cvTrain) {
                trainPredictions = new double[trainNearestNeighbourFinders.size()][];
                for(int i = 0; i < trainPredictions.length; i++) {
                    trainPredictions[i] = trainNearestNeighbourFinders.get(i).predict();
                    checkpoint();
                }
            }
            trainDone = true;
            trainDuration += System.nanoTime() - timeStamp;
            checkpoint(true);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public String getParameters() {
        return null; // todo delegate to getOptions
    }

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

    public double getSampleSizePercentage() {
        return sampleSizePercentage;
    }

    public void setSampleSizePercentage(final double percentage) {
        Utilities.percentageCheck(percentage);
        this.sampleSizePercentage = percentage;
        reset();
    }

    private int getK() {
        return 1 + (int) kPercentage * originalTrainInstances.numInstances();
    }

    public double getKPercentage() {
        return kPercentage;
    }

    public void setKPercentage(final double percentage) {
        Utilities.percentageCheck(percentage);
        this.kPercentage = percentage;
        reset();
    }

    public boolean isCvTrain() {
        return cvTrain;
    }

    public void setCvTrain(final boolean cvTrain) {
        this.cvTrain = cvTrain;
        reset();
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
        long timeStamp;
        resumeFromCheckpoint();
        if(!testInitialised) {
            timeStamp = System.nanoTime();
            originalTestInstances = testInstances;
            testNearestNeighbourFinders = new ArrayList<>();
            for (Instance testInstance : testInstances) {
                testNearestNeighbourFinders.add(new NearestNeighbourFinder(testInstance));
            }
            sampledTrainInstances = new Instances(originalSampledTrainInstances);
            testInitialised = true;
            testDuration = 0;
            testDuration += System.nanoTime() - timeStamp;
            checkpoint();
        }
        if(!testDone) {
            timeStamp = System.nanoTime();
            while (!sampledTrainInstances.isEmpty()) {
                Instance sampledTrainInstance = sampledTrainInstances.remove(0);
                for (NearestNeighbourFinder testNearestNeighbourFinder : testNearestNeighbourFinders) {
                    testNearestNeighbourFinder.addNeighbour(sampledTrainInstance);
                }
                testDuration += System.nanoTime() - timeStamp;
                checkpoint();
                timeStamp = System.nanoTime();
            }
            testDone = true;
            testDuration += System.nanoTime() - timeStamp;
            checkpoint(true);
        }
        timeStamp = System.nanoTime();
        double[][] predictions = new double[testNearestNeighbourFinders.size()][];
        for (int i = 0; i < predictions.length; i++) {
            double[] prediction = testNearestNeighbourFinders.get(i).predict();
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

    public void setDistanceMeasure(final DistanceMeasure distanceMeasure) {
        this.distanceMeasure = distanceMeasure;
        reset();
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
        reset();
    }

    @Override
    public void copyFromSerObject(final Object obj) throws Exception {
        Nn other = (Nn) obj;
        originalTestInstances = other.originalTestInstances;
        originalTrainInstances = other.originalTrainInstances;
        sampledTrainInstances = other.sampledTrainInstances;
        originalSampledTrainInstances = other.originalSampledTrainInstances;
        random = other.random;
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
        sampleSizePercentage = other.sampleSizePercentage;
        testDuration = other.testDuration;
        trainDuration = other.trainDuration;
        testDone = other.testDone;
        testInitialised = other.testInitialised;
        trainInitialised = other.trainInitialised;
        trainDone = other.trainDone;
        sampler = other.sampler;
        // todo copy over contracting vars
    }

    public NeighbourWeighter getNeighbourWeighter() {
        return neighbourWeighter;
    }

    public void setNeighbourWeighter(final NeighbourWeighter neighbourWeighter) {
        this.neighbourWeighter = neighbourWeighter;
        reset();
    }

    @Override
    public void setTimeLimit(final long time) { // todo split to train time limit and test time limit
        trainDurationLimit = time;
        reset();
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
            double[] predictions = new double[instance.numClasses()];
            Iterator<Map.Entry<Double, List<Instance>>> iterator = nearestNeighbours.entrySet().iterator();
            if(!iterator.hasNext()) {
                predictions[random.nextInt(predictions.length)]++;
                return predictions;
            }
            Map.Entry<Double, List<Instance>> entry = iterator.next();
            neighbourCount = 0;
            int k = getK();
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
