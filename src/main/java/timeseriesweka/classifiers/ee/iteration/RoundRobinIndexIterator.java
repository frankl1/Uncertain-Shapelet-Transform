package timeseriesweka.classifiers.ee.iteration;

public class RoundRobinIndexIterator extends LinearIndexIterator {

    @Override
    public boolean hasNext() {
        return workingRange.size() > 0;
    }

    @Override
    protected int nextIndex() {
        index++;
        index %= workingRange.size();
        return index;
    }
}
