package ee.sampling;

import ee.Randomised;

import java.util.Random;

public abstract class Distribution<A> {
    public A sample() {
        return sample(new Random());
    }

    public abstract A sample(Random random);

}
