package timeseriesweka.classifiers.nn.Sampling;

import weka.core.Instance;
import weka.core.Instances;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public interface Sampler extends Iterator<Instance>, Serializable { // todo custom iterator with set seed / random

    void setInstances(List<Instance> instances);

    default void setRandom(Random random) {

    }

    default void setSeed(long seed) {

    }
}
