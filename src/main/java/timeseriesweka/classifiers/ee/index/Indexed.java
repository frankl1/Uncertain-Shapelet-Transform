package timeseriesweka.classifiers.ee.index;

public interface Indexed<A> {
    int size();
    A get(int index);
}
