package development.go.Constituents.ParameterSpaces;

import development.go.Indexed.IndexConsumer;
import timeseriesweka.classifiers.nn.NearestNeighbour;
import utilities.Utilities;
import weka.core.Instances;

import java.util.List;
import java.util.function.Supplier;

public abstract class ParameterSpace<A> {

    private List<IndexConsumer<?>> parameters;

    public final void useInstances(Instances instances) {
        parameters = setupParameters(instances);
    }

    protected abstract List<IndexConsumer<?>> setupParameters(Instances instances);

    public void setCombination(int combination) {
        int[] parameterValueIndices = Utilities.fromCombination(combination, getParameterBins());
        for(int i = 0; i < parameterValueIndices.length; i++) {
            parameters.get(i).accept(parameterValueIndices[i]);
        }
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

    public A build() {
        A subject = get();
        configure(subject);
        return subject;
    }

    protected abstract A get();

    protected abstract void configure(A subject);
}
