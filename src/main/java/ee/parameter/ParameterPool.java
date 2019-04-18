package ee.parameter;

import ee.Randomised;
import ee.iteration.Indexed;
import ee.sampling.Distribution;
import utilities.Utilities;

import java.util.*;

public class ParameterPool {
    private final Map<String, List> discreteParameterPools = new TreeMap<>();
    private final Map<String, Distribution> continuousParameterPools = new TreeMap<>();

    public Map<String, List> getDiscreteParameterPools() {
        return discreteParameterPools;
    }

    public Map<String, Distribution> getContinuousParameterPools() {
        return continuousParameterPools;
    }

    public void add(String name, List parameterPool) {
        putWithWarning(name, parameterPool, discreteParameterPools);
    }

    private <A> void putWithWarning(String key, A value, Map<String, A> map) {
        if(map.get(key) != null) {
            System.err.println("warning: overwriting parameter");
        }
        map.put(key, value);
    }

    public void add(String name, Distribution parameterPool) {
        putWithWarning(name, parameterPool, continuousParameterPools);
    }

    public ParameterPermutation getDiscreteParameterPermutationFromIndex(int index) {
        ParameterPermutation parameterPermutation = new ParameterPermutation();
        List<Integer> indices = Utilities.fromPermutation(index, getDiscreteParameterPoolSizes());
        int i = 0;
        for(Map.Entry<String, List> entry : discreteParameterPools.entrySet()) {
            parameterPermutation.addParameterValue(entry.getKey(), entry.getValue().get(indices.get(i++)));
        }
        return parameterPermutation;
    }

    public List<Integer> getDiscreteParameterPoolSizes() {
        List<Integer> sizes = new ArrayList<>();
        for(String key : discreteParameterPools.keySet()) {
            sizes.add(discreteParameterPools.get(key).size());
        }
        return sizes;
    }

    public int numDiscreteParameterPermutations() {
        if(discreteParameterPools.isEmpty()) {
            return 0;
        }
        return Utilities.numPermutations(getDiscreteParameterPoolSizes());
    }

    public boolean isOnlyDiscrete() {
        return continuousParameterPools.isEmpty() && !discreteParameterPools.isEmpty();
    }

    public boolean isOnlyContinuous() {
        return !continuousParameterPools.isEmpty() && discreteParameterPools.isEmpty();
    }

    public boolean isEmpty() {
        return continuousParameterPools.isEmpty() && discreteParameterPools.isEmpty();
    }

    public boolean containsDiscrete() {
        return !discreteParameterPools.isEmpty();
    }

    public boolean containsContinuous() {
        return !continuousParameterPools.isEmpty();
    }

}
