package ee.sampling;

import java.util.Random;

public class ScaledDistribution extends Distribution<Double> {
    private final Distribution<Double> distribution;
    private final double scale;

    public ScaledDistribution(Distribution<Double> distribution, double scale) {
        this.distribution = distribution;
        this.scale = scale;
    }

    @Override
    public Double sample(final Random random) {
        return scale * distribution.sample(random);
    }

}

