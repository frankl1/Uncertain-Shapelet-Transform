package ee.dump.DistanceMeasures.erp;

import ee.sampling.Distribution;
import ee.sampling.ScaledDistribution;
import ee.sampling.UniformDistribution;
import weka.core.Instances;

import java.util.function.Function;

public class ErpUniformRandomDeltaGenerator implements Function<Instances, Distribution<Double>> {
    @Override
    public Distribution<Double> apply(Instances instances) {
        int length = (instances.numAttributes() - 1) / 4 + 1;
        return new ScaledDistribution(new UniformDistribution(), length);
    }
}
