package timeseriesweka.classifiers.ensembles.ee.Iteration;

import timeseriesweka.classifiers.ensembles.ee.Indexing.IndexedObtainer;
import timeseriesweka.classifiers.ensembles.ee.Indexing.ListElementObtainer;
import utilities.Reproducible;

import java.util.LinkedList;

public abstract class AbstractIterator<E> implements Iterator<E>, Reproducible {

    public IndexedObtainer<E> getIndexedObtainer() {
        return indexedObtainer;
    }

    public void setIndexedObtainer(final IndexedObtainer<E> indexedObtainer) {
        this.indexedObtainer = indexedObtainer;
        reset();
    }

    private IndexedObtainer<E> indexedObtainer;

    public AbstractIterator(final IndexedObtainer<E> indexedObtainer) {
        this.indexedObtainer = indexedObtainer;
    }

    public AbstractIterator() {
        this(new ListElementObtainer<>(new LinkedList<>()));
    }

    @Override
    public int size() {
        return indexedObtainer.getSize();
    }
}
