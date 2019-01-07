package utilities.instances;

import weka.core.Instance;
import weka.core.Instances;

public class Distribution {
    public static int[] findClassDistribution(Instances instances) {
        int[] distibution = new int[instances.numClasses()];
        for(Instance instance : instances) {
            distibution[(int) instance.classValue()]++;
        }
        return distibution;
    }

    public static Instances[] binClasses(Instances instances) {
        Instances[] bins = new Instances[instances.numClasses()];
        for(int i = 0; i < bins.length; i++) {
            bins[i] = new Instances(instances, 0);
        }
        for(Instance instance : instances) {
            bins[(int) instance.classValue()].add(instance);
        }
        return bins;
    }
}
