package timeseriesweka.classifiers.ee.abcdef.generators;

import timeseriesweka.classifiers.ee.abcdef.Indexed;
import timeseriesweka.classifiers.ee.abcdef.IndexedMutator;
import timeseriesweka.classifiers.ee.abcdef.TargetedMutator;
import timeseriesweka.classifiers.ee.index.ElementObtainer;
import timeseriesweka.classifiers.ee.index.LinearInterpolater;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.msm.Msm;
import utilities.Box;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MsmGenerator extends NnGenerator {

    private final Box<Msm> distanceMeasureBox = new Box<>();
    private final IndexedMutator<Msm, Double> warpingWindowParameter = new IndexedMutator<>(Msm.WARPING_WINDOW_MUTABLE);
    private final TargetedMutator<Msm> warpingWindowMutator = new TargetedMutator<>(warpingWindowParameter, distanceMeasureBox);
    private final IndexedMutator<Msm, Double> costParameter = new IndexedMutator<>(Msm.COST_MUTABLE);
    private final TargetedMutator<Msm> costMutator = new TargetedMutator<>(costParameter, distanceMeasureBox);

    public MsmGenerator() {
        List<Indexed> parameters = getParameters().getIndexeds();
        parameters.add(warpingWindowMutator);
        parameters.add(costMutator);
    }

    @Override
    protected DistanceMeasure getDistanceMeasure() {
        distanceMeasureBox.setContents(new Msm());
        return distanceMeasureBox.getContents();
    }

    @Override
    public void setParameterRanges(final Instances instances) {
        ElementObtainer<Double> elementObtainer = new ElementObtainer<>(COST_VALUES);
        costParameter.getValueRange().setIndexedSupplier(elementObtainer);
        warpingWindowParameter.getValueRange().setIndexedSupplier(new LinearInterpolater(1, 1, 1));
    }

    private static final List<Double> COST_VALUES = new ArrayList<>(Arrays.asList(// <editor-fold defaultstate="collapsed" desc="hidden for space">
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
        1.0,
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
        10.0,
        13.6,
        17.2,
        20.8,
        24.4,
        28.0,
        31.6,
        35.2,
        38.8,
        42.4,
        46.0,
        49.6,
        53.2,
        56.8,
        60.4,
        64.0,
        67.6,
        71.2,
        74.8,
        78.4,
        82.0,
        85.6,
        89.2,
        92.8,
        96.4,
        100.0// </editor-fold>
    ));
}
