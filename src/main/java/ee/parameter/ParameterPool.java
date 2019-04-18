package ee.parameter;

import ee.Randomised;
import ee.iteration.Indexed;

import java.util.Random;

public abstract class ParameterPool<A> implements Randomised {

    private Random random = new Random();

    @Override
    public void setRandom(final Random random) {
        this.random = random;
    }

    @Override
    public Random getRandom() {
        return random;
    }
}
