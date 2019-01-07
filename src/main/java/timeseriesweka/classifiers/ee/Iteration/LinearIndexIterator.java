package timeseriesweka.classifiers.ee.Iteration;

import timeseriesweka.classifiers.ee.Indexed;
import timeseriesweka.classifiers.ee.Range;

public class LinearIndexIterator extends AbstractIndexIterator {

    protected int index = 0;

    @Override
    public void remove() {
        workingRange.remove(index);
    }

    @Override
    public boolean hasNext() {
        return index < workingRange.size();
    }

    @Override
    public Integer next() {
        index++;
        return index;
    }

    public void resetPostRange() {
        index = 0;
    }

    @Override
    public void setSeed(final long seed) {

    }
}
