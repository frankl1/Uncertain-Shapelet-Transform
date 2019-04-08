package development.go.Ee;

import development.go.Ee.ParameterIteration.RandomIterator;
import development.go.Ee.ParameterIteration.SourcedIterator;
import evaluation.tuning.ParameterSet;
import evaluation.tuning.ParameterSpace;
import utilities.Utilities;
import weka.classifiers.AbstractClassifier;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class Tuned extends AbstractTuned {
    private SourcedIterator<Integer, List<Integer>> iterator = new RandomIterator<>();

    public ParameterSpace getParameterSpace() {
        return parameterSpace;
    }

    @Override
    protected AbstractClassifier combineParameterResults(final Instances trainInstances) throws Exception {
        Iterator<ParameterRecord> parameterResultsIterator = parameterRecords.iterator();
        if(!parameterResultsIterator.hasNext()) {
            throw new IllegalStateException("no params evaluated");
        }
        ParameterRecord bestParameterRecord;
        if(isUseRandomTieBreak()) {
            bestParameterRecord = Utilities.best(parameterResultsIterator, getComparator(), ParameterRecord::getTrainResults, random);
        } else {
            bestParameterRecord = Utilities.best(parameterResultsIterator, getComparator(), ParameterRecord::getTrainResults).get(0);
        }
        trainResults = bestParameterRecord.getTrainResults();
        AbstractClassifier classifier = bestParameterRecord.getClassifier();
        if(!bestParameterRecord.isBuilt()) {
            classifier.setOptions(bestParameterRecord.getTrainResults().getParas().split(","));
            classifier.buildClassifier(trainInstances);
            bestParameterRecord.setBuilt(true);
        }
        return classifier;
    }

    @Override
    protected AbstractClassifier getClassifierParameterPermutation(final int index) throws Exception {
        ParameterSet parameterPermutation = parameterSpace.getParameterSet(index);
        AbstractClassifier classifier = getClassifierSupplier().get();
        classifier.setOptions(parameterPermutation.toOptionsList());
        return classifier;
    }

    @Override
    protected void recordParameterResult(final ParameterRecord parameterRecord) {
        parameterRecords.add(parameterRecord);
    }

    @Override
    protected Iterator<Integer> getParameterPermutationIterator(int seed) {
        iterator.setSource(Utilities.naturalNumbersFromZero(size()));
        iterator.setSeed(seed);
        return iterator;
    }

    public SourcedIterator<Integer, List<Integer>> getIterator() {
        return iterator;
    }

    public void setIterator(final SourcedIterator<Integer, List<Integer>> iterator) {
        this.iterator = iterator;
    }

    public Supplier<AbstractClassifier> getClassifierSupplier() {
        return classifierSupplier;
    }

    public void setClassifierSupplier(final Supplier<AbstractClassifier> classifierSupplier) {
        this.classifierSupplier = classifierSupplier;
    }

    private Supplier<AbstractClassifier> classifierSupplier;
    private List<ParameterRecord> parameterRecords = new ArrayList<>();

    @Override
    public String toString() {
        return "TUNED-" + classifierSupplier.get().toString();
    }
}
