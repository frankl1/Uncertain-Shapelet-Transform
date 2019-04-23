package ee.sampling;

import ee.Randomised;

import java.util.Random;

public interface Distribution<A> {
    A sample(Random random);
}
