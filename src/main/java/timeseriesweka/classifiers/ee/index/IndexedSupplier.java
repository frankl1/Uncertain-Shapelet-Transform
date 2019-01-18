package timeseriesweka.classifiers.ee.index;

public interface IndexedSupplier<A> {
    int size();
    A get(int index);
}
