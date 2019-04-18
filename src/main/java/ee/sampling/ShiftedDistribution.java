package ee.sampling;

import java.util.Random;

public class ShiftedDistribution implements Distribution<Double> {
    private final ScaledDistribution scaledDistribution;
    private final double min;

    public ShiftedDistribution(Distribution<Double> distribution, double min, double max) {
        scaledDistribution = new ScaledDistribution(distribution, max - min);
        this.min = min;
    }

    @Override
    public Double sample() {
        return min + scaledDistribution.sample();
    }

    private Random random = new Random();

    @Override
    public void setRandom(Random random) {
        scaledDistribution.setRandom(random);
        this.random = random;
    }

    @Override
    public Random getRandom() {
        return random;
    }
}
