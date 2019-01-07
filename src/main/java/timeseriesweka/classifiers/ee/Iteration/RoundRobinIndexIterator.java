package timeseriesweka.classifiers.ee.Iteration;

public class RoundRobinIndexIterator extends LinearIndexIterator {

    @Override
    public boolean hasNext() {
        return workingRange.size() > 0;
    }

    @Override
    public Integer next() {
        index++;
        index %= workingRange.size();
        return index;
    }
}
