package timeseriesweka.classifiers.ee.abcdef;

public interface IndexedMutatorInterface<A, B> extends Mutable<A, B> {
    int size();
}
