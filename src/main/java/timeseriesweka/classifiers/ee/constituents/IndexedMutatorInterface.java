package timeseriesweka.classifiers.ee.constituents;

public interface IndexedMutatorInterface<A, B> extends Mutable<A, B> {
    int size();
}
