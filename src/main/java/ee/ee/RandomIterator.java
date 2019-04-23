package ee.ee;


import ee.parameter.ParameterPermutation;
import ee.parameter.ParameterPool;
import utilities.Utilities;

import java.util.*;

public class RandomIterator implements Iterator<String[]> {

    private final Ee ee;
    private final Map<ParameterPool, List<Integer>> indicesMap = new HashMap<>();


    public RandomIterator(Ee ee) {
        this.ee = ee;
        for(ParameterPool parameterPool : ee.parameterPools) {
            indicesMap.put(parameterPool, Utilities.naturalNumbersFromZero(parameterPool.getNumDiscreteParameterPermutations() - 1));
        }
    }

    @Override
    public boolean hasNext() {
        return !ee.parameterPools.isEmpty();
    }

    @Override
    public String[] next() { // todo deal with pool already empty
        Random random = ee.getRandom();
        int parameterPoolIndex = random.nextInt(ee.parameterPools.size());
        ParameterPool parameterPool = ee.parameterPools.get(parameterPoolIndex);
        List<Integer> indices = indicesMap.get(parameterPool);
        int index = indices.remove(random.nextInt(indices.size()));
        ParameterPermutation parameterPermutation = parameterPool.getParameterPermutationFromIndexAndRandom(index, random);
        if(indices.isEmpty()) {
            ee.parameterPools.remove(parameterPoolIndex);
            indicesMap.remove(parameterPool);
        }
        return parameterPermutation.getOptions();
    }
}
