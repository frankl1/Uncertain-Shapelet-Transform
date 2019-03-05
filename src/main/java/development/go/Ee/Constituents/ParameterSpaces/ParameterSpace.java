package development.go.Ee.Constituents.ParameterSpaces;

import development.go.Indexed.IndexConsumer;
import utilities.Utilities;
import weka.core.Instances;

import java.util.List;

public abstract class ParameterSpace<A> {

    private List<IndexConsumer<?>> parameters;

    public final void useInstances(Instances instances) {
        parameters = setupParameters(instances);
    }

    protected abstract List<IndexConsumer<?>> setupParameters(Instances instances);

    public void setCombination(int combination) {
        int[] parameterValueIndices = Utilities.fromCombination(combination, getParameterBins());
        setCombination(parameterValueIndices);
    }

    public void setCombination(int[] combination) {
        if(combination.length != parameters.size()) {
            throw new IllegalArgumentException("wrong number of parameter indices in combination, expecting " + parameters.size() + ", given: " + Utilities.toString(combination));
        }
        for(int i = 0; i < combination.length; i++) {
            parameters.get(i).accept(combination[i]);
        }
    }

    public int getNumParameters() {
        return parameters.size();
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
