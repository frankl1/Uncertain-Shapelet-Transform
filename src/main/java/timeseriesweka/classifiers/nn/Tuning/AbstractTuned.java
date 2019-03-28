package timeseriesweka.classifiers.nn.Tuning;

import evaluation.storage.ClassifierResults;
import timeseriesweka.classifiers.AdvancedAbstractClassifier.AdvancedAbstractClassifier;
import timeseriesweka.classifiers.AdvancedAbstractClassifier.AdvancedAbstractClassifierInterface;
import timeseriesweka.classifiers.ParameterSplittable;
import timeseriesweka.classifiers.nn.ParametersSpace;
import utilities.Utilities;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Random;

public abstract class AbstractTuned<A extends AdvancedAbstractClassifier> implements AdvancedAbstractClassifierInterface, ParameterSplittable {
    private A classifier;
    private Random random = new Random();
    private Long seed;
    private Comparator<ClassifierResults> parameterPermutationComparator = (results, t1) -> {
        results.findAllStats();
        t1.findAllStats();
        return Double.compare(results.getAcc(), t1.getAcc());
    };
    private String trainPath;
    private boolean postProcess = true;

    private A getClassifier() {
        if(classifier == null) {
            classifier = getClassifierInstance();
        }
        return classifier;
    }

    protected abstract A getClassifierInstance();

    public ParametersSpace getParametersSpace() {
        return parametersSpace;
    }

    public void setParametersSpace(final ParametersSpace parametersSpace) {
        this.parametersSpace = parametersSpace;
    }

    private ParametersSpace parametersSpace = new ParametersSpace();

    @Override
    public void buildClassifier(final Instances trainInstances) throws Exception {
        if(postProcess) {
            postProcess();
        }
        getClassifier().buildClassifier(trainInstances);
    }

    private void postProcess() throws Exception {
        File[] permutations = new File(trainPath).listFiles(file -> {
            if(!file.isFile()) {
                return false;
            }
            return parseFold(file.getPath()) == seed;
        });
        if(permutations == null || permutations.length <= 0) {
            throw new IllegalArgumentException("No files found");
        }
        ClassifierResults bestResults = Utilities.bestConvertion(Arrays.asList(permutations), Comparator.comparingDouble(ClassifierResults::getAcc), file -> {
            ClassifierResults classifierResults = new ClassifierResults();
            try {
                classifierResults.loadResultsFromFile(file.getPath());
                return classifierResults;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }, random);
//
//
//        ClassifierResults bestResults = new ClassifierResults();
//        File bestFile = permutations[0];
//        bestResults.loadResultsFromFile(permutations[0].getPath());
//        for(int i = 1; i < permutations.length; i++) { // todo more than one best perm? need to rand choose
//            ClassifierResults other = new ClassifierResults();
//            other.loadResultsFromFile(permutations[i].getPath());
//            if(parameterPermutationComparator.compare(bestResults, other) > 0) {
//                bestResults = other;
//                bestFile = permutations[i];
//            }
//        }
        String bestParameterPermutation = bestResults.getParas();
        getClassifier().setOptions(bestParameterPermutation.split(","));
    }

    private long parseFold(String path) {
        String str = new File(path).getName();
        String substring = str.substring("fold".length());
        String foldString = substring.substring(0, substring.indexOf("_"));
        return Long.parseLong(foldString);
    }

    @Override
    public double classifyInstance(final Instance testInstance) throws Exception {
        return getClassifier().classifyInstance(testInstance);
    }

    @Override
    public double[] distributionForInstance(final Instance testInstance) throws Exception {
        return getClassifier().distributionForInstance(testInstance);
    }

    @Override
    public Capabilities getCapabilities() {
        return getClassifier().getCapabilities();
    }

    private int parseParamIndex(String path) {
        String str = new File(path).getName();
        String substring = str.substring(str.indexOf("_"), str.indexOf("."));
        return Integer.parseInt(substring);
    }

    public Comparator<ClassifierResults> getParameterPermutationComparator() {
        return parameterPermutationComparator;
    }

    public void setParameterPermutationComparator(final Comparator<ClassifierResults> parameterPermutationComparator) {
        this.parameterPermutationComparator = parameterPermutationComparator;
    }

    @Override
    public void setParamSearch(final boolean b) {
        postProcess = !b;
    }

    @Override
    public void setParametersFromIndex(final int x) {
        if(x >= 0) {
            parametersSpace.setParameterPermutation(x);
        }
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
        return parametersSpace.size();
    }


    /**
     * TODO
     * Below just defers all funcs to the classifier itself. Not sure if this should be the default behaviour of a tuner?
     */

    @Override
    public long getMinCheckpointInterval() {
        return getClassifier().getMinCheckpointInterval();
    }

    @Override
    public void setMinCheckpointInterval(final long nanoseconds) {
        getClassifier().setMinCheckpointInterval(nanoseconds);
    }

    @Override
    public long getPredictionContract() {
        return getClassifier().getPredictionContract();
    }

    @Override
    public void setPredictionContract(final long predictionContract) {
        getClassifier().setPredictionContract(predictionContract);
    }

    @Override
    public long getTrainContract() {
        return getClassifier().getTrainContract();
    }

    @Override
    public void setTrainContract(final long trainContract) {
        getClassifier().setTrainContract(trainContract);
    }

    @Override
    public long getTestContract() {
        return getClassifier().getTestContract();
    }

    @Override
    public void setTestContract(final long testContract) {
        getClassifier().setTestContract(testContract);
    }

    @Override
    public String getSavePath() {
        return getClassifier().getSavePath();
    }

    @Override
    public void setSavePath(final String path) {
        getClassifier().setSavePath(path);
    }

    @Override
    public void copyFromSerObject(final Object obj) throws Exception {
        getClassifier().copyFromSerObject(obj);
    }

    @Override
    public void reset() {
        getClassifier().reset();
        if(seed != null) {
            random.setSeed(seed);
        }
    }

    @Override
    public void resetTest() {
        getClassifier().resetTest();
    }

    @Override
    public void setSeed(final long seed) {
        getClassifier().setSeed(seed);
        this.seed = seed;
        reset();
    }

    @Override
    public void setRandom(final Random random) {
        getClassifier().setRandom(random);
        this.random = random;
        reset();
    }

    @Override
    public String getParameters() {
        return getClassifier().getParameters();
    }

    @Override
    public long getTrainTime() {
        return getClassifier().getTrainTime();
    }

    @Override
    public long getTestTime() {
        return getClassifier().getTestTime();
    }

    @Override
    public void setTimeLimit(final long nanoseconds) {
        getClassifier().setTimeLimit(nanoseconds);
    }

    @Override
    public boolean isCvTrain() {
        return getClassifier().isCvTrain();
    }

    @Override
    public void setCvTrain(final boolean cvTrain) {
        getClassifier().setCvTrain(cvTrain);
    }

    @Override
    public void setFindTrainAccuracyEstimate(final boolean setCV) {
        getClassifier().setFindTrainAccuracyEstimate(setCV);
    }

    @Override
    public boolean findsTrainAccuracyEstimate() {
        return getClassifier().findsTrainAccuracyEstimate();
    }

    @Override
    public void writeCVTrainToFile(final String train) {
        getClassifier().writeCVTrainToFile(train);
        trainPath = train;
    }

    @Override
    public ClassifierResults getTrainResults() {
        return getClassifier().getTrainResults();
    }

    @Override
    public boolean isCheckpointing() {
        return getClassifier().isCheckpointing();
    }

    @Override
    public void setCheckpointing(final boolean on) {
        getClassifier().setCheckpointing(on);
    }

    @Override
    public boolean setOption(final String key, final String value) {
        return getClassifier().setOption(key, value);
    }

    @Override
    public void saveToFile(final String filename) throws IOException {
        getClassifier().saveToFile(filename);
    }

    @Override
    public void loadFromFile(final String filename) throws Exception {
        getClassifier().loadFromFile(filename);
    }

    @Override
    public void setOneDayLimit() {
        getClassifier().setOneDayLimit();
    }

    @Override
    public void setOneHourLimit() {
        getClassifier().setOneHourLimit();
    }

    @Override
    public void setOneMinuteLimit() {
        getClassifier().setOneMinuteLimit();
    }

    @Override
    public void setDayLimit(final int t) {
        getClassifier().setDayLimit(t);
    }

    @Override
    public void setHourLimit(final int t) {
        getClassifier().setHourLimit(t);
    }

    @Override
    public void setMinuteLimit(final int t) {
        getClassifier().setMinuteLimit(t);
    }

    @Override
    public void setTimeLimit(final TimeLimit time, final int amount) {
        getClassifier().setTimeLimit(time, amount);
    }

    @Override
    public int setNumberOfFolds(final Instances data) {
        return getClassifier().setNumberOfFolds(data);
    }

    @Override
    public Enumeration listOptions() {
        return getClassifier().listOptions();
    }

    @Override
    public String[] getOptions() {
        return getClassifier().getOptions();
    }

    @Override
    public void setOptions(final String[] options) throws Exception {
        getClassifier().setOptions(options);
    }
}
