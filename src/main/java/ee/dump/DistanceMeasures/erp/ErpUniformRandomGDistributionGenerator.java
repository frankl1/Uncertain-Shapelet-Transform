package ee.dump.DistanceMeasures.erp;

import ee.sampling.Distribution;
import ee.sampling.ShiftedDistribution;
import ee.sampling.UniformDistribution;
import utilities.StatisticUtilities;
import weka.core.Instances;

import java.util.function.Function;

public class ErpUniformRandomGDistributionGenerator implements Function<Instances, Distribution<Double>> {
    @Override
    public Distribution<Double> apply(Instances instances) {
        double stdp = StatisticUtilities.populationStandardDeviation(instances);
        double min = 0.2 * stdp;
        double max = stdp;
        return new ShiftedDistribution(new UniformDistribution(), min, max);
    }
}
