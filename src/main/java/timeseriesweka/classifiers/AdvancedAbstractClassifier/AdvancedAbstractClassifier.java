package timeseriesweka.classifiers.AdvancedAbstractClassifier;

import evaluation.storage.ClassifierResults;
import net.sourceforge.sizeof.SizeOf;
import utilities.OptionsSetter;
import utilities.Utilities;
import weka.classifiers.AbstractClassifier;
import weka.core.Instances;

import java.io.*;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public abstract class AdvancedAbstractClassifier extends AbstractClassifier implements AdvancedAbstractClassifierInterface {

    public static final String CV_TRAIN_KEY = "estimateTrain";
    public static final String PREDICTION_CONTRACT_KEY = "predictionContract";
    public static final String TRAIN_CONTRACT_KEY = "trainContract";
    public static final String TEST_CONTRACT_KEY = "testContract";    @Override
    public Logger getLogger() {
        return logger;
    }
    private static final String TRAIN_CHECKPOINT_FILENAME = "checkpoint";
    private static final String TEST_CHECKPOINT_FILENAME = "checkpoint";
    protected Logger logger = Logger.getLogger(getClass().getCanonicalName());@Override
    public void setLogger(final Logger logger) {
        this.logger = logger;
    }
    //    public static final String SEED_KEY = "seed";
    //    public static final String TRAIN_TIME_KEY = "trainTime";
//    public static final String TEST_TIME_KEY = "testTime";
//    public static final String PREDICTION_TIME_KEY = "predictionTime";
    private boolean hasResumedFromTrainCheckpoint = false;
    private boolean hasResumedFromTestCheckpoint = false;
    private long testTime;
    @Override
    public void setOptions(final String[] options) throws Exception {
        OptionsSetter.setOptions(this, options);
    }
    private long trainTime;
    private String trainCheckpointDirPath;
    private String testCheckpointDirPath;
    @Override
    public int getSeed() {
        return seed;
    }
    private long testTimeStamp;
    private long trainTimeStamp;
    @Override
    public void setSeed(final int seed) {
        this.seed = seed;
        setTrainRandom(new Random(seed));
        setTestRandom(new Random(seed));
    }
    private long predictionTime;
    private boolean estimateTrain = true;
    private String testCheckpointFilePath;
    private String trainCheckpointFilePath;
    private long predictionContract = -1;
    private int seed = 0;
    private Random trainRandom = new Random(seed);
    private Random testRandom = new Random(seed);
    private long trainContract = -1;
    private long testContract = -1;
    private boolean trainCheckpointing = false;
    private boolean testCheckpointing = false;
    private long lastCheckpointTimeStamp = 0;
    private long minCheckpointInterval = TimeUnit.NANOSECONDS.convert(10, TimeUnit.MINUTES);
    private ClassifierResults trainResults;
    private ClassifierResults testResults;
    private Instances originalTrainInstances = null;
    private Instances originalTestInstances;
    private boolean resetOnTrain = true;
    private boolean resetOnTest = true;
    private String trainEstimateFilePath;
    private long predictionTimeStamp;
    private boolean writeTrainEstimate = false;

    protected long getTestTimeStamp() {
        return testTimeStamp;
    }

    protected void setTestTimeStamp(final long testTimeStamp) {
        this.testTimeStamp = testTimeStamp;
    }

    protected long getTrainTimeStamp() {
        return trainTimeStamp;
    }

    protected void setTrainTimeStamp(final long trainTimeStamp) {
        this.trainTimeStamp = trainTimeStamp;
    }

    public String getTestCheckpointFilePath() {
        return testCheckpointFilePath;
    }

    public void setTestCheckpointFilePath(final String testCheckpointFilePath) {
        this.testCheckpointFilePath = testCheckpointFilePath;
    }

    protected long getLastCheckpointTimeStamp() {
        return lastCheckpointTimeStamp;
    }

    protected void setLastCheckpointTimeStamp(final long lastCheckpointTimeStamp) {
        this.lastCheckpointTimeStamp = lastCheckpointTimeStamp;
    }

    public ClassifierResults getTestResults() {
        return testResults;
    }

    protected void setTestResults(final ClassifierResults testResults) {
        this.testResults = testResults;
    }

    protected Instances getOriginalTrainInstances() {
        return originalTrainInstances;
    }

    protected void setOriginalTrainInstances(final Instances originalTrainInstances) {
        this.originalTrainInstances = originalTrainInstances;
    }

    protected Instances getOriginalTestInstances() {
        return originalTestInstances;
    }

    protected void setOriginalTestInstances(final Instances originalTestInstances) {
        this.originalTestInstances = originalTestInstances;
    }


    public void setResetOnTrain(final boolean resetOnTrain) {
        this.resetOnTrain = resetOnTrain;
    }

    public boolean resetsOnTrain() {
        return resetOnTrain;
    }

    public boolean resetsOnTest() {
        return resetOnTest;
    }


    public void setResetOnTest(final boolean resetOnTest) {
        this.resetOnTest = resetOnTest;
    }

    public long getTrainTime() {
        return trainTime;
    }

    protected void setTrainTime(final long trainTime) {
        this.trainTime = trainTime;
    }

    public long getTestTime() {
        return testTime;
    }

    protected void setTestTime(final long testTime) {
        this.testTime = testTime;
    }

    @Override
    public void setTimeLimit(final long nanoseconds) { // todo split to train time limit and test time limit
        trainContract = nanoseconds;
    }

    public boolean isEstimateTrain() {
        return estimateTrain;
    }

public void setEstimateTrain(final boolean estimateTrain) {
        this.estimateTrain = estimateTrain;
    }

    @Override
    public void setFindTrainAccuracyEstimate(final boolean setCV) {
        setEstimateTrain(setCV);
    }

    @Override
    public boolean findsTrainAccuracyEstimate() {
        return estimateTrain;
    }

    @Override
    public void writeCVTrainToFile(final String train) {
        trainEstimateFilePath = train;
    }

    @Override
    public ClassifierResults getTrainResults() {
        return trainResults;
    }

    protected void setTrainResults(final ClassifierResults trainResults) {
        this.trainResults = trainResults;
    }

    public void setTrainRandom(final Random trainRandom) {
        this.trainRandom = trainRandom;
    }

    public boolean isTrainCheckpointing() {
        return trainCheckpointing;
    }

    public Random getTrainRandom() {
        return trainRandom;
    }

    @Override
    public void setSavePath(final String path) {
        setTrainCheckpointDirPath(path);
    }

    public void setTrainCheckpointing(boolean on) {
        trainCheckpointing = on;
    }

    public boolean setOption(String key, String value) {
        if (key.equalsIgnoreCase(CV_TRAIN_KEY)) {
            setEstimateTrain(Boolean.parseBoolean(value));
        } else if (key.equalsIgnoreCase(PREDICTION_CONTRACT_KEY)) {
            setPredictionContract(Long.parseLong(value));
        } else if (key.equalsIgnoreCase(TRAIN_CONTRACT_KEY)) {
            setTrainContract(Long.parseLong(value));
        } else if (key.equalsIgnoreCase(TEST_CONTRACT_KEY)) {
            setTestContract(Long.parseLong(value));
//        } else if(key.equalsIgnoreCase(SEED_KEY)) { // todo use weka's randomize thing
//            if(value == null) {
//                setSeed(null);
//            } else {
//                setSeed(Long.parseLong(value));
//            }
        } else {
            return false;
        }
        return true;
    }

    public String getTrainEstimateFilePath() {
        return trainEstimateFilePath;
    }

    public void setTrainEstimateFilePath(final String trainEstimateFilePath) {
        this.trainEstimateFilePath = trainEstimateFilePath;
    }

    protected long getPredictionTimeStamp() {
        return predictionTimeStamp;
    }

    protected void setPredictionTimeStamp(final long predictionTimeStamp) {
        this.predictionTimeStamp = predictionTimeStamp;
    }

    protected void resetTest() throws Exception {
        setTestTime(0);
        testRandom.setSeed(0);
        setLastCheckpointTimeStamp(0);
        setOriginalTestInstances(null);
        setTestTimeStamp(System.nanoTime());
    }

    public long getMinCheckpointInterval() {
        return minCheckpointInterval;
    }

    public void setMinCheckpointInterval(long nanoseconds) {
        minCheckpointInterval = nanoseconds;
    }

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

    public String getTrainCheckpointDirPath() {
        return trainCheckpointDirPath;
    }

    @Override
    public void setTrainCheckpointDirPath(String path) {
        File file = new File(path);
        Utilities.mkdir(file);
        this.trainCheckpointDirPath = path;
        this.trainCheckpointFilePath = new File(file, TRAIN_CHECKPOINT_FILENAME).getPath();
    }


    @Override
    public void copyFromSerObject(final Object obj) throws Exception {
        AdvancedAbstractClassifier other = (AdvancedAbstractClassifier) obj;
        originalTestInstances = other.originalTestInstances;
        originalTrainInstances = other.originalTrainInstances;
        estimateTrain = other.estimateTrain;
        trainTime = other.trainTime;
        testTime = other.testTime;
        trainRandom = other.trainRandom;
        testRandom = other.testRandom;
        trainContract = other.trainContract;
        testContract = other.testContract;
        predictionContract = other.predictionContract;
        seed = other.seed;
        trainResults = other.trainResults;
        testResults = other.testResults;
        resetOnTest = other.resetOnTest;
        resetOnTrain = other.resetOnTrain;
        trainEstimateFilePath = other.trainEstimateFilePath; // todo should this be here?
        setSeed(seed);
    }

    protected ClassifierResults setResultsMetaData(int numClasses, ClassifierResults results) throws Exception {
        results.setNumClasses(numClasses);
        try {
            results.setMemory(SizeOf.deepSizeOf(this));
        } catch (Exception e) {

        }
        results.setClassifierName(toString());
        results.setParas(getParameters());
        results.setTimeUnit(TimeUnit.NANOSECONDS);
        results.setBuildTime(getTrainTime());
        results.setTestTime(getTestTime());
        results.findAllStatsOnce();
        return results;
    }

        public long getPredictionTime() {
        return predictionTime;
    }
    public void setPredictionTime(final long predictionTime) {
        this.predictionTime = predictionTime;
    }@Override
    public String[] getOptions() {
        return new String[]{
            CV_TRAIN_KEY, String.valueOf(estimateTrain),
            PREDICTION_CONTRACT_KEY, String.valueOf(predictionContract),
            TRAIN_CONTRACT_KEY, String.valueOf(trainContract),
            TEST_CONTRACT_KEY, String.valueOf(testContract),
//            SEED_KEY, String.valueOf(seed),
//            PREDICTION_TIME_KEY, String.valueOf(predictionTime),
//            TRAIN_TIME_KEY, String.valueOf(trainTime),
//            TEST_TIME_KEY, String.valueOf(testTime) // only need these if experiments isn't recording this
        };
    }

    protected void resumeFromTrainCheckpoint() throws Exception {
        if (isTrainCheckpointing() && !hasResumedFromTrainCheckpoint) {
            File checkpointFile = new File(trainCheckpointFilePath);
            if(checkpointFile.exists()) {
                loadFromFile(trainCheckpointFilePath);
                hasResumedFromTrainCheckpoint = true;
                getLogger().info("resumed from train checkpoint");
            } else {
                getLogger().info("no train checkpoint file to resume from");
            }
        }
    }

    protected void resumeFromTestCheckpoint() throws Exception {
        if (isTestCheckpointing() && !hasResumedFromTestCheckpoint) {
            File checkpointFile = new File(testCheckpointFilePath);
            if(checkpointFile.exists()) {
                loadFromFile(testCheckpointFilePath);
                hasResumedFromTestCheckpoint = true;
                getLogger().info("resumed from test checkpoint");
            } else {
                getLogger().info("no test checkpoint file to resume from");
            }
        }
    }

    protected void updatePredictionTime() {
        long timeStamp = System.nanoTime();
        predictionTime += timeStamp - predictionTimeStamp;
        predictionTimeStamp = timeStamp;
    }

    protected boolean withinTrainContract() {
        return (trainContract < 0 || trainTime < trainContract);
    }

    protected boolean withinTestContract() {
        return (testContract < 0 || testTime < testContract);
    }

    protected boolean withinPredictionContract() {
        return (predictionContract < 0 || predictionTime < predictionContract);
    }

    protected void checkpoint(String filePath) throws IOException {
        checkpoint(filePath, false);
    }

    protected void checkpoint(String filePath, boolean force) throws IOException {
        if (isTrainCheckpointing() && (force || System.nanoTime() - lastCheckpointTimeStamp > minCheckpointInterval)) {
            saveToFile(filePath);
            lastCheckpointTimeStamp = System.nanoTime();
        }
    }

    protected void testCheckpoint() throws IOException {
        testCheckpoint(false);
    }

    protected void testCheckpoint(boolean force) throws IOException {
        updateTestTime();
        checkpoint(testCheckpointFilePath, force);
        testTimeStamp = System.nanoTime();
    }

    protected void updateTestTime() {
        long timeStamp = System.nanoTime();
        testTime += timeStamp - testTimeStamp;
        testTimeStamp = timeStamp;
    }

    protected void trainCheckpoint() throws IOException {
        trainCheckpoint(false);
    }

    protected void trainCheckpoint(boolean force) throws IOException {// todo should force overwrite if one already there / loaded?
        updateTrainTime();
        checkpoint(trainCheckpointFilePath, force);
        trainTimeStamp = System.nanoTime();
    }

    protected void updateTrainTime() {
        long timeStamp = System.nanoTime();
        trainTime += timeStamp - trainTimeStamp;
        trainTimeStamp = timeStamp;
    }

    public Random getTestRandom() {
        return testRandom;
    }

    public void setTestRandom(final Random testRandom) {
        this.testRandom = testRandom;
    }

    @Override
    public void buildClassifier(final Instances trainInstances) throws Exception {
        getCapabilities().testWithFail(trainInstances);
        resumeFromTrainCheckpoint();
        if(resetsOnTrain()) {
            resetTrain();
        }
    }

    protected void resetTrain() throws Exception {
        setTrainTime(0);
        trainRandom.setSeed(seed);
        setOriginalTrainInstances(null);
        setLastCheckpointTimeStamp(0);
        setTrainTimeStamp(System.nanoTime());
    }

    public boolean isWriteTrainEstimate() {
        return writeTrainEstimate;
    }

    public void setWriteTrainEstimate(final boolean writeTrainEstimate) {
        this.writeTrainEstimate = writeTrainEstimate;
    }

    protected void resetPrediction() {
        predictionTimeStamp = System.nanoTime();
        predictionTime = 0;
    }

    public String getTrainCheckpointFilePath() {
        return trainCheckpointFilePath;
    }

    public void setTrainCheckpointFilePath(final String trainCheckpointFilePath) {
        this.trainCheckpointFilePath = trainCheckpointFilePath;
    }

    protected long getRemainingTrainTime() {
        if(trainContract < 0) {
            return -1;
        }
        return Math.max(trainContract - trainTime, 0);
    }

    protected long getRemainingTestTime() {
        if(trainContract < 0) {
            return -1;
        }
        return Math.max(testContract - testTime, 0);
    }

    protected long getRemainingPredictionTime() {
        return Math.max(predictionContract - predictionTime, 0);
    }

    public boolean isTestCheckpointing() {
        return testCheckpointing;
    }

    public void setTestCheckpointing(final boolean testCheckpointing) {
        this.testCheckpointing = testCheckpointing;
    }

    public String getTestCheckpointDirPath() {
        return testCheckpointDirPath;
    }

    public void setTestCheckpointDirPath(final String path) {
        File file = new File(path);
        Utilities.mkdir(file);
        this.testCheckpointDirPath = path;
        this.testCheckpointFilePath = new File(file, TEST_CHECKPOINT_FILENAME).getPath();
    }
}
