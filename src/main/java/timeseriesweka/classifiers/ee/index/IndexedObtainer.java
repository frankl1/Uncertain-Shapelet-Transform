package timeseriesweka.classifiers.ee.index;

public abstract class IndexedObtainer<A> implements Indexed<A> {

    public IndexedObtainer() {
        this(0);
    }

    public IndexedObtainer(int size) {
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
        return obtain((double) index / size);
    }
}
