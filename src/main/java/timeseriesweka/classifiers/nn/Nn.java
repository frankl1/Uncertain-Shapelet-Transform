package timeseriesweka.classifiers.nn;

import net.sourceforge.sizeof.SizeOf;
import timeseriesweka.classifiers.CompressedCheckpointClassifier;
import timeseriesweka.classifiers.ContractClassifier;
import timeseriesweka.classifiers.SaveParameterInfo;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;
import utilities.*;
import weka.classifiers.AbstractClassifier;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static utilities.Utilities.argMax;
import static utilities.Utilities.trainAndTest;

public class Nn extends AbstractClassifier implements Serializable, Reproducible, SaveParameterInfo, CompressedCheckpointClassifier, ContractClassifier, OptionsSetter {

    public static final NeighbourWeighter WEIGHT_BY_DISTANCE = distance -> 1 / (1 + distance);
    public static final NeighbourWeighter WEIGHT_UNIFORM = distance -> 1;
    private static final String CHECKPOINT_FILE_NAME = "checkpoint.ser.gzip";
    private final List<Instance> sampledTrainInstances = new ArrayList<>();
    private final List<NearestNeighbourFinder> trainNearestNeighbourFinders = new ArrayList<>();
    private final List<Instance> originalSampledTrainInstances = new ArrayList<>();
    private String savePath;
    private Instances originalTrainInstances = null;
    private double sampleSizePercentage = 1;
    private Random random = new Random();
    private double kPercentage = 0;
    private boolean cvTrain = false;
    private boolean useRandomTieBreak = true;
    private Instances originalTestInstances;
    private final List<NearestNeighbourFinder> testNearestNeighbourFinders = new ArrayList<>();
    private DistanceMeasure distanceMeasure;
    private boolean useEarlyAbandon = false;
    private NeighbourWeighter neighbourWeighter = WEIGHT_UNIFORM; // todo non static classes
    // todo implement contract
    private Sampler sampler = new RandomRoundRobinSampler();
    private String checkpointFilePath;
    private long predictionContract = -1;
    private long trainContract = -1;
    private long testContract = -1;
    private Long seed = null;
    private double[][] trainResults;
    private boolean resetTrain = true; // todo resetTrain after specific setters, e.g. setTrainCv
    private boolean resetTest = true;
    private long trainTime;
    private boolean regenerateTrainResults = true;
    private boolean regenerateTestResults = true;
    private long testTime;
    private boolean checkpointing = false;
    private long lastCheckpointTimeStamp = 0;
    private long minCheckpointInterval = TimeUnit.NANOSECONDS.convert(10, TimeUnit.MINUTES); // todo getters / setters / ship out to parent class

    public Nn() {
        setDistanceMeasure(new Dtw());
    }

    public static void main(String[] args) throws Exception {
//        ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//        executorService.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        String datasetsDirPath = "/scratch/Datasets/TSCProblems2015/";
        System.out.println(datasetsDirPath);
        List<String> datasetNames = new ArrayList<>();
        datasetNames.add("GunPoint");
        String datasetName = datasetNames.get(0);
        Instances trainInstances = ClassifierTools.loadData(datasetsDirPath + datasetName + "/" + datasetName + "_TRAIN.arff");
        Instances testInstances = ClassifierTools.loadData(datasetsDirPath + datasetName + "/" + datasetName + "_TEST.arff");
        Instances[] splitInstances = InstanceTools.resampleTrainAndTestInstances(trainInstances, testInstances, 0);
        trainInstances = splitInstances[0];
        testInstances = splitInstances[1];
        Nn nn = new Nn();
        nn.setSavePath("checkpoints");
        nn.setCheckpointing(true);
        nn.setSeed(0);
        nn.setSampleSizePercentage(1);
        nn.setCvTrain(true);
        nn.buildClassifier(trainInstances);
        ClassifierResults testResults = nn.getTestResults(testInstances);
        ClassifierResults trainResults = nn.getTrainResults();
        System.out.print(trainResults.acc);
        System.out.print(", ");
        System.out.println(testResults.acc);
//        for(int i = 0; i <= trainInstances.numInstances(); i++) {
//            if(i == trainInstances.numInstances() - 2) {
//                boolean b = true;
//            }
//            nn.setSampleSizePercentage((double) i / trainInstances.numInstances());
//            ClassifierResults testResults = trainAndTest(nn, trainInstances, testInstances);
//            ClassifierResults trainResults = nn.getTrainResults();
//            System.out.print(trainResults.acc);
//            System.out.print(", ");
//            System.out.println(testResults.acc);
//        }

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
//        int foldIndex = 0;
//        String type = "DTW";
//        for(String datasetName : datasetNames) {
//            String datasetPath = datasetsPath + datasetName;
////            executorService.submit(new Runnable() {
////                @Override
////                public void run() {
////                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
//                    try {
////                        System.out.println("loading " + datasetName);
//                        Instances[] split = Utilities.loadSplitInstances(new File(datasetPath));
//                        Instances trainInstances = split[0];
//                        Instances testInstances = split[1];
//                        DTW1NN orig = new DTW1NN();
////                        Dtw1Nn2 orig2 = new Dtw1Nn2();
//                        Nn nn = new Nn();
////                        nn.setSavePath("/scratch/checkpoints/" + datasetName);
//                        nn.setCvTrain(true);
//                        nn.setUseEarlyAbandon(false);
//                        nn.setUseRandomTieBreak(false);
//                        nn.setNeighbourWeighter(WEIGHT_UNIFORM);
//                        Dtw dtw = new Dtw();
//                        nn.setDistanceMeasure(dtw);
//                        String previousTestResult = "/run/user/33190/gvfs/smb-share:server=cmptscsvr.cmp.uea.ac.uk,share=ueatsc/Results_7_2_19/JayMovingInProgress/EEConstituentResults/" + type + "_Rn_1NN/Predictions/" + datasetName + "/testFold" + foldIndex + ".csv";
//                        String previousTrainResult = "/run/user/33190/gvfs/smb-share:server=cmptscsvr.cmp.uea.ac.uk,share=ueatsc/Results_7_2_19/JayMovingInProgress/EEConstituentResults/" + type + "_Rn_1NN/Predictions/" + datasetName + "/trainFold" + foldIndex + ".csv";
//                        BufferedReader testReader = new BufferedReader(new FileReader(previousTestResult));
//                        testReader.readLine();
//                        int testParam = Integer.parseInt(testReader.readLine());
//                        double testAcc = Double.parseDouble(testReader.readLine());
//                        testReader.close();
//                        BufferedReader trainReader = new BufferedReader(new FileReader(previousTrainResult));
//                        trainReader.readLine();
//                        int trainParam = Integer.parseInt(trainReader.readLine());
//                        double trainAcc = Double.parseDouble(trainReader.readLine());
//                        trainReader.close();
//                        dtw.setWarpingWindow((double) trainParam / 100);
//                        orig.setParamsFromParamId(trainInstances, testParam);
////                        orig2.setParamsFromParamId(trainInstances, testParam);
//                        ClassifierResults origTestResults = trainAndTest(orig, trainInstances, testInstances);
////                        ClassifierResults orig2TestResults = trainAndTest(orig2, trainInstances, testInstances);
//                        nn.buildClassifier(trainInstances);
//                        ClassifierResults nnTestResults = nn.getTestPrediction(trainInstances, testInstances);
//                        nnTestResults.findAllStatsOnce();
//                        dtw.setWarpingWindow((double) trainParam / 100);
//                        orig.setParamsFromParamId(trainInstances, trainParam);
////                        orig2.setParamsFromParamId(trainInstances, trainParam);
//                        ClassifierResults results = nn.getTrainResults();
//                        results.findAllStatsOnce();
//                        double nnLoocv = results.acc;
//                        double origLoocv = orig.loocvAccAndPreds(trainInstances,  trainParam)[0];
////                        double orig2Loocv = orig2.loocvAccAndPreds(trainInstances,  trainParam)[0];
////                        System.out.println(orig2Loocv);
////                        System.out.println("---");
////                        System.out.println(nnLoocv);
////                        System.out.println("---");
//                        System.out.println(datasetName
//                            + ", " + origLoocv
////                            + ", " + orig2Loocv
//                            + ", " + nnLoocv
//                            + ", " + trainAcc
//                            + ", " + origTestResults.acc
////                            + ", " + orig2TestResults.acc
//                            + ", " + nnTestResults.acc
//                            + ", " + testAcc
//                        );
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
////                }
////            });
//        }
//        executorService.shutdown();
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
//        ClassifierResults trainResults = nearestNeighbour.getTrainResults(trainInstances);
//        trainResults.findAllStatsOnce();
//        ClassifierResults testResults = nearestNeighbour.getTestPrediction(testInstances);
//        testResults.findAllStatsOnce();
//        System.out.println(trainResults.acc);
//        System.out.println(testResults.acc);
    }

    public void setSeed(long seed) {
        this.seed = seed;
        reset();
    }

    public void setRandom(Random random) {
        this.random = random;
        reset();
    }

    public ClassifierResults getTrainResults() {
        return getResults(originalTrainInstances, trainResults);
    }

    private ClassifierResults getResults(Instances instances, double[][] allPredictions) {
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
        results.setTrainTime(getTrainTime());
        results.setTestTime(getTestTime());
        results.findAllStatsOnce();
        return results;
    }

    @Override
    public String toString() {
        return distanceMeasure.toString() + "-" + getClass().getSimpleName();
    }

    @Override
    public String getParameters() {
        return null; // todo delegate to getOptions
    }

    public long getTrainTime() {
        return trainTime;
    }

    public long getTestTime() {
        return testTime;
    }

    public String getSavePath() {
        return savePath;
    }

    @Override
    public void setSavePath(String path) {
        File file = new File(path);
        Utilities.mkdir(file);
        this.savePath = path;
        this.checkpointFilePath = file.getPath() + "/" + CHECKPOINT_FILE_NAME;
    }

    public boolean usesSettingsFromCheckpoint() {
        return useSettingsFromCheckpoint;
    }

    public void setUseSettingsFromCheckpoint(final boolean useSettingsFromCheckpoint) {
        this.useSettingsFromCheckpoint = useSettingsFromCheckpoint;
    }

    private boolean useSettingsFromCheckpoint = false;

    @Override
    public void copyFromSerObject(final Object obj) throws Exception {
        reset();
        Nn other = (Nn) obj;
        if(usesSettingsFromCheckpoint()) {
            kPercentage = other.kPercentage;
            cvTrain = other.cvTrain;
            useRandomTieBreak = other.useRandomTieBreak;
            useEarlyAbandon = other.useEarlyAbandon;
            distanceMeasure = other.distanceMeasure;
            neighbourWeighter = other.neighbourWeighter;
            sampleSizePercentage = other.sampleSizePercentage;
            sampler = other.sampler;
        }
        originalTestInstances = other.originalTestInstances;
        originalTrainInstances = other.originalTrainInstances;
        sampledTrainInstances.addAll(other.sampledTrainInstances);
        originalSampledTrainInstances.addAll(other.originalSampledTrainInstances);
        trainNearestNeighbourFinders.addAll(other.trainNearestNeighbourFinders);
        trainTime = other.trainTime;
        testTime = other.testTime;
        random = other.random;
        testNearestNeighbourFinders.addAll(other.testNearestNeighbourFinders);
        trainContract = other.trainContract;
        testContract = other.testContract;
        predictionContract = other.predictionContract;
        seed = other.seed;
        trainResults = other.trainResults;
        resetTrain = other.resetTrain;
        resetTest = other.resetTest;
        regenerateTrainResults = other.regenerateTrainResults;
        regenerateTestResults = other.regenerateTestResults;
        testResults = other.testResults;
    }

    public long getMinCheckpointInterval() {
        return minCheckpointInterval;
    }

    public void setMinCheckpointInterval(long nanoseconds) {
        minCheckpointInterval = nanoseconds;
    }
    // todo generify sampler?
    // todo time prediction of train?

    public long getPredictionContract() {
        return predictionContract;
    }

    public void setPredictionContract(final long predictionContract) {
        this.predictionContract = predictionContract;
    }

    public long getTrainContract() {
        return trainContract;
    }

    public void setTrainContract(final long trainContract) {
        this.trainContract = trainContract;
    }

    public long getTestContract() {
        return testContract;
    }

    public void setTestContract(final long testContract) {
        this.testContract = testContract;
    }

    @Override
    public boolean setOption(final String key, final String value) {
        reset();
        return false; // todo + getOptions
    }

    public void reset() {
        if(seed != null) {
            random.setSeed(seed);
        }
        regenerateTrainResults = true;
        resetTrain = true;
        resetTest();
    }

    public void resetTest() {
        resetTest = true;
        regenerateTestResults = true;
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

    private long testTimeStamp;
    private long trainTimeStamp;

    @Override
    public void buildClassifier(final Instances trainInstances) throws Exception {
        resumeFromCheckpoint();
        trainTimeStamp = System.nanoTime();
        if(resetTrain) {
            trainTime = 0;
            resetTrain = false;
            this.originalTrainInstances = trainInstances;
            sampler.setInstances(trainInstances);
            originalSampledTrainInstances.clear();
            trainNearestNeighbourFinders.clear();
            if(cvTrain) {
                for(Instance trainInstance : trainInstances) {
                    trainNearestNeighbourFinders.add(new NearestNeighbourFinder(trainInstance));
                }
            }
            trainCheckpoint();
        }
        int sampleSize = (int) (trainInstances.numInstances() * sampleSizePercentage);
        while (originalSampledTrainInstances.size() < sampleSize &&
            (trainContract < 0 ||
                trainTime < trainContract)) {
            if(!sampler.hasNext()) {
                throw new IllegalStateException("Cannot sample another instance, this should never happen!");
            }
            Instance sampledInstance = sampler.next();
            originalSampledTrainInstances.add(sampledInstance);
            if (cvTrain) {
                for(NearestNeighbourFinder nearestNeighbourFinder : trainNearestNeighbourFinders) {
                    if(!nearestNeighbourFinder.getInstance().equals(sampledInstance)) {
                        nearestNeighbourFinder.addNeighbour(sampledInstance);
                    }
                }
            }
            regenerateTrainResults = true;
            trainCheckpoint();
        }
        if(regenerateTrainResults) {
            regenerateTrainResults = false;
            long timeStamp = System.nanoTime();
            trainTime += timeStamp - trainTimeStamp;
            trainTimeStamp = timeStamp;
            if(cvTrain) {
                trainResults = new double[trainNearestNeighbourFinders.size()][];
                for(int i = 0; i < trainResults.length; i++) {
                    trainResults[i] = trainNearestNeighbourFinders.get(i).predict();
                }
            }
            timeStamp = System.nanoTime();
            trainTime += timeStamp - trainTimeStamp;
            trainTimeStamp = timeStamp;
            checkpoint(true);
        }
    }

    private int getSampleSize() {
        return (int) (sampleSizePercentage * originalTrainInstances.numInstances());
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
    public double classifyInstance(final Instance testInstance) throws Exception {
        return Utilities.argMax(distributionForInstance(testInstance), random);
    }

    private long predictionTime;

    private long getPredictionTime() {
        return predictionTime;
    }

    @Override
    public double[] distributionForInstance(final Instance testInstance) throws Exception {
        long timeStamp = System.nanoTime();
        predictionTime = 0;
        NearestNeighbourFinder nearestNeighbourFinder = new NearestNeighbourFinder(testInstance);
        List<Instance> sampledTrainInstances = new ArrayList<>(originalSampledTrainInstances);
        while (!sampledTrainInstances.isEmpty() &&
            (predictionContract < 0 ||
                predictionTime < predictionContract)) {
            Instance sampledTrainInstance = sampledTrainInstances.remove(random.nextInt(sampledTrainInstances.size()));
            nearestNeighbourFinder.addNeighbour(sampledTrainInstance);
            long currentTimeStamp = System.nanoTime();
            predictionTime += currentTimeStamp - timeStamp;
            timeStamp = currentTimeStamp;
        }
        double[] prediction = nearestNeighbourFinder.predict();
        Utilities.normalise(prediction);
        return prediction;
    }

    public ClassifierResults getTestResults(Instances testInstances) throws Exception {
        testTimeStamp = System.nanoTime();
        if(resetTest) {
            resetTest = false;
            testTime = 0;
            sampledTrainInstances.clear();
            testNearestNeighbourFinders.clear();
            sampledTrainInstances.addAll(originalSampledTrainInstances);
            for(Instance testInstance : testInstances) {
                testNearestNeighbourFinders.add(new NearestNeighbourFinder(testInstance));
            }
            testCheckpoint();
        }
        while (!sampledTrainInstances.isEmpty() &&
            (testContract < 0 ||
                testTime < testContract)) {
            regenerateTestResults = true;
            Instance sampledTrainInstance = sampledTrainInstances.remove(random.nextInt(sampledTrainInstances.size()));
            for(NearestNeighbourFinder nearestNeighbourFinder : testNearestNeighbourFinders) {
                nearestNeighbourFinder.addNeighbour(sampledTrainInstance);
            }
            testCheckpoint();
        }
        if(regenerateTestResults) {
            regenerateTestResults = false;
            testResults = new double[testNearestNeighbourFinders.size()][];
            for(int i = 0; i < testResults.length; i++) {
                testResults[i] = testNearestNeighbourFinders.get(i).predict();
            }
        }
        checkpoint(true);
        return getResults(testInstances, testResults);
    }

    private double[][] testResults;

    private void resumeFromCheckpoint() throws Exception {
        if(isCheckpointing()) {
            try {
                loadFromFile(checkpointFilePath);
            } catch (FileNotFoundException e) {

            }
        }
    }

    private void checkpoint() throws IOException {
        checkpoint(false);
    }

    private void checkpoint(boolean force) throws IOException {
        if(isCheckpointing() && (force || System.nanoTime() - lastCheckpointTimeStamp > minCheckpointInterval)) {
            saveToFile(checkpointFilePath);
            lastCheckpointTimeStamp = System.nanoTime();
        }
    }

    public boolean isCheckpointing() {
        return checkpointing;
    }

    public void setCheckpointing(boolean on) {
        checkpointing = on;
    }

    private void testCheckpoint() throws IOException {
        testCheckpoint(false);
    }

    private void testCheckpoint(boolean force) throws IOException {
        testTime += System.nanoTime() - testTimeStamp;
        checkpoint(force);
        testTimeStamp = System.nanoTime();
    }

    private void trainCheckpoint(boolean force) throws IOException {
        trainTime += System.nanoTime() - trainTimeStamp;
        checkpoint(force);
        trainTimeStamp = System.nanoTime();
    }

    private void trainCheckpoint() throws IOException {
        trainCheckpoint(false);
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

    public NeighbourWeighter getNeighbourWeighter() {
        return neighbourWeighter;
    }

    public void setNeighbourWeighter(final NeighbourWeighter neighbourWeighter) {
        this.neighbourWeighter = neighbourWeighter;
        reset();
    }

    @Override
    public void setTimeLimit(final long nanoseconds) { // todo split to train time limit and test time limit
        trainContract = nanoseconds;
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
