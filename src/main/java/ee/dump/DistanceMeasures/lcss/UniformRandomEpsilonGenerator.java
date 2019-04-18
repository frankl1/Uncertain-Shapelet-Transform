package ee.dump.DistanceMeasures.lcss;

import ee.sampling.Distribution;
import ee.sampling.ShiftedDistribution;
import ee.sampling.UniformDistribution;
import utilities.StatisticUtilities;
import weka.core.Instances;

import java.util.function.Function;

public class UniformRandomEpsilonGenerator implements Function<Instances, Distribution<Double>> {
    @Override
    public Distribution<Double> apply(Instances instances) {
        double stdp = StatisticUtilities.populationStandardDeviation(instances);
        double minTolerance = 0.2 * stdp;
        double maxTolerance = stdp;
        return new ShiftedDistribution(new UniformDistribution(), minTolerance, maxTolerance);
    }
}
