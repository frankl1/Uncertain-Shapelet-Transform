package development.go.Ee.ConstituentBuilders.DistanceMeasureBuilders;

import development.go.Ee.ConstituentBuilders.ConstituentBuilder;
import development.go.Indexed.Indexed;
import development.go.Indexed.IndexedValues;
import timeseriesweka.classifiers.nn.Nn;
import timeseriesweka.measures.lcss.Lcss;
import utilities.StatisticUtilities;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static utilities.Utilities.incrementalDiffList;

public class LcssBuilder extends ConstituentBuilder<Lcss> {

    private Indexed<Double> toleranceValues;
    private final Indexed<Double> warpingWindowValues = new IndexedValues<>(incrementalDiffList(0, 0.25, 10));

    @Override
    public List<Integer> getDistanceMeasureParameterSizes() {
        return new ArrayList<>(Arrays.asList(toleranceValues.size(), warpingWindowValues.size()));
    }

    @Override
    public void setUpParameters(final Instances instances) {
        double maxTolerance = StatisticUtilities.populationStandardDeviation(instances);
        double minTolerance = maxTolerance * 0.2;
        toleranceValues = new IndexedValues<>(incrementalDiffList(minTolerance, maxTolerance, 10));
    }

    @Override
    public Lcss getDistanceMeasure() {
        return new Lcss();
    }

    @Override
    public void configureDistanceMeasure(final Lcss lcss, final List<Integer> parameterPermutation) {
        double tolerance = toleranceValues.apply(parameterPermutation.get(0));
        lcss.setTolerance(tolerance);
        double warpingWindow = warpingWindowValues.apply(parameterPermutation.get(1));
        lcss.setWarpingWindow(warpingWindow);
    }
}
