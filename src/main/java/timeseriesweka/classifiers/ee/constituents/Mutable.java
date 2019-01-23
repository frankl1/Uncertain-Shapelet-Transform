package timeseriesweka.classifiers.ee.constituents;

public interface Mutable<A, B> {
    <C extends A, D extends B> void setValue(C subject, D value);
    <C extends A> B getValue(C subject);
}
