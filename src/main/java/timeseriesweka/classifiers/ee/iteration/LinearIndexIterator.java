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

    public void resetPostRange() {
        index = 0;
    }

    @Override
    protected int nextIndex() {
        index++;
        return index;
    }

    @Override
    public void setSeed(final long seed) {

    }
}
