package timeseriesweka.classifiers.ensembles.ee.Iteration;

import timeseriesweka.classifiers.ensembles.ee.Indexing.IndexedObtainer;

public class RoundRobinIterator<E> extends LinearIterator<E> {
    public RoundRobinIterator(final IndexedObtainer<E> indexedObtainer) {
        super(indexedObtainer);
    }

    public RoundRobinIterator() {

    }

    @Override
    protected void nextIndex() {
        setNextIndex((getNextIndex() + 1) % size());
    }
}
