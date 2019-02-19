package timeseriesweka.classifiers.ee;

public interface Selector<A> {
    boolean add(A a);
    A[] getSelected();
}
