package ee.parameter;

import ee.sampling.Distribution;
import utilities.Utilities;

import java.util.*;

public class ParameterPermutationIterator implements Iterator<ParameterPermutation> {

    private final ParameterPool pool;
    private final Random random;
    private int limit;
    private int count = 0;
    private final List<Integer> discretePoolSizes;
    private final int numDiscretePermutations;

    public ParameterPermutationIterator(ParameterPool pool, Random random, int limit) {
        this.pool = pool;
        this.random = random;
        this.limit = limit;
        if(pool.isOnlyDiscrete()) {
            discretePoolSizes = pool.getDiscreteParameterPoolSizes();
            numDiscretePermutations = Utilities.numPermutations(discretePoolSizes);
        } else {
            discretePoolSizes = null;
            numDiscretePermutations = -1;
        }
    }

    public ParameterPermutationIterator(ParameterPool pool, Random random) {
        this(pool, random, -1);
    }

    @Override
    public boolean hasNext() {
        return (limit < 0 || count < limit) && (numDiscretePermutations < 0 || count < numDiscretePermutations);
    }

    @Override
    public ParameterPermutation next() {
        ParameterPermutation parameterPermutation = new ParameterPermutation();
        if(discretePoolSizes != null) {
            Map<String, List> discreteParameterPools = pool.getDiscreteParameterPools();
            int i = 0;
            List<Integer> permutationIndices = Utilities.fromPermutation(count, discretePoolSizes);
            for(String key : discreteParameterPools.keySet()) {
                List list = discreteParameterPools.get(key);
                Object value = list.get(permutationIndices.get(i));
                parameterPermutation.addParameterValue(key, value);
                i++;
            }
        } else {
            parameterPermutation = new ParameterPermutation();
            for(String key : pool.getDiscreteParameterPools().keySet()) {
                List list = pool.getDiscreteParameterPools().get(key);
                parameterPermutation.addParameterValue(key, list.get(random.nextInt(list.size())));
            }
        }
        for(Map.Entry<String, Distribution> entry : pool.getContinuousParameterPools().entrySet()) {
            parameterPermutation.addParameterValue(entry.getKey(), entry.getValue().sample(random));
        }
        count++;
        return parameterPermutation;
    }
}
