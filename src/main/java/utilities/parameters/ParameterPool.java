package utilities.parameters;

import utilities.distributions.Distribution;
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

    public void add(String name, List list) {
        putWithWarning(name, list, discreteParameterPools);
    }

    private <A> void putWithWarning(String key, A value, Map<String, A> map) {
        if(map.get(key) != null) {
            System.err.println("warning: overwriting parameters"); // todo use logger
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
            parameterPermutation.put(entry.getKey(), entry.getValue().get(indices.get(i++)));
        }
        return parameterPermutation;
    }

    public ParameterPermutation getContinuousParameterPermutation(Random random) {
        ParameterPermutation parameterPermutation = new ParameterPermutation();
        for(Map.Entry<String, Distribution> entry : continuousParameterPools.entrySet()) {
            parameterPermutation.put(entry.getKey(), entry.getValue().sample(random));
        }
        return parameterPermutation;
    }

    public ParameterPermutation getParameterPermutationFromIndexAndRandom(int index, Random random) {
        ParameterPermutation parameterPermutation = getDiscreteParameterPermutationFromIndex(index);
        parameterPermutation.putAll(getContinuousParameterPermutation(random));
        return parameterPermutation;
    }

    public int getNumDiscreteParameterPermutations() {
        return Utilities.numPermutations(getDiscreteParameterPoolSizes());
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
