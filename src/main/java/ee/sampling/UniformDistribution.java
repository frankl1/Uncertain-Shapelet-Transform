package ee.sampling;

import ee.Randomised;

import java.util.Random;

public class UniformDistribution implements Distribution<Double>, Randomised {

    @Override
    public Double sample() {
        return getRandom().nextDouble();
    }

    @Override
    public void setRandom(Random random) {
        this.random = random;
    }

    private Random random = new Random();

    @Override
    public Random getRandom() {
        return random;
    }
}
