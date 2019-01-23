package timeseriesweka.classifiers.ee.iteration;

public class LinearIndexIterator extends AbstractIndexIterator {

    protected int index = 0;

    @Override
    public void remove() {
        workingRange.removeAt(index);
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
