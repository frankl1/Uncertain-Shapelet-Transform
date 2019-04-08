package development.go.Ee;

import evaluation.evaluators.CrossValidationEvaluator;
import evaluation.evaluators.Evaluator;
import evaluation.storage.ClassifierResults;
import evaluation.tuning.ParameterSet;
import evaluation.tuning.ParameterSpace;
import timeseriesweka.classifiers.AdvancedAbstractClassifier.AdvancedAbstractClassifier;
import timeseriesweka.classifiers.Distributable;
import utilities.Utilities;
import weka.classifiers.AbstractClassifier;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class AbstractTuned extends AdvancedAbstractClassifier implements BasedOnTrainSet, Distributable {

    protected final ParameterSpace parameterSpace = new ParameterSpace();
    private Evaluator evaluator = new CrossValidationEvaluator();
    private List<Integer> untestedParameterIndices = new ArrayList<>();
    private List<Integer> testedParameterIndices = new ArrayList<>();
    private Comparator<ClassifierResults> comparator = Comparator.comparingDouble(ClassifierResults::getAcc);
    private AbstractClassifier classifier;
    private int parameterPermutationIndex = -1;
    private boolean distributed = false;
    private boolean useRandomTieBreak = false;

    public int getParameterPermutationIndex() {
        return parameterPermutationIndex;
    }

    protected abstract AbstractClassifier getClassifierParameterPermutation(int index) throws Exception;

    public List<Integer> getUntestedParameterIndices() {
        return Collections.unmodifiableList(untestedParameterIndices);
    }

    protected abstract void recordParameterResult(ParameterRecord parameterRecord);

    public List<Integer> getTestedParameterIndices() {
        return Collections.unmodifiableList(testedParameterIndices);
    }

    public Comparator<ClassifierResults> getComparator() {
        return comparator;
    }

    public void setComparator(final Comparator<ClassifierResults> comparator) {
        this.comparator = comparator;
    }

    @Override
    public void buildClassifier(final Instances trainInstances) throws Exception {
        if(distributed && parameterPermutationIndex >= 0) {
            classifier = getClassifierParameterPermutation(parameterPermutationIndex);
            classifier.buildClassifier(trainInstances);
            ClassifierResults trainResults = evaluator.evaluate(classifier, trainInstances);
            // todo write to file
            throw new UnsupportedOperationException();
        } else {
            untestedParameterIndices.clear();
            testedParameterIndices.clear();
            untestedParameterIndices = getParameterPermutationIndices();
            Iterator<Integer> parameterPermutationIterator = getParameterPermutationIterator(getSeed());
            while (parameterPermutationIterator.hasNext() && withinTrainContract()) {
                int parameterPermutationIndex = parameterPermutationIterator.next();
                logger.info("Running parameter " + parameterPermutationIndex);
                classifier = getClassifierParameterPermutation(parameterPermutationIndex);
                ClassifierResults trainResults = evaluator.evaluate(classifier, trainInstances);
                recordParameterResult(new ParameterRecord(parameterPermutationIndex, classifier, trainResults, true));
                parameterIndexTested(parameterPermutationIndex);
            }
            classifier = combineParameterResults(trainInstances);
        }
    }

    protected List<Integer> getParameterPermutationIndices() {
        untestedParameterIndices.addAll(Utilities.naturalNumbersFromZero(size() - 1));
        return untestedParameterIndices;
    }

    protected abstract Iterator<Integer> getParameterPermutationIterator(int seed);

    public void parameterIndexTested(Integer index) {
        untestedParameterIndices.remove(index);
        testedParameterIndices.add(index);
    }

    protected abstract AbstractClassifier combineParameterResults(Instances trainInstances) throws Exception;

    public boolean isUseRandomTieBreak() {
        return useRandomTieBreak;
    }

    public void setUseRandomTieBreak(final boolean useRandomTieBreak) {
        this.useRandomTieBreak = useRandomTieBreak;
    }

    @Override
    public double classifyInstance(final Instance instance) throws Exception {
        return classifier.classifyInstance(instance);
    }

    @Override
    public double[] distributionForInstance(final Instance instance) throws Exception {
        return classifier.distributionForInstance(instance);
    }

    @Override
    public void setRunDistributed(final boolean on) {
        distributed = on;
    }

    @Override
    public int size() {
        return parameterSpace.size();
    }

    @Override
    public void setSubTaskIndex(final int index) {
        parameterPermutationIndex = index;
    }

    @Override
    public String toString() {
        return "TUNED-" + getClass().getSimpleName().toUpperCase();
    }

}
