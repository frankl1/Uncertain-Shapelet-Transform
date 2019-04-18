package ee.sampling;

import java.util.Random;

public class ScaledDistribution implements Distribution<Double> {
    private final Distribution<Double> distribution;
    private final double scale;

    public ScaledDistribution(Distribution<Double> distribution, double scale) {
        this.distribution = distribution;
        this.scale = scale;
    }

    @Override
    public Double sample() {
        return scale * distribution.sample();
    }

    private Random random = new Random();

    @Override
    public void setRandom(Random random) {
        distribution.setRandom(random);
        this.random = random;
    }

    @Override
    public Random getRandom() {
        return random;
    }
}

