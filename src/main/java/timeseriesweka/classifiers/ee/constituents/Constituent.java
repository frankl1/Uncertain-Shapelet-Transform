package timeseriesweka.classifiers.ee.constituents;

import timeseriesweka.classifiers.Classifier;
import timeseriesweka.classifiers.ee.iteration.AbstractIndexIterator;
import timeseriesweka.classifiers.ee.iteration.Iterator;
import timeseriesweka.classifiers.ee.iteration.LinearIndexIterator;
import timeseriesweka.classifiers.ee.index.IndexConsumer;
import timeseriesweka.classifiers.ee.index.CombinedIndexConsumer;
import timeseriesweka.classifiers.ee.range.Range;

public abstract class Constituent implements Iterator<Classifier> {

    private final CombinedIndexConsumer parameterCombiner = new CombinedIndexConsumer();

    public AbstractIndexIterator getParameterCombinationIterator() {
        return parameterCombinationIterator;
    }

    public void setParameterCombinationIterator(final AbstractIndexIterator parameterCombinationIterator) {
        this.parameterCombinationIterator = parameterCombinationIterator;
        reset();
    }

    private AbstractIndexIterator parameterCombinationIterator = new LinearIndexIterator();

    protected abstract Classifier build();

    protected abstract IndexConsumer<?>[] getParameters();

    @Override
    public boolean hasNext() {
        return parameterCombinationIterator.hasNext();
    }

    @Override
    public Classifier next() {
        Classifier classifier = build();
        int combinationIndex = parameterCombinationIterator.next();
        parameterCombiner.accept(combinationIndex);
        return classifier;
    }

    @Override
    public void remove() {
        parameterCombinationIterator.remove();
    }

    @Override
    public void reset() {
        parameterCombiner.setIndexConsumers(getParameters());
        Range range = parameterCombinationIterator.getRange();
        range.clear();
        range.add(0, parameterCombiner.size() - 1);
    }

    @Override
    public void setSeed(final long seed) {
        parameterCombinationIterator.setSeed(seed);
    }
}
