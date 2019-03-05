package development.go.Ee.Constituents;

import development.go.Indexed.IndexConsumer;
import timeseriesweka.classifiers.nn.Nn;
import utilities.Utilities;
import weka.core.Instances;

import java.util.List;

public abstract class AbstractConstituentBuilder {
    private List<IndexConsumer<?>> parameters;

    public final AbstractConstituentBuilder useInstances(Instances instances) {
        parameters = setupParameters(instances);
        return this;
    }

    protected abstract List<IndexConsumer<?>> setupParameters(Instances instances);

    public AbstractConstituentBuilder setCombination(int combination) {
        int[] parameterValueIndices = Utilities.fromCombination(combination, getParameterBins());
        for(int i = 0; i < parameterValueIndices.length; i++) {
            parameters.get(i).accept(parameterValueIndices[i]);
        }
        return this;
    }

    private int[] getParameterBins() {
        int[] sizes = new int[parameters.size()];
        for(int i = 0; i < sizes.length; i++) {
            sizes[i] = parameters.get(i).size();
        }
        return sizes;
    }

    public int size() {
        return Utilities.numCombinations(getParameterBins());
    }

    public abstract Nn build();
}
