package timeseriesweka.classifiers.ensembles.ee.Iteration;

import timeseriesweka.classifiers.ensembles.ee.Indexing.IndexedObtainer;

import java.util.Set;
import java.util.TreeSet;

public class LinearIterator<E> extends AbstractIterator<E> {

    public LinearIterator(final IndexedObtainer<E> indexedObtainer) {
        super(indexedObtainer);
    }

    private final Set<Integer> removed = new TreeSet<>();

    public LinearIterator() {
    }

    protected Set<Integer> getRemoved() {
        return removed;
    }

    private int index = 0;

    protected int getIndex() {
        return index;
    }

    protected void setIndex(final int index) {
        this.index = index;
    }

    protected void setNextIndex(final int nextIndex) {
        this.nextIndex = nextIndex;
    }

    private int nextIndex = -1;

    @Override
    public void remove() {
        removed.add(index);
        hasNext();
        shift();
    }

    @Override
    public void reset() {
        setIndex(0);
        removed.clear();
    }

    @Override
    public boolean hasNext() {
        int size = size();
        do {
            nextIndex();
        } while (removed.contains(nextIndex) && nextIndex < size); // todo perhaps switch around inheritance with round robin?
        if(nextIndex >= size) {
            nextIndex = -1;
            return false;
        } else {
            return true;
        }
    }

    protected void nextIndex() {
        nextIndex = nextIndex + 1;
    }

    protected int getNextIndex() {
        return nextIndex;
    }

    @Override
    public void shift() {
        index = nextIndex;
    }

    @Override
    public void setSeed(long seed) {

    }

    @Override
    public E get() {
        if(index < 0) {
            throw new IllegalStateException();
        }
        return getIndexedObtainer().obtain(getIndex());
    }
}
