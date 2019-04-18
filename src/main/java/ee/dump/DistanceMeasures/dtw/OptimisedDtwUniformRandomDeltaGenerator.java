package ee.dump.DistanceMeasures.dtw;

import ee.sampling.Distribution;
import ee.sampling.ScaledDistribution;
import ee.sampling.UniformDistribution;
import weka.core.Instances;

import java.util.function.Function;

public class OptimisedDtwUniformRandomDeltaGenerator implements Function<Instances, Distribution<Double>> {

    @Override
    public Distribution<Double> apply(Instances instances) {
        int length = instances.numAttributes() - 1;
        length = (length + 1) / 4; // optimisation according to pf
        return new ScaledDistribution(new UniformDistribution(), length);
    }
}
