package timeseriesweka.classifiers.nn.Sampling;

import utilities.Utilities;
import weka.core.Instance;
import weka.core.Instances;

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
    private Random random = new Random();
    private final List<Integer> indicies = new ArrayList<>();

    private void regenerateClassValues() {
        for(int i = 0; i < instancesByClass.size(); i++) {
            indicies.add(i);
        }
    }

    @Override
    public void setInstances(final List<Instance> instances) {
        instancesByClass = Utilities.instancesByClass(instances);
        regenerateClassValues();
    }

    @Override
    public boolean hasNext() {
        return !indicies.isEmpty() || !instancesByClass.isEmpty();
    }

    @Override
    public Instance next() {
        int classValue = indicies.remove(random.nextInt(indicies.size()));
        List<Instance> homogeneousInstances = instancesByClass.get(classValue);
        Instance instance = homogeneousInstances.remove(random.nextInt(homogeneousInstances.size()));
        if(homogeneousInstances.isEmpty()) {
            instancesByClass.remove(classValue);
            for(int i = classValue; i < indicies.size(); i++) {
                indicies.set(i, indicies.get(i) - 1);
            }
        }
        if(indicies.isEmpty()) {
            regenerateClassValues();
        }
        return instance;
    }
}
