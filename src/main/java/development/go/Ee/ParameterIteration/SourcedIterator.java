package development.go.Ee.ParameterIteration;

import timeseriesweka.classifiers.Nn.AbstractNn;

import java.util.List;
import java.util.Random;

public interface SourcedIterator<A, B> extends java.util.Iterator<A> {
    void setSource(B source);

    void setRandom(Random random);

    void setSeed(long seed);
}
