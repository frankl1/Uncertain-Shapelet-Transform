package timeseriesweka.classifiers.ee.abcdef;

import java.util.function.Supplier;

public class TargetedMutator<A> implements Indexed {
    public IndexedMutator<A, ?> getIndexedMutator() {
        return indexedMutator;
    }

    public void setIndexedMutator(final IndexedMutator<A, ?> indexedMutator) {
        this.indexedMutator = indexedMutator;
    }

    public Supplier<? extends A> getSupplier() {
        return supplier;
    }

    public void setSupplier(final Supplier<? extends A> supplier) {
        this.supplier = supplier;
    }

    private IndexedMutator<A, ?> indexedMutator;
    private Supplier<? extends A> supplier;

    public TargetedMutator(final IndexedMutator<A, ?> indexedMutator, final Supplier<? extends A> supplier) {
        this.indexedMutator = indexedMutator;
        this.supplier = supplier;
    }

    public int size() {
        return indexedMutator.size();
    }

    public <D extends Integer> void setValueAt(final D value) {
        indexedMutator.setValue(supplier.get(), value);
    }

    public Integer getIndex() {
        return indexedMutator.getValue(supplier.get());
    }
}
