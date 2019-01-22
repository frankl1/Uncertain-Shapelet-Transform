package timeseriesweka.classifiers.ee.iteration;

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
        return workingRange.size() > 0;
    }

    @Override
    public Integer next() {
        index = random.nextInt(workingRange.size());
        return workingRange.get(index);
    }

    @Override
    public void resetPostRange() {
        if(seed != null) random.setSeed(seed);
    }
}
