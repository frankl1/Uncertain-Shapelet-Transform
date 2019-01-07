package timeseriesweka.classifiers.ee.Iteration;

import timeseriesweka.classifiers.Reproducible;

public interface Iterator<E> extends java.util.Iterator<E>, Reproducible {
    void remove();
    void reset();
}
