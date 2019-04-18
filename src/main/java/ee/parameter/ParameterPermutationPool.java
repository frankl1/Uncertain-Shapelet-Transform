package ee.parameter;

import ee.Randomised;
import ee.iteration.Indexed;
import ee.sampling.Distribution;
import utilities.Utilities;

import java.util.*;

public class ParameterPermutationPool {
    private final Map<String, List> discreteParameterPools = new TreeMap<>();

    public Map<String, List> getDiscreteParameterPools() {
        return discreteParameterPools;
    }

    public Map<String, Distribution> getContinuousParameterPools() {
        return continuousParameterPools;
    }

    private final Map<String, Distribution> continuousParameterPools = new TreeMap<>();

    private <A> void putWithWarning(String key, A value, Map<String, A> map) {
        if(map.get(key) != null) {
            System.err.println("warning: overwriting parameter");
        }
        map.put(key, value);
    }

    public void addParameterPool(String name, List parameterPool) {
        putWithWarning(name, parameterPool, discreteParameterPools);
    }

    public void addParameterPool(String name, Distribution parameterPool) {
        putWithWarning(name, parameterPool, continuousParameterPools);
    }

    public List<Integer> getDiscreteParameterPoolSizes() {
        List<Integer> sizes = new ArrayList<>();
        for(Map.Entry<String, List> entry : discreteParameterPools.entrySet()) {
            sizes.add(entry.getValue().size());
        }
        return sizes;
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

//    @Override
//    public int size() {
//        if(!continuousParameterPools.isEmpty()) {
//            return -1;
//        } else {
//            List<Integer> discreteParameterPoolSizes = getDiscreteParameterPoolSizes(); // todo zero params?
//            int size = Utilities.numPermutations(discreteParameterPoolSizes);
//            return size;
//        }
//    }
//
//    @Override
//    public ParameterPermutation get(int index) {
//        if(index < 0 && !discreteParameterPools.isEmpty() && continuousParameterPools.isEmpty()) {
//            throw new IllegalArgumentException("invalid index " + index + " to obtain value from discrete parameter pool(s)");
//        }
//        if(index >= 0 && !continuousParameterPools.isEmpty() && discreteParameterPools.isEmpty()) {
//            throw new IllegalArgumentException("invalid index " + index + " to obtain value from continuous parameter pool(s)");
//        }
//        ParameterPermutation parameterPermutation = new ParameterPermutation();
//        if(!discreteParameterPools.isEmpty()) {
//            // feed index into permutation of discrete parameters
//            List<Integer> sizes = getDiscreteParameterPoolSizes();
//            List<Integer> indices;
//            if(index < 0) {
//                indices = new ArrayList<>();
//                for(Integer size : sizes) {
//                    indices.add(index);
//                }
//            } else {
//                indices = Utilities.fromPermutation(index, sizes);
//            }
//            int i = 0;
//            for(Map.Entry<String, List> entry : discreteParameterPools.entrySet()) {
//                int valueIndex = indices.get(i++);
//                List parameterPool = entry.getValue();
//                Object value = parameterPool.get(valueIndex);
//                parameterPermutation.addParameterValue(entry.getKey(), value);
//            }
//        }
//        for(Map.Entry<String, Distribution> entry : continuousParameterPools.entrySet()) {
//            parameterPermutation.addParameterValue(entry.getKey(), entry.getValue().sample());
//        }
//        return parameterPermutation;
//    }

}
