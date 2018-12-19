package timeseriesweka.classifiers.ensembles.ee.Indexing;

import utilities.Obtainer;
import utilities.Supplier;

public abstract class IndexedObtainer<A> implements Obtainer<Integer, A> {

    private Supplier<Integer> size;

    public IndexedObtainer(final Supplier<Integer> size) {
        this.size = size;
    }

    public int getSize() {
        return size.supply();
    }

    protected abstract A obtainByIndex(final Integer index);

    @Override
    public A obtain(final Integer index) {
        if(index >= getSize()) {
            throw new IllegalArgumentException();
        }
        return obtainByIndex(index);
    }
}
