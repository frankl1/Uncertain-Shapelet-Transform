package Tuning;

import Tuning.ParameterSpaces.BasedOnTrainSet;
import Tuning.ParameterSpaces.ParameterSpaces;
import development.go.Ee.ParameterIteration.Iterator;
import development.go.Ee.ParameterIteration.RandomIterator;
import evaluation.evaluators.CrossValidationEvaluator;
import evaluation.storage.ClassifierResults;
import timeseriesweka.classifiers.AdvancedAbstractClassifier.AdvancedAbstractClassifier;
import timeseriesweka.classifiers.Distributable;
import utilities.TrainAccuracyEstimate;
import utilities.Utilities;
import weka.classifiers.AbstractClassifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

import java.io.File;
import java.util.*;

public class Tuned extends AdvancedAbstractClassifier implements Distributable, BasedOnTrainSet {
    protected AbstractClassifier classifier;
    private Comparator<ClassifierResults> parameterPermutationComparator = (results, t1) -> {
        results.findAllStats();
        t1.findAllStats();
        return Double.compare(results.getAcc(), t1.getAcc());
    };
    private ParameterSpaces parameterSpaces = new ParameterSpaces();
    private Comparator<ClassifierResults> comparator = Comparator.comparingDouble(ClassifierResults::getAcc);
    private int parameterIndex = -1;
    private boolean distributed = true;
    private final Set<Integer> remainingParameters = new HashSet<>();
    private final Map<Integer, ClassifierResults> parametersResults = new HashMap<>();

    public Set<Integer> getRemainingParameters() {
        return remainingParameters;
    }

    public Iterator<Integer> getParameterIterator() {
        return parameterIterator;
    }

    public void setParameterIterator(final Iterator<Integer> parameterIterator) {
        this.parameterIterator = parameterIterator;
    }

    private Iterator<Integer> parameterIterator = new RandomIterator<>();

    public ParameterSpaces getParameterSpaces() {
        return parameterSpaces;
    }

    public void setParameterSpaces(final ParameterSpaces parameterSpaces) {
        this.parameterSpaces = parameterSpaces;
    }

    private void distributedSetup(final Instances trainInstances) throws Exception {
        if(parameterIndex < 0) {
            nonDistributedSetup(trainInstances);
        } else {
            evaluateParameter(trainInstances, parameterIndex);
        }
    }

    @Override
    public void buildClassifier(final Instances trainInstances) throws Exception {
        if(distributed) {
            distributedSetup(trainInstances);
        } else {
            nonDistributedSetup(trainInstances);
        }
        classifier.buildClassifier(trainInstances);
    }

    protected void evaluateParameter(final Instances trainInstances, int index) throws Exception {
        if(!remainingParameters.remove(index)) throw new IllegalArgumentException("parameter not waiting");
        File parent = new File(getSavePathNotNull(), "params");
        setParameterIndex(index);
        File file = new File(parent, "paramTrainFold" + index + ".csv");
        String paramPath = file.getPath();
        ClassifierResults results;
        boolean loadedFromFile = false;
        if(isCheckpointing() && file.exists()) {
            results = new ClassifierResults(); // todo checkpoint / contract
            results.loadResultsFromFile(paramPath);
            loadedFromFile = true;
        } else {
            classifier.buildClassifier(trainInstances);
            if(classifier instanceof TrainAccuracyEstimate) {
                results = ((TrainAccuracyEstimate) classifier).getTrainResults();
            } else {
                CrossValidationEvaluator crossValidator = new CrossValidationEvaluator();
                crossValidator.setNumFolds(10); // todo var for num folds, but if classifier finds its own then need a flag - api redesign!
                crossValidator.setSeed(getSeedNotNull());
                results = crossValidator.crossValidateWithStats(classifier, trainInstances);
            }
            results.setParas("paramIndex," + index + "," + results.getParas());
        }
        if(isCheckpointing() && !loadedFromFile) {
            results.writeFullResultsToFile(paramPath);
        }
        parametersResults.put(index, results);
    }

    // todo is it best to instantiate random where needed with the seed or have one for the class at detriment of multiple calls producing diff output

    protected void setParameterIndex(int index) {
        parameterSpaces.setParameterPermutation(index);
    }

    private String getSavePathNotNull() {
        String savePath = getSavePath();
        if(savePath == null) {
            throw new IllegalStateException("save path null");
        }
        return savePath;
    }

    protected void evaluateParameters(final Instances trainInstances) throws Exception {
        for(int i = 0; i < size(); i++) {
            evaluateParameter(trainInstances, i); // todo iteration strat + contracting / checkpointing
        }
    }

    private void nonDistributedSetup(final Instances trainInstances) throws Exception {
        trainTime = 0;
        remainingParameters.clear();
        parametersResults.clear();
        remainingParameters.addAll(Utilities.naturalNumbersFromZero(size()));
        if(seed == null) {
            throw new IllegalStateException("seed not set");
        }
        evaluateParameters(trainInstances);
        onAllParametersTried();
    }

    private void onAllParametersTried() throws Exception {
        Map.Entry<Integer, ClassifierResults> best = Utilities.best(parametersResults.entrySet().iterator(), comparator, Map.Entry::getValue, random);
        int parameterIndex = best.getKey();
        ClassifierResults results = best.getValue();
        String bestParameterPermutation = results.getParas(); // todo trim param index, use setOption
//        System.out.println(bestParameterPermutation); // todo log instead
        classifier.setOptions(bestParameterPermutation.split(","));
    }

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

    public Comparator<ClassifierResults> getParameterPermutationComparator() {
        return parameterPermutationComparator;
    }

    public void setParameterPermutationComparator(final Comparator<ClassifierResults> parameterPermutationComparator) {
        this.parameterPermutationComparator = parameterPermutationComparator;
    }

    @Override
    public void setRunDistributed(final boolean on) {
        distributed = on;
    }

    public int size() {
        return parameterSpaces.size();
    }

    @Override
    public void setSubTaskIndex(final int x) {
        parameterIndex = x;
    }

    /**
     * TODO
     * Below just defers all funcs to the classifier itself. Not sure if this should be the default behaviour of a tuner?
     */
    @Override
    public void copyFromSerObject(final Object obj) throws Exception {
        super.copyFromSerObject(obj);
        Tuned other = (Tuned) obj;
        classifier = other.classifier;
        parameterSpaces = other.parameterSpaces;
        parameterPermutationComparator = other.parameterPermutationComparator;
        comparator = other.comparator;
        parameterIndex = other.parameterIndex;
        distributed = other.distributed;
        parameterIterator = other.parameterIterator;
    }

}
