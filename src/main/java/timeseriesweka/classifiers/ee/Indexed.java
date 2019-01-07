package timeseriesweka.classifiers.ee;

public interface Indexed<A> {
    int size();
    A get(int index);
}
