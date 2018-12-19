package timeseriesweka.classifiers.ensembles.ee.Iteration;

import timeseriesweka.classifiers.ensembles.ee.Indexing.IndexedObtainer;

import java.util.Random;

public class RandomIterator<E> extends LinearIterator<E> {
    public RandomIterator(final IndexedObtainer<E> indexedObtainer) {
        super(indexedObtainer);
    }

    private final Random random = new Random();

    @Override
    protected void nextIndex() {
        setNextIndex(random.nextInt(size()));
    }

    @Override
    public void reset() {
        super.reset();
        if(seed != null) {
            setSeed(seed);
        }
    }

    private Long seed = null;

    @Override
    public void setSeed(final long seed) {
        this.seed = seed;
        random.setSeed(seed);
    }
}
