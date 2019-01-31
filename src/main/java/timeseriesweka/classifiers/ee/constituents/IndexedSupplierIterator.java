package timeseriesweka.classifiers.ee.constituents;

import timeseriesweka.classifiers.AdvancedClassifier;
import timeseriesweka.classifiers.ee.constituents.generators.ParameterisedSupplier;
import timeseriesweka.classifiers.ee.index.IndexedSupplier;
import timeseriesweka.classifiers.ee.iteration.AbstractIndexIterator;
import timeseriesweka.classifiers.ee.iteration.Iterator;
import timeseriesweka.classifiers.ee.iteration.RandomIndexIterator;

public class IndexedSupplierIterator<A, B extends IndexedSupplier<? extends A>> implements Iterator<A> {
    private AbstractIndexIterator iterator = new RandomIndexIterator();
    private B indexedSupplier;

    public AbstractIndexIterator getIterator() {
        return iterator;
    }

    public void setIterator(final AbstractIndexIterator iterator) {
        this.iterator = iterator;
    }

    public B getIndexedSupplier() {
        return indexedSupplier;
    }

    public void setIndexedSupplier(final B indexedSupplier) {
        this.indexedSupplier = indexedSupplier;
    }

    public IndexedSupplierIterator(final B indexedSupplier) {
        setIndexedSupplier(indexedSupplier);
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public A next() {
        return indexedSupplier.get(iterator.next());
    }

    @Override
    public void remove() {
        iterator.remove();
    }

    @Override
    public void reset() {
        iterator.getRange().clear();
        iterator.getRange().add(0, indexedSupplier.size());
        iterator.reset();
    }

    @Override
    public void setSeed(final long seed) {
        iterator.setSeed(seed);
    }
}
