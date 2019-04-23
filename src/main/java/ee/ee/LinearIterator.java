package ee.ee;

import ee.parameter.ParameterPermutation;
import ee.parameter.ParameterPool;
import utilities.Utilities;

import java.util.Iterator;

public class LinearIterator implements Iterator<String[]> {
    private int index = 0;
    private final Ee ee;

    public LinearIterator(final Ee ee) {

        this.ee = ee;
    }

    @Override
    public boolean hasNext() {
        return !ee.parameterPools.isEmpty();
    }

    @Override
    public String[] next() { // todo deal with pool already empty
        ParameterPool parameterPool = ee.parameterPools.get(0);
        ParameterPermutation parameterPermutation = parameterPool.getParameterPermutationFromIndexAndRandom(index, random);
        index++;
        if(index >= Utilities.numPermutations(parameterPool.getDiscreteParameterPoolSizes())) {
            index = 0;
            ee.parameterPools.remove(0);
        }
        return parameterPermutation.getOptions();
    }
}
