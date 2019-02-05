package timeseriesweka.classifiers.ee.iteration;

import java.util.*;

public class RandomIndexIterator extends LinearIndexIterator {
    private Random random = new Random();
    private Long seed = null;

    public Random getRandom() {
        return random;
    }

    public void setRandom(final Random random) {
        this.random = random;
        reset();
    }

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
    protected int nextIndex() {
        index = random.nextInt(workingRange.size());
        return index;
    }

    @Override
    public void resetPostRange() {
        if(seed != null) random.setSeed(seed);
    }
}
