package ee.parameter;

import ee.iteration.RandomIndexIterator;
import ee.sampling.Distribution;
import utilities.Utilities;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RandomParameterPermutationIterator implements Iterator<ParameterPermutation> {

    private final ParameterPermutationPool pool;
    private final Random random;
    private final RandomIndexIterator discretePermutationIterator;
    private int limit;
    private int count = 0;

    public RandomParameterPermutationIterator(ParameterPermutationPool pool, Random random, int limit) {
        this.pool = pool;
        this.random = random;
        this.limit = limit;
        if(pool.isOnlyDiscrete()) {
            int numDiscretePermutations = Utilities.numPermutations(pool.getDiscreteParameterPoolSizes());
            discretePermutationIterator = new RandomIndexIterator(Utilities.naturalNumbersFromZero(numDiscretePermutations - 1), random);
        } else {
            discretePermutationIterator = null;
        }
        for(Distribution distribution : pool.getContinuousParameterPools().values()) {
            distribution.setRandom(random);
        }
    }

    public RandomParameterPermutationIterator(ParameterPermutationPool pool, Random random) {
        this(pool, random, -1);
    }

    @Override
    public boolean hasNext() {
        return (limit < 0 || count < limit) && (discretePermutationIterator == null || discretePermutationIterator.hasNext());
    }

    @Override
    public ParameterPermutation next() {
        count++;
        ParameterPermutation parameterPermutation;
        if(discretePermutationIterator != null) {
            int discretePermutationIndex = discretePermutationIterator.next();
            parameterPermutation = pool.getDiscreteParameterPermutationFromIndex(discretePermutationIndex);
        } else {
            parameterPermutation = new ParameterPermutation();
            for(Map.Entry<String, List> entry : pool.getDiscreteParameterPools().entrySet()) {
                List list = entry.getValue();
                parameterPermutation.addParameterValue(entry.getKey(), list.get(random.nextInt(list.size())));
            }
        }
        for(Map.Entry<String, Distribution> entry : pool.getContinuousParameterPools().entrySet()) {
            parameterPermutation.addParameterValue(entry.getKey(), entry.getValue().sample());
        }
        return parameterPermutation;
    }
}
