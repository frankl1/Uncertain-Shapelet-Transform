package utilities.range;

import timeseriesweka.classifiers.ee.index.IndexedSupplier;

public class ValueRange<A> implements IndexedSupplier<A> {

    private IndexedSupplier<? extends A> indexedSupplier;

    public void setIndexedSupplier(final IndexedSupplier<? extends A> indexedSupplier) {
        this.indexedSupplier = indexedSupplier;
        reset();
    }

    public void setRange(final Range range) {
        this.range = range;
    }

    private Range range;

    public ValueRange(IndexedSupplier<? extends A> indexedSupplier, Range range) {
        this.indexedSupplier = indexedSupplier;
        this.range = range;
    }

    public ValueRange() {
        this(new IndexedSupplier<A>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public A get(final int index) {
                return null;
            }
        }, new Range());
    }

    public IndexedSupplier<? extends A> getIndexedSupplier() {
        return indexedSupplier;
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
        range.add(0, indexedSupplier.size() - 1);
    }

    @Override
    public A get(final int index) {
        return indexedSupplier.get(range.get(index));
    }

    public <B extends A> Integer get(final B subject) {
        throw new UnsupportedOperationException();
    }
}
