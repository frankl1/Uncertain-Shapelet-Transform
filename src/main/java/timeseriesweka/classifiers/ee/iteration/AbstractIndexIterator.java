package timeseriesweka.classifiers.ee.iteration;

import timeseriesweka.classifiers.ee.range.Range;

public abstract class AbstractIndexIterator implements IndexIterator {
    public Range getRange() {
        return originalRange;
    }

    private final Range originalRange = new Range();

    protected final Range workingRange = new Range();

    protected abstract void resetPostRange();

    @Override
    public final void reset() {
        workingRange.clear();
        workingRange.add(originalRange);
        resetPostRange();
    }
}
