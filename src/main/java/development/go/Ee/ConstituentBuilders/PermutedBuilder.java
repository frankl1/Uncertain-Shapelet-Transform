package development.go.Ee.ConstituentBuilders;

import weka.core.Instances;

public interface PermutedBuilder<A> extends Builder<A> {
    void setPermutation(int permutation);
    void useInstances(Instances instances);
}
