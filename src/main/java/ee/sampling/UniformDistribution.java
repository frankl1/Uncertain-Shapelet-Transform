package ee.sampling;

import ee.Randomised;

import java.util.Random;

public class UniformDistribution implements Distribution<Double> {

    @Override
    public Double sample(Random random) {
        return random.nextDouble();
    }
}
