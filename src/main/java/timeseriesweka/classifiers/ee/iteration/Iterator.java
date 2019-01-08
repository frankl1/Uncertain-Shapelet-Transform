package timeseriesweka.classifiers.ee.iteration;

import utilities.Reproducible;

public interface Iterator<E> extends java.util.Iterator<E>, Reproducible {
    void remove();
    void reset();
}
