package timeseriesweka.classifiers.nn.Tuning;

import evaluation.storage.ClassifierResults;
import timeseriesweka.classifiers.AdvancedAbstractClassifier.AdvancedAbstractClassifier;
import timeseriesweka.classifiers.AdvancedAbstractClassifier.AdvancedAbstractClassifierInterface;
import timeseriesweka.classifiers.ParameterSplittable;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Random;
import java.util.function.Function;

public abstract class AbstractTuned implements AdvancedAbstractClassifierInterface, ParameterSplittable {
    private int parameterId = -1;
    private PermutationBuilder permutationBuilder = null;
    private AdvancedAbstractClassifier classifier;
    private Random random = new Random();
    private Long seed;

    @Override
    public void buildClassifier(final Instances trainInstances) throws Exception {
        if(parameterId < 0) {
            postProcess();
        } else {
            setPermutationBuilder(trainInstances);
        }
        buildPermutation();
        classifier.buildClassifier(trainInstances);
    }

    private long parseFold(String path) {
        String str = new File(path).getName();
        String substring = str.substring("fold".length());
        String foldString = substring.substring(0, substring.indexOf("_"));
        return Long.parseLong(foldString);
    }

    private int parseParamIndex(String path) {
        String str = new File(path).getName();
        String substring = str.substring(str.indexOf("_"), str.indexOf("."));
        return Integer.parseInt(substring);
    }

    private void postProcess() throws Exception {
        long currentFold = parseFold(trainPath); // todo change to use seed
        File[] permutations = new File(trainPath).listFiles(file -> {
            if(!file.isFile()) {
                return false;
            }
            return parseFold(file.getPath()) == currentFold;
        });
        if(permutations == null || permutations.length <= 0) {
            throw new IllegalArgumentException("No files found");
        }
        ClassifierResults bestResults = new ClassifierResults();
        File bestFile = permutations[0];
        bestResults.loadResultsFromFile(permutations[0].getPath());
        for(int i = 1; i < permutations.length; i++) {
            ClassifierResults other = new ClassifierResults();
            other.loadResultsFromFile(permutations[i].getPath());
            if(permutationComparator.compare(bestResults, other) > 0) {
                bestResults = other;
                bestFile = permutations[i];
            }
        }
        classifier = permutationBuilder.build();
        classifier.setOptions(bestResults.getParas().split(","));
    }

    public Comparator<ClassifierResults> getPermutationComparator() {
        return permutationComparator;
    }

    public void setPermutationComparator(final Comparator<ClassifierResults> permutationComparator) {
        this.permutationComparator = permutationComparator;
    }

    private Comparator<ClassifierResults> permutationComparator = (results, t1) -> {
        results.findAllStats();
        t1.findAllStats();
        return Double.compare(results.getAcc(), t1.getAcc());
    };

    private void setPermutationBuilder(Instances trainInstances) {
        if(permutationBuilder == null) {
            permutationBuilder = getPermutationBuilderFunction().apply(trainInstances);
        }
    }

    private void buildPermutation() {
        permutationBuilder.setParameterPermutation(parameterId);
        classifier = permutationBuilder.build();
    }

    protected abstract Function<Instances, PermutationBuilder> getPermutationBuilderFunction();

    @Override
    public double classifyInstance(final Instance testInstance) throws Exception {
        return classifier.classifyInstance(testInstance);
    }

    @Override
    public double[] distributionForInstance(final Instance testInstance) throws Exception {
        return classifier.distributionForInstance(testInstance);
    }

    @Override
    public Capabilities getCapabilities() {
        return classifier.getCapabilities();
    }

    @Override
    public void setParamSearch(final boolean b) {
        if(!b) {
            parameterId = -1;
        }
    }

    @Override
    public void setParametersFromIndex(final int x) {
        parameterId = x;
    }

    @Override
    public String getParas() {
        return getParameters();
    }

    @Override
    public double getAcc() {
        return 0;
    }

    public int size() {
        if(permutationBuilder == null) {
            return 1;
        }
        return permutationBuilder.size();
    }

    @Override
    public long getMinCheckpointInterval() {
        return classifier.getMinCheckpointInterval();
    }

    @Override
    public void setMinCheckpointInterval(final long nanoseconds) {
        classifier.setMinCheckpointInterval(nanoseconds);
    }

    @Override
    public long getPredictionContract() {
        return classifier.getPredictionContract();
    }

    @Override
    public void setPredictionContract(final long predictionContract) {
        classifier.setPredictionContract(predictionContract);
    }

    @Override
    public long getTrainContract() {
        return classifier.getTrainContract();
    }

    @Override
    public void setTrainContract(final long trainContract) {
        classifier.setTrainContract(trainContract);
    }

    @Override
    public long getTestContract() {
        return classifier.getTestContract();
    }

    @Override
    public void setTestContract(final long testContract) {
        classifier.setTestContract(testContract);
    }

    @Override
    public String getSavePath() {
        return classifier.getSavePath();
    }

    @Override
    public void setSavePath(final String path) {
        classifier.setSavePath(path);
    }

    @Override
    public void copyFromSerObject(final Object obj) throws Exception {
        classifier.copyFromSerObject(obj);
    }

    @Override
    public void reset() {
        classifier.reset();
        if(seed != null) {
            random.setSeed(seed);
        }
    }

    @Override
    public void resetTest() {
        classifier.resetTest();
    }

    @Override
    public void setSeed(final long seed) {
        classifier.setSeed(seed);
        this.seed = seed;
        reset();
    }

    @Override
    public void setRandom(final Random random) {
        classifier.setRandom(random);
        this.random = random;
        reset();
    }

    @Override
    public String getParameters() {
        return classifier.getParameters();
    }

    @Override
    public long getTrainTime() {
        return classifier.getTrainTime();
    }

    @Override
    public long getTestTime() {
        return classifier.getTestTime();
    }

    @Override
    public void setTimeLimit(final long nanoseconds) {
        classifier.setTimeLimit(nanoseconds);
    }

    @Override
    public boolean isCvTrain() {
        return classifier.isCvTrain();
    }

    @Override
    public void setCvTrain(final boolean cvTrain) {
        classifier.setCvTrain(cvTrain);
    }

    @Override
    public void setFindTrainAccuracyEstimate(final boolean setCV) {
        classifier.setFindTrainAccuracyEstimate(setCV);
    }

    @Override
    public boolean findsTrainAccuracyEstimate() {
        return classifier.findsTrainAccuracyEstimate();
    }

    @Override
    public void writeCVTrainToFile(final String train) {
        classifier.writeCVTrainToFile(train);
        trainPath = train;
    }

    private String trainPath;

    @Override
    public ClassifierResults getTrainResults() {
        return classifier.getTrainResults();
    }

    @Override
    public boolean isCheckpointing() {
        return classifier.isCheckpointing();
    }

    @Override
    public void setCheckpointing(final boolean on) {
        classifier.setCheckpointing(on);
    }

    @Override
    public boolean setOption(final String key, final String value) {
        return classifier.setOption(key, value);
    }

    @Override
    public void saveToFile(final String filename) throws IOException {
        classifier.saveToFile(filename);
    }

    @Override
    public void loadFromFile(final String filename) throws Exception {
        classifier.loadFromFile(filename);
    }

    @Override
    public void setOneDayLimit() {
        classifier.setOneDayLimit();
    }

    @Override
    public void setOneHourLimit() {
        classifier.setOneHourLimit();
    }

    @Override
    public void setOneMinuteLimit() {
        classifier.setOneMinuteLimit();
    }

    @Override
    public void setDayLimit(final int t) {
        classifier.setDayLimit(t);
    }

    @Override
    public void setHourLimit(final int t) {
        classifier.setHourLimit(t);
    }

    @Override
    public void setMinuteLimit(final int t) {
        classifier.setMinuteLimit(t);
    }

    @Override
    public void setTimeLimit(final TimeLimit time, final int amount) {
        classifier.setTimeLimit(time, amount);
    }

    @Override
    public int setNumberOfFolds(final Instances data) {
        return classifier.setNumberOfFolds(data);
    }

    @Override
    public Enumeration listOptions() {
        return classifier.listOptions();
    }

    @Override
    public String[] getOptions() {
        return classifier.getOptions();
    }

    @Override
    public void setOptions(final String[] options) throws Exception {
        classifier.setOptions(options);
    }
}
