package development.go.Ee.ParameterIteration;

import development.go.Ee.ConstituentBuilders.ConstituentBuilder;
import timeseriesweka.classifiers.Nn.AbstractNn;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public interface Iterator<A> extends java.util.Iterator<A> {
    void setSource(List<A> source);

    default void setRandom(Random random) {

    }

    default void setSeed(long seed) {

    }
}
