package timeseriesweka.classifiers.AdvancedAbstractClassifier;

import evaluation.storage.ClassifierResults;
import net.sourceforge.sizeof.SizeOf;
import timeseriesweka.classifiers.CheckpointClassifier;
import timeseriesweka.classifiers.ContractClassifier;
import timeseriesweka.classifiers.SaveParameterInfo;
import utilities.OptionsSetter;
import utilities.Reproducible;
import utilities.TrainAccuracyEstimate;
import utilities.Utilities;
import weka.classifiers.AbstractClassifier;
import weka.core.Instances;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class AdvancedAbstractClassifier extends AbstractClassifier implements Serializable, Reproducible, SaveParameterInfo, CheckpointClassifier, ContractClassifier, OptionsSetter, TrainAccuracyEstimate {

    public static final String CV_TRAIN_KEY = "c";
    public static final String PREDICTION_CONTRACT_KEY = "pc";
    public static final String TRAIN_CONTRACT_KEY = "trc";
    public static final String TEST_CONTRACT_KEY = "tec";
    public static final String SEED_KEY = "se";
    public static final String TRAIN_TIME_KEY = "trt";
    public static final String TEST_TIME_KEY = "tet";
    public static final String PREDICTION_TIME_KEY = "pt";
    protected boolean hasResumedFromCheckpoint = false;
    protected long testTime;
    protected long trainTime;
    protected String savePath;
    protected long testTimeStamp;
    protected long trainTimeStamp;
    protected long predictionTime;
    protected boolean cvTrain = false;
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
    }

    public void reset() {
        trainTime = -1;
        if(seed != null) {
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
    public String getParameters() {
        StringBuilder stringBuilder = new StringBuilder();
        if(cvTrain) {
            stringBuilder.append(CV_TRAIN_KEY);
            stringBuilder.append(",");
        }
        stringBuilder.append(",");
        stringBuilder.append(PREDICTION_CONTRACT_KEY);
        stringBuilder.append(",");
        stringBuilder.append(predictionContract);
        stringBuilder.append(",");
        stringBuilder.append(TRAIN_CONTRACT_KEY);
        stringBuilder.append(",");
        stringBuilder.append(trainContract);
        stringBuilder.append(",");
        stringBuilder.append(TEST_CONTRACT_KEY);
        stringBuilder.append(",");
        stringBuilder.append(testContract);
        stringBuilder.append(",");
        stringBuilder.append(SEED_KEY);
        stringBuilder.append(",");
        stringBuilder.append(seed);
        stringBuilder.append(",");
        stringBuilder.append(PREDICTION_TIME_KEY);
        stringBuilder.append(",");
        stringBuilder.append(predictionTime);
        stringBuilder.append(",");
        stringBuilder.append(TRAIN_TIME_KEY);
        stringBuilder.append(",");
        stringBuilder.append(trainTime);
        stringBuilder.append(",");
        stringBuilder.append(TEST_TIME_KEY);
        stringBuilder.append(",");
        stringBuilder.append(testTime);
        return stringBuilder.toString();
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

    @Override
    public void writeCVTrainToFile(final String train) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassifierResults getTrainResults() {
        return trainResults;
    }

    protected void resumeFromCheckpoint() throws Exception {
        if(isCheckpointing() && !hasResumedFromCheckpoint) {
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
        if(isCheckpointing() && (force || System.nanoTime() - lastCheckpointTimeStamp > minCheckpointInterval)) {
            saveToFile(checkpointFilePath);
            lastCheckpointTimeStamp = System.nanoTime();
        }
    }

    public boolean setOption(String key, String value) {
        reset();
        throw new UnsupportedOperationException(); // todo
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
