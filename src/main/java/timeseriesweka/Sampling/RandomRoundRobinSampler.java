package timeseriesweka.Sampling;

import utilities.Utilities;
import weka.core.Instance;

import java.util.*;

public class RandomRoundRobinSampler implements Sampler {
    @Override
    public void setRandom(final Random random) {
        this.random = random;
    }

    @Override
    public void setSeed(final long seed) {
        random.setSeed(seed);
    }

    private List<List<Instance>> instancesByClass;
    private final List<List<Instance>> nonEmptyInstancesByClass = new ArrayList<>();
    private Random random = new Random();

    @Override
    public void setInstances(final List<Instance> instances) {
        instancesByClass = Utilities.instancesByClass(instances);
    }

    @Override
    public boolean hasNext() {
        return !instancesByClass.isEmpty();
    }

    @Override
    public Instance next() {
        List<Instance> homogeneousInstances = instancesByClass.remove(random.nextInt(instancesByClass.size()));
        Instance instance = homogeneousInstances.remove(random.nextInt(homogeneousInstances.size()));
        if(!homogeneousInstances.isEmpty()) {
            nonEmptyInstancesByClass.add(homogeneousInstances);
        }
        if(instancesByClass.isEmpty()) {
            instancesByClass.addAll(nonEmptyInstancesByClass);
            nonEmptyInstancesByClass.clear();
        }
        return instance;
    }
}
