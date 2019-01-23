package timeseriesweka.classifiers.ee.constituents;

import timeseriesweka.classifiers.AdvancedClassifier;
import timeseriesweka.classifiers.ee.constituents.generators.NnGenerator;
import timeseriesweka.classifiers.ee.iteration.AbstractIndexIterator;
import timeseriesweka.classifiers.ee.iteration.Iterator;
import timeseriesweka.classifiers.ee.iteration.RandomIndexIterator;
import weka.classifiers.Classifier;

public class Constituent implements Iterator<AdvancedClassifier> {
    private AbstractIndexIterator iterator = new RandomIndexIterator();
    private NnGenerator generator;

    public AbstractIndexIterator getIterator() {
        return iterator;
    }

    public void setIterator(final AbstractIndexIterator iterator) {
        this.iterator = iterator;
    }

    public NnGenerator getGenerator() {
        return generator;
    }

    public void setGenerator(final NnGenerator generator) {
        this.generator = generator;
    }

    public Constituent(final AbstractIndexIterator iterator, final NnGenerator generator) {
        this.iterator = iterator;
        this.generator = generator;
    }

    public Constituent(final NnGenerator generator) {
        this.generator = generator;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public AdvancedClassifier next() {
        return generator.get(iterator.next());
    }

    @Override
    public void remove() {
        iterator.remove();
    }

    @Override
    public void reset() {
        iterator.reset();
    }

    @Override
    public void setSeed(final long seed) {
        iterator.setSeed(seed);
    }
}
