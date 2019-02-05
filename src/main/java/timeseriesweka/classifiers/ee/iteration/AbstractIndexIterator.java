package timeseriesweka.classifiers.ee.iteration;

import utilities.range.Range;

public abstract class AbstractIndexIterator implements IndexIterator {
    public Range getRange() {
        return originalRange;
    }

    public void setRange(Range range) {
        originalRange = new Range(range);

    }

    private Range originalRange = new Range();

    protected final Range workingRange = new Range();

    protected abstract void resetPostRange();

    @Override
    public final void reset() {
        workingRange.clear();
        workingRange.add(originalRange);
        resetPostRange();
    }
}
