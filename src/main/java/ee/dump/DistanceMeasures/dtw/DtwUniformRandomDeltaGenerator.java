package ee.dump.DistanceMeasures.dtw;

import ee.sampling.Distribution;
import ee.sampling.ScaledDistribution;
import ee.sampling.UniformDistribution;
import weka.core.Instances;

import java.util.function.Function;

public class DtwUniformRandomDeltaGenerator implements Function<Instances, Distribution<Double>> {

    @Override
    public Distribution<Double> apply(Instances instances) {
        int length = instances.numAttributes() - 1;
        return new ScaledDistribution(new UniformDistribution(), length);
    }
}
