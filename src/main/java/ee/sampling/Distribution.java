package ee.sampling;

import java.util.Random;

public interface Distribution<A> {
    A sample(Random random);
}
