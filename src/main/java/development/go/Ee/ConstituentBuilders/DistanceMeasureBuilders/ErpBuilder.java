package development.go.Ee.ConstituentBuilders.DistanceMeasureBuilders;

import development.go.Ee.ConstituentBuilders.ConstituentBuilder;
import development.go.Indexed.Indexed;
import development.go.Indexed.IndexedValues;
import timeseriesweka.measures.erp.Erp;
import utilities.StatisticUtilities;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static utilities.Utilities.incrementalDiffList;

public class ErpBuilder extends ConstituentBuilder<Erp> {
    private Indexed<Double> penaltyValues;
    private final Indexed<Double> warpingWindowValues = new IndexedValues<>(incrementalDiffList(0, 0.25, 10)); // todo overhaul to lin interp

    @Override
    public List<Integer> getDistanceMeasureParameterSizes() {
        return new ArrayList<>(Arrays.asList(penaltyValues.size(), warpingWindowValues.size()));
    }

    @Override
    public void setUpParameters(final Instances instances) {
        double maxTolerance = StatisticUtilities.populationStandardDeviation(instances);
        double minTolerance = maxTolerance * 0.2;
        penaltyValues = new IndexedValues<>(incrementalDiffList(minTolerance, maxTolerance, 10));
    }

    @Override
    public Erp getDistanceMeasure() {
        return new Erp();
    }

    @Override
    public void configureDistanceMeasure(final Erp distanceMeasure, final List<Integer> parameterPermutation) {
        double penalty = penaltyValues.apply(parameterPermutation.get(0));
        distanceMeasure.setPenalty(penalty);
        double warpingWindow = warpingWindowValues.apply(parameterPermutation.get(1));
        distanceMeasure.setWarpingWindow(warpingWindow);
    }
}
