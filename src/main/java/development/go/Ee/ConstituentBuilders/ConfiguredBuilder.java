package development.go.Ee.ConstituentBuilders;

import utilities.Utilities;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

public abstract class ConfiguredBuilder<A> {

    protected abstract void setupParameters(Instances instances);

    protected abstract List<Integer> getParameterSizes();

    public final void setParametersPermutation(int permutation) {
        // todo
    }

    private List<Integer> parametersPermutation;

    public final void setParametersPermutation(List<Integer> parametersPermutation) {
        this.parametersPermutation = new ArrayList<>(parametersPermutation); // todo checks, null + len
    }

    protected final List<Integer> getParametersPermutation() {
        return parametersPermutation;
    }

    public A build() {
        A a = get();
        configure(a, new ArrayList<>(getParametersPermutation()));
        return a;
    }

    protected abstract int getNumParameters();

    protected abstract void configure(A a, List<Integer> parametersPermutation);

    protected abstract A get();
}
