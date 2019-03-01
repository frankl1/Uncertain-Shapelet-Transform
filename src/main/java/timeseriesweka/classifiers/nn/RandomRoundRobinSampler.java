package timeseriesweka.classifiers.nn;

import utilities.Utilities;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RandomRoundRobinSampler implements Sampler {
    @Override
    public void setRandom(final Random random) {
        this.random = random;
    }

    @Override
    public void setSeed(final long seed) {
        random.setSeed(seed);
    }

    private int index;
    private List<Instances> instancesByClass;
    private Random random = new Random();

    @Override
    public void setInstances(final Instances instances) {
        instancesByClass = new ArrayList<>(Arrays.asList(Utilities.instancesByClass(instances)));
        index = random.nextInt(instancesByClass.size());
    }

    @Override
    public boolean hasNext() {
        return !instancesByClass.isEmpty();
    }

    @Override
    public Instance next() {
        Instances instances = instancesByClass.get(index);
        Instance instance = instances.remove(random.nextInt(instances.numInstances()));
        if(instances.isEmpty()) {
            instancesByClass.remove(index);
        } else {
            index++;
        }
        if(index >= instancesByClass.size()) {
            index = 0;
        }
        return instance;
    }
}
