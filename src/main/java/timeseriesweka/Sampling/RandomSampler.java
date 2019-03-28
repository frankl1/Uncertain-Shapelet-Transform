package timeseriesweka.Sampling;

import weka.core.Instance;

import java.util.List;
import java.util.Random;

public class RandomSampler implements Sampler {

    private List<Instance> instances;

    @Override
    public void setInstances(final List<Instance> instances) {
        this.instances = instances;
    }

    @Override
    public void setRandom(Random random) {
        this.random = random;
    }

    @Override
    public void setSeed(long seed) {
        random.setSeed(seed);
    }

    private Random random = new Random();

    @Override
    public boolean hasNext() {
        return !instances.isEmpty();
    }

    @Override
    public Instance next() {
        return instances.remove(random.nextInt(instances.size()));
    }
}
