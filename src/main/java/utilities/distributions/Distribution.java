package utilities.distributions;

import java.util.Random;

public interface Distribution<A> {
    A sample(Random random);
}
