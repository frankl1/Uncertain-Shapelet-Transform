package timeseriesweka.classifiers.ee.Constituents;

import timeseriesweka.classifiers.ee.Iteration.AbstractIndexIterator;
import timeseriesweka.classifiers.ee.Iteration.Iterator;
import timeseriesweka.classifiers.ee.Iteration.LinearIndexIterator;
import timeseriesweka.classifiers.ee.Parameter;
import timeseriesweka.classifiers.ee.ParameterCombiner;
import timeseriesweka.classifiers.ee.Range;
import weka.classifiers.Classifier;

public abstract class Constituent implements Iterator<Classifier> {

    private final ParameterCombiner parameterCombiner = new ParameterCombiner();

    public AbstractIndexIterator getParameterCombinationIterator() {
        return parameterCombinationIterator;
    }

    public void setParameterCombinationIterator(final AbstractIndexIterator parameterCombinationIterator) {
        this.parameterCombinationIterator = parameterCombinationIterator;
        reset();
    }

    private AbstractIndexIterator parameterCombinationIterator = new LinearIndexIterator();

    protected abstract Classifier build();

    protected abstract Parameter<?>[] getParameters();

    @Override
    public boolean hasNext() {
        return parameterCombinationIterator.hasNext();
    }

    @Override
    public Classifier next() {
        Classifier classifier = build();
        int combinationIndex = parameterCombinationIterator.next();
        parameterCombiner.setParameterCombination(combinationIndex);
        return classifier;
    }

    @Override
    public void remove() {
        parameterCombinationIterator.remove();
    }

    @Override
    public void reset() {
        parameterCombiner.setParameters(getParameters());
        Range range = parameterCombinationIterator.getRange();
        range.clear();
        range.add(0, parameterCombiner.size() - 1);
    }

    @Override
    public void setSeed(final long seed) {
        parameterCombinationIterator.setSeed(seed);
    }
}
