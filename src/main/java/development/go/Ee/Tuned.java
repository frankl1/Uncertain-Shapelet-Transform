package development.go.Ee;

import development.go.Ee.ParameterIteration.RandomIterator;
import development.go.Ee.ParameterIteration.SourcedIterator;
import evaluation.tuning.ParameterSpace;
import utilities.Utilities;

import java.util.Iterator;
import java.util.List;

public class Tuned extends AbstractTuned {
    private SourcedIterator<Integer, List<Integer>> iterator = new RandomIterator<>();

    public ParameterSpace getParameterSpace() {
        return parameterSpace;
    }

    @Override
    protected Iterator<Integer> getParameterPermutationIterator(int seed) {
        iterator.setSource(Utilities.naturalNumbersFromZero(size()));
        iterator.setSeed(seed);
        return iterator;
    }
}
