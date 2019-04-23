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
    public Double sample(Random random) {
        return min + scaledDistribution.sample(random);
    }
}
