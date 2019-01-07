package timeseriesweka.classifiers.ee;

public class ValueRange<A> implements Indexed<A> {
    private Indexed<A> indexed;

    public void setIndexed(final Indexed<A> indexed) {
        this.indexed = indexed;
    }

    public void setRange(final Range range) {
        this.range = range;
    }

    private Range range;

    public ValueRange(Indexed<A> indexed, Range range) {
        this.indexed = indexed;
        this.range = range;
    }

    public Indexed<A> getIndexed() {
        return indexed;
    }

    public Range getRange() {
        return range;
    }

    @Override
    public int size() {
        return range.size();
    }

    public void reset() {
        range.clear();
        range.add(0, indexed.size() - 1);
    }

    @Override
    public A get(final int index) {
        return indexed.get(range.get(index));
    }
}
