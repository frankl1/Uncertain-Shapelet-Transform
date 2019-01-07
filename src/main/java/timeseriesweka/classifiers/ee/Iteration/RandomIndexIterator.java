package timeseriesweka.classifiers.ee.Iteration;

import timeseriesweka.classifiers.ee.Range;

import java.util.*;

public class RandomIndexIterator extends LinearIndexIterator {
    private final Random random = new Random();
    private Long seed = null;

    @Override
    public void setSeed(final long seed) {
        this.seed = seed;
        reset();
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Integer next() {
        index = random.nextInt(workingRange.size());
        return workingRange.get(index);
    }

    @Override
    public void resetPostRange() {
        random.setSeed(seed);
    }
}
