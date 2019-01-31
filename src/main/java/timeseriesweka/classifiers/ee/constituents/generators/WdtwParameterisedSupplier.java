package timeseriesweka.classifiers.ee.constituents.generators;

import timeseriesweka.classifiers.ee.constituents.Indexed;
import timeseriesweka.classifiers.ee.constituents.IndexedMutator;
import timeseriesweka.classifiers.ee.constituents.TargetedMutator;
import timeseriesweka.classifiers.ee.index.LinearInterpolater;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;
import timeseriesweka.measures.wdtw.Wdtw;
import utilities.Box;
import utilities.StatisticUtilities;
import weka.core.Instances;

import java.util.List;

public class WdtwParameterisedSupplier extends ParameterisedSupplier<Wdtw> {

    private final IndexedMutator<Dtw, Double> warpingWindowParameter = new IndexedMutator<>(Dtw.WARPING_WINDOW_MUTABLE);
    private final TargetedMutator<Dtw> warpingWindowMutator = new TargetedMutator<>(warpingWindowParameter, getBox());
    private final IndexedMutator<Wdtw, Double> weightParameter = new IndexedMutator<>(Wdtw.WEIGHT_MUTABLE);
    private final TargetedMutator<Wdtw> weightMutator = new TargetedMutator<>(weightParameter, getBox());

    public WdtwParameterisedSupplier() {
        List<Indexed> parameters = getParameters().getIndexeds();
        parameters.add(warpingWindowMutator);
        parameters.add(weightMutator);
    }

    @Override
    protected Wdtw get() {
        return new Wdtw();
    }

    @Override
    public void setParameterRanges(final Instances instances) {
        warpingWindowParameter.getValueRange().setIndexedSupplier(new LinearInterpolater(1, 1, 1));
        weightParameter.getValueRange().setIndexedSupplier(new LinearInterpolater(0.01, 1, 100));
    }

}
