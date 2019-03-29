package Tuning;

import evaluation.storage.ClassifierResults;
import timeseriesweka.classifiers.AdvancedAbstractClassifier.AdvancedAbstractClassifier;
import timeseriesweka.classifiers.AdvancedAbstractClassifier.AdvancedAbstractClassifierInterface;
import timeseriesweka.classifiers.ParameterSplittable;
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
    private String trainPath; // todo use one set in classifier instead - for API overhaul
    private boolean postProcess = true;

    private A getClassifier() {
        if(classifier == null) {
            classifier = getClassifierInstance();
        }
        return classifier;
    }

    @Override
    public void setPostProcess(final boolean on) {
        postProcess = on;
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
        else if(searchPermutations) {
            String origTrainPath = trainPath;
            long fold = parseCurrentFold(origTrainPath);
            File parent = new File(origTrainPath).getParentFile();
            String trainPath;
            for(int i = 0; i < size(); i++) {
                trainPath = new File(parent, "fold" + fold + "_" + i + ".csv").getPath();
                if(new File(trainPath).exists()) {
                    throw new IllegalStateException("parameter fold already exists"); // todo not necessarily a bad thing, what if you're adding to some pre-computed folds? Could do with API support
                }
                setParametersFromIndex(i);
                getClassifier().writeCVTrainToFile(trainPath);
                getClassifier().buildClassifier(trainInstances);
                getClassifier().reset();
            }
            getClassifier().writeCVTrainToFile(origTrainPath);
            postProcess();
        }
        getClassifier().buildClassifier(trainInstances);
    }

    private void postProcess() throws Exception {
        reset();
        final long currentFold = parseCurrentFold(trainPath);
        String permutationPath = new File(trainPath).getParent(); // assumes parameter folds saved in same place as train / test folds - todo update API to support this
        File[] permutations = new File(permutationPath).listFiles(file -> {
            if(!file.isFile() || !file.getName().contains("_")) {
                return false;
            }
            return parseFold(file.getName()) == currentFold;
        });
        if(permutations == null || permutations.length <= 0) {
            throw new IllegalArgumentException("No files found");
        }
        Arrays.sort(permutations, Comparator.comparing(File::getName));
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
        String bestParameterPermutation = bestResults.getParas();
        System.out.println(bestParameterPermutation);
        getClassifier().setOptions(bestParameterPermutation.split(","));
    }

    private long parseFold(String path) {
        String str = new File(path).getName();
        String substring = str.substring("fold".length());
        String foldString = substring.substring(0, substring.indexOf("_"));
        return Long.parseLong(foldString);
    }

    private long parseCurrentFold(String path) {
        String str = new File(path).getName();
        String substring = str.substring("trainFold".length());
        String foldString = substring.substring(0, substring.indexOf(".csv"));
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
    public void setParametersFromIndex(final int x) {
        if(x >= 0) {
            parametersSpace.setParameterPermutation(x);
            postProcess = false;
            searchPermutations = false;
        } else {
            searchPermutations = true;
        }
    }

    private boolean searchPermutations = true;

    @Override
    public String getParas() {
        return getParameters();
    }

    @Override
    public double getAcc() {
        return -1;
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
