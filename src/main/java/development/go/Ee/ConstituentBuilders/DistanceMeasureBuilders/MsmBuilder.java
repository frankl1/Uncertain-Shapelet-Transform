package development.go.Ee.ConstituentBuilders.DistanceMeasureBuilders;

import development.go.Ee.ConstituentBuilders.ConstituentBuilder;
import development.go.Indexed.Indexed;
import development.go.Indexed.IndexedValues;
import timeseriesweka.classifiers.nn.Nn;
import timeseriesweka.measures.lcss.Lcss;
import timeseriesweka.measures.msm.Msm;
import utilities.StatisticUtilities;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static utilities.Utilities.incrementalDiffList;

public class MsmBuilder extends ConstituentBuilder<Msm> {
    private final Indexed<Double> warpingWindowValues = new IndexedValues<>(Collections.singletonList(1.0));

    @Override
    public List<Integer> getDistanceMeasureParameterSizes() {
        return new ArrayList<>(Arrays.asList(penaltyValues.size(), warpingWindowValues.size()));
    }

    @Override
    public Msm getDistanceMeasure() {
        return new Msm();
    }

    @Override
    public void configureDistanceMeasure(final Msm distanceMeasure, final List<Integer> parameterPermutation) {
        double penalty = penaltyValues.apply(parameterPermutation.get(0));
        distanceMeasure.setPenalty(penalty);
        double warpingWindow = warpingWindowValues.apply(parameterPermutation.get(1));
        distanceMeasure.setWarpingWindow(warpingWindow);
    }

    private final IndexedValues<Double> penaltyValues = new IndexedValues<>(Arrays.asList(
        0.01,
        0.01375,
        0.0175,
        0.02125,
        0.025,
        0.02875,
        0.0325,
        0.03625,
        0.04,
        0.04375,
        0.0475,
        0.05125,
        0.055,
        0.05875,
        0.0625,
        0.06625,
        0.07,
        0.07375,
        0.0775,
        0.08125,
        0.085,
        0.08875,
        0.0925,
        0.09625,
        0.1,
        0.136,
        0.172,
        0.208,
        0.244,
        0.28,
        0.316,
        0.352,
        0.388,
        0.424,
        0.46,
        0.496,
        0.532,
        0.568,
        0.604,
        0.64,
        0.676,
        0.712,
        0.748,
        0.784,
        0.82,
        0.856,
        0.892,
        0.928,
        0.964,
        1d,
        1.36,
        1.72,
        2.08,
        2.44,
        2.8,
        3.16,
        3.52,
        3.88,
        4.24,
        4.6,
        4.96,
        5.32,
        5.68,
        6.04,
        6.4,
        6.76,
        7.12,
        7.48,
        7.84,
        8.2,
        8.56,
        8.92,
        9.28,
        9.64,
        10d,
        13.6,
        17.2,
        20.8,
        24.4,
        28d,
        31.6,
        35.2,
        38.8,
        42.4,
        46d,
        49.6,
        53.2,
        56.8,
        60.4,
        64d,
        67.6,
        71.2,
        74.8,
        78.4,
        82d,
        85.6,
        89.2,
        92.8,
        96.4,
        100d
    ));
}
