package ee.sampling;

import ee.Randomised;

import java.util.Iterator;
import java.util.Random;

public class DistributionRandomSamplerWithReplacement<A> implements Iterator<A>, Randomised {

    private final Distribution<A> distribution;

    public DistributionRandomSamplerWithReplacement(Distribution<A> distribution) {
        this.distribution = distribution;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public A next() {
        return distribution.sample();
    }

    private Random random = new Random();

    @Override
    public void setRandom(Random random) {
        this.random = random;
        distribution.setRandom(random);
    }

    @Override
    public Random getRandom() {
        return random;
    }
}
