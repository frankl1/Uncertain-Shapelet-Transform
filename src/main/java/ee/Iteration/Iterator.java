package timeseriesweka.classifiers.ensembles.ee.Iteration;

import utilities.Reproducible;

public interface Iterator<E> extends java.util.Iterator<E>, Reproducible {
    E get();
    void shift();
    default E next() {
        shift();
        return get();
    }
    boolean hasNext();
    void reset();
    int size();
    void remove();
}
