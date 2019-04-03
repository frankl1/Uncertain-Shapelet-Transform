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


public abstract class AbstractTuned extends AdvancedAbstractClassifier implements BasedOnTrainSet, Distributable {

    protected final ParameterSpace parameterSpace = new ParameterSpace();
    private Evaluator evaluator = new CrossValidationEvaluator();
    private List<Integer> untestedParameterIndices = new ArrayList<>();
    private List<Integer> testedParameterIndices = new ArrayList<>();
    private Comparator<ClassifierResults> comparator = Comparator.comparingDouble(ClassifierResults::getAcc);
    private List<ParameterResult> parameterResults = new ArrayList<>();
    private AbstractClassifier classifier;

    public int getParameterPermutationIndex() {
        return parameterPermutationIndex;
    }

    private int parameterPermutationIndex = -1;
    private boolean distributed = false;

    protected void populateParameterPermutationIndices() {
        untestedParameterIndices.clear();
        testedParameterIndices.clear();
        untestedParameterIndices.addAll(Utilities.naturalNumbersFromZero(size() - 1));
    }

    public List<Integer> getUntestedParameterIndices() {
        return Collections.unmodifiableList(untestedParameterIndices);
    }

    public List<Integer> getTestedParameterIndices() {
        return Collections.unmodifiableList(testedParameterIndices);
    }

    public Comparator<ClassifierResults> getComparator() {
        return comparator;
    }

    public void setComparator(final Comparator<ClassifierResults> comparator) {
        this.comparator = comparator;
    }

    public AbstractClassifier getClassifier() {
        return classifier;
    }

    public void setClassifier(final AbstractClassifier classifier) {
        this.classifier = classifier;
    }

    protected AbstractClassifier postTune(Instances trainInstances, List<ParameterResult> parameterResults) throws Exception {
        Iterator<ParameterResult> parameterResultsIterator = parameterResults.iterator();
        if(!parameterResultsIterator.hasNext()) {
            throw new IllegalStateException("no params evaluated");
        }
        ParameterResult bestParameterResult;
        if(useRandomTieBreak) {
            bestParameterResult = Utilities.best(parameterResultsIterator, comparator, ParameterResult::getTrainResults, random);
        } else {
            bestParameterResult = Utilities.best(parameterResultsIterator, comparator, ParameterResult::getTrainResults).get(0);
        }
        trainResults = bestParameterResult.getTrainResults();
        classifier = bestParameterResult.getClassifier();
        if(!bestParameterResult.isBuilt()) {
            classifier.setOptions(bestParameterResult.getTrainResults().getParas().split(","));
            classifier.buildClassifier(trainInstances);
            bestParameterResult.setBuilt(true);
        }
        return classifier;
    }

    @Override
    public void buildClassifier(final Instances trainInstances) throws Exception {
        if(distributed && parameterPermutationIndex >= 0) {
            setParameterPermutationIndex(parameterPermutationIndex);
            classifier.buildClassifier(trainInstances);
            ClassifierResults trainResults = evaluator.evaluate(classifier, trainInstances);
            // todo write to file
            throw new UnsupportedOperationException();
        } else {
            populateParameterPermutationIndices();
            Iterator<Integer> parameterPermutationIterator = getParameterPermutationIterator(getSeedNotNull());
            while (parameterPermutationIterator.hasNext() && withinTrainContract()) {
                int parameterPermutationIndex = parameterPermutationIterator.next();
                System.out.println(parameterPermutationIndex);
                setParameterPermutationIndex(parameterPermutationIndex);
                ClassifierResults trainResults = evaluator.evaluate(classifier, trainInstances);
                parameterResults.add(new ParameterResult(classifier, trainResults, true));
                parameterIndexTested(parameterPermutationIndex);
            }
            classifier = postTune(trainInstances, parameterResults);
        }
    }

    public boolean isUseRandomTieBreak() {
        return useRandomTieBreak;
    }

    public void setUseRandomTieBreak(final boolean useRandomTieBreak) {
        this.useRandomTieBreak = useRandomTieBreak;
    }

    private boolean useRandomTieBreak = false;

    protected abstract Iterator<Integer> getParameterPermutationIterator(int seed);

    protected void setParameterPermutationIndex(int index) throws Exception {
        ParameterSet parameterPermutation = parameterSpace.getParameterSet(index);
        classifier.setOptions(parameterPermutation.toOptionsList());
    }

    public void parameterIndexTested(Integer index) {
        untestedParameterIndices.remove(index);
        testedParameterIndices.add(index);
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

    private static class ParameterResult {
        private final AbstractClassifier classifier;
        private final ClassifierResults trainResults;
        private boolean built;

        private ParameterResult(final AbstractClassifier classifier, final ClassifierResults trainResults, final boolean built) {
            this.classifier = classifier;
            this.trainResults = trainResults;
            this.built = built;
        }

        public AbstractClassifier getClassifier() {
            return classifier;
        }

        public ClassifierResults getTrainResults() {
            return trainResults;
        }

        public boolean isBuilt() {
            return built;
        }

        public void setBuilt(final boolean built) {
            this.built = built;
        }
    }
}
