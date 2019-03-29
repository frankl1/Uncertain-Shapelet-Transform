package timeseriesweka.classifiers.AdvancedAbstractClassifier;

import evaluation.storage.ClassifierResults;
import net.sourceforge.sizeof.SizeOf;
import timeseriesweka.classifiers.*;
import utilities.OptionsSetter;
import utilities.Reproducible;
import utilities.TrainAccuracyEstimate;
import utilities.Utilities;
import weka.classifiers.AbstractClassifier;
import weka.core.Instances;

import java.io.*;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class AdvancedAbstractClassifier extends AbstractClassifier implements Serializable, Reproducible, SaveParameterInfoOptions, CheckpointClassifier, ContractClassifier, OptionsSetter, TrainAccuracyEstimate {

    @Override
    public void setOptions(final String[] options) throws Exception {
        OptionsSetter.setOptions(this, options);
    }

    public static final String CV_TRAIN_KEY = "cvTrain";
    public static final String PREDICTION_CONTRACT_KEY = "predictionContract";
    public static final String TRAIN_CONTRACT_KEY = "trainContract";
    public static final String TEST_CONTRACT_KEY = "testContract";
//    public static final String SEED_KEY = "seed";
    //    public static final String TRAIN_TIME_KEY = "trainTime";
//    public static final String TEST_TIME_KEY = "testTime";
//    public static final String PREDICTION_TIME_KEY = "predictionTime";
    protected boolean hasResumedFromCheckpoint = false;
    protected long testTime;
    protected long trainTime;
    protected String savePath;
    protected long testTimeStamp;
    protected long trainTimeStamp;
    protected long predictionTime;
    protected boolean cvTrain = true;
    protected String checkpointFilePath;
    protected long predictionContract = -1;
    protected Random random = new Random();
    protected long trainContract = -1;
    protected long testContract = -1;
    protected boolean checkpointing = false;
    protected long lastCheckpointTimeStamp = 0;
    protected long minCheckpointInterval = TimeUnit.NANOSECONDS.convert(10, TimeUnit.MINUTES);
    protected Long seed = null;
    protected boolean resetTrain = true;
    protected boolean resetTest = true;
    protected ClassifierResults trainResults;
    protected ClassifierResults testResults;
    protected Instances originalTrainInstances = null;
    protected Instances originalTestInstances;

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

    public String getSavePath() {
        return savePath;
    }

    @Override
    public void setSavePath(String path) {
        File file = new File(path);
        Utilities.mkdir(file);
        this.savePath = path;
    }

    @Override
    public void copyFromSerObject(final Object obj) throws Exception {
        reset();
        AdvancedAbstractClassifier other = (AdvancedAbstractClassifier) obj;
        originalTestInstances = other.originalTestInstances;
        originalTrainInstances = other.originalTrainInstances;
        cvTrain = other.cvTrain;
        trainTime = other.trainTime;
        testTime = other.testTime;
        random = other.random;
        trainContract = other.trainContract;
        testContract = other.testContract;
        predictionContract = other.predictionContract;
        seed = other.seed;
        trainResults = other.trainResults;
        resetTrain = other.resetTrain;
        resetTest = other.resetTest;
        testResults = other.testResults;
        trainFilePath = other.trainFilePath; // todo should this be here?
    }

    public void reset() {
        trainTime = -1;
        if (seed != null) {
            random.setSeed(seed);
        }
        trainResults = null;
        resetTrain = true;
        hasResumedFromCheckpoint = false;
        resetTest();
    }

    public void resetTest() {
        resetTest = true;
        testResults = null;
        testTime = -1;
    }

    public void setSeed(long seed) {
        this.seed = seed;
        reset();
    }

    public void setRandom(Random random) {
        this.random = random;
        reset();
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


    @Override
    public String[] getOptions() {
        return new String[]{
            CV_TRAIN_KEY, String.valueOf(cvTrain),
            PREDICTION_CONTRACT_KEY, String.valueOf(predictionContract),
            TRAIN_CONTRACT_KEY, String.valueOf(trainContract),
            TEST_CONTRACT_KEY, String.valueOf(testContract),
//            SEED_KEY, String.valueOf(seed),
//            PREDICTION_TIME_KEY, String.valueOf(predictionTime),
//            TRAIN_TIME_KEY, String.valueOf(trainTime),
//            TEST_TIME_KEY, String.valueOf(testTime) // only need these if experiments isn't recording this
        };
    }

    public boolean setOption(String key, String value) {
        if (key.equalsIgnoreCase(CV_TRAIN_KEY)) {
            setCvTrain(Boolean.parseBoolean(value));
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

    public long getTrainTime() {
        return trainTime;
    }

    public long getTestTime() {
        return testTime;
    }

    @Override
    public void setTimeLimit(final long nanoseconds) { // todo split to train time limit and test time limit
        trainContract = nanoseconds;
    }

    public boolean isCvTrain() {
        return cvTrain;
    }

    public void setCvTrain(final boolean cvTrain) {
        this.cvTrain = cvTrain;
        reset();
    }

    private long getPredictionTime() {
        return predictionTime;
    }

    @Override
    public void setFindTrainAccuracyEstimate(final boolean setCV) {
        setCvTrain(setCV);
    }

    @Override
    public boolean findsTrainAccuracyEstimate() {
        return cvTrain;
    }

    protected String trainFilePath;

    @Override
    public void writeCVTrainToFile(final String train) {
        trainFilePath = train;
    }

    @Override
    public ClassifierResults getTrainResults() {
        return trainResults;
    }

    protected void resumeFromCheckpoint() throws Exception {
        if (isCheckpointing() && !hasResumedFromCheckpoint) {
            try {
                loadFromFile(checkpointFilePath);
                hasResumedFromCheckpoint = true;
            } catch (FileNotFoundException e) {

            }
        }
    }

    protected void updateTrainTime() {
        long timeStamp = System.nanoTime();
        trainTime += timeStamp - trainTimeStamp;
        trainTimeStamp = timeStamp;
    }

    protected void updateTestTime() {
        long timeStamp = System.nanoTime();
        testTime += timeStamp - testTimeStamp;
        testTimeStamp = timeStamp;
    }

    protected void updatePredictionTime() {
        long timeStamp = System.nanoTime();
        predictionTime += timeStamp - predictionTimeStamp;
        predictionTimeStamp = timeStamp;
    }

    protected long predictionTimeStamp;

    protected boolean withinTrainContract() {
        return (trainContract < 0 || trainTime < trainContract);
    }

    protected boolean withinTestContract() {
        return (testContract < 0 || testTime < testContract);
    }

    protected boolean withinPredictionContract() {
        return (predictionContract < 0 || predictionTime < predictionContract);
    }

    public boolean isCheckpointing() {
        return checkpointing;
    }

    public void setCheckpointing(boolean on) {
        checkpointing = on;
    }

    protected void checkpoint() throws IOException {
        checkpoint(false);
    }

    protected void checkpoint(boolean force) throws IOException {
        if (isCheckpointing() && (force || System.nanoTime() - lastCheckpointTimeStamp > minCheckpointInterval)) {
            saveToFile(checkpointFilePath);
            lastCheckpointTimeStamp = System.nanoTime();
        }
    }


    protected void testCheckpoint() throws IOException {
        testCheckpoint(false);
    }

    protected void testCheckpoint(boolean force) throws IOException {
        updateTestTime();
        checkpoint(force);
        testTimeStamp = System.nanoTime();
    }

    protected void trainCheckpoint() throws IOException {
        trainCheckpoint(false);
    }

    protected void trainCheckpoint(boolean force) throws IOException {
        updateTrainTime();
        checkpoint(force);
        trainTimeStamp = System.nanoTime();
    }

}
