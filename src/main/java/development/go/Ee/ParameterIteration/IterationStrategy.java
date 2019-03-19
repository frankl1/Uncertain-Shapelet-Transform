package development.go.Ee.ParameterIteration;

import development.go.Ee.ConstituentBuilders.ConstituentBuilder;
import timeseriesweka.classifiers.nn.Nn;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public interface IterationStrategy extends Iterator<Nn> {
    void useConstituentBuilders(List<ConstituentBuilder> constituentBuilders);

    default void setRandom(Random random) {

    }

    default void setSeed(long seed) {

    }
}
