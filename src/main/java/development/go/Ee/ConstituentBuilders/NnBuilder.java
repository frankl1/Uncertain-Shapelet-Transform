package development.go.Ee.ConstituentBuilders;

import timeseriesweka.classifiers.nn.Nn;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

public class NnBuilder extends ConfiguredBuilder<Nn> {

    @Override
    protected void setupParameters(final Instances instances) {

    }

    @Override
    protected List<Integer> getParameterSizes() {
        return new ArrayList<>();
    }

    @Override
    protected int getNumParameters() {
        return 0;
    }

    @Override
    protected void configure(final Nn nn, List<Integer> parametersPermutation) {

    }

    @Override
    protected Nn get() {
        Nn nn = new Nn();
        return nn;
    }
}
