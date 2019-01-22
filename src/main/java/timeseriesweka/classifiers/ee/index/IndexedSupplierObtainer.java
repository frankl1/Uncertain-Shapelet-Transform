package timeseriesweka.classifiers.ee.index;

public abstract class IndexedSupplierObtainer<A> implements IndexedSupplier<A> {

    public IndexedSupplierObtainer() {
        this(0);
    }

    public IndexedSupplierObtainer(int size) {
        this.size = size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    private int size;

    protected abstract A obtain(double value);

    @Override
    public int size() {
        return size;
    }

    @Override
    public A get(int index) {
        if (index >= size || index < 0) {
            throw new IllegalArgumentException();
        }
        if(size == 1) {
            return obtain(0);
        } else {
            return obtain((double) index / (size - 1));
        }
    }
}
