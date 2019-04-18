package ee.sampling;

import ee.Randomised;

public interface Distribution<A> extends Randomised {
    A sample();
}
