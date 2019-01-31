package timeseriesweka.classifiers.ee.constituents.generators;

import timeseriesweka.classifiers.ee.constituents.*;
import timeseriesweka.classifiers.ee.index.LinearInterpolater;
import timeseriesweka.measures.lcss.Lcss;
import utilities.StatisticUtilities;
import weka.core.Instances;

import java.util.List;

public class LcssParameterisedSupplier extends ParameterisedSupplier<Lcss> {

    private final IndexedMutator<Lcss, Double> warpingWindowParameter = new IndexedMutator<>(Lcss.WARPING_WINDOW_MUTABLE);
    private final TargetedMutator<Lcss> warpingWindowMutator = new TargetedMutator<>(warpingWindowParameter, getBox());
    private final IndexedMutator<Lcss, Double> costParameter = new IndexedMutator<>(Lcss.COST_MUTABLE);
    private final TargetedMutator<Lcss> costMutator = new TargetedMutator<>(costParameter, getBox());

    public LcssParameterisedSupplier() {
        List<Indexed> parameters = getParameters().getIndexeds();
        parameters.add(costMutator);
        parameters.add(warpingWindowMutator);
    }

    @Override
    public void setParameterRanges(final Instances instances) {
        double pStdDev = StatisticUtilities.populationStandardDeviation(instances);
        warpingWindowParameter.getValueRange().setIndexedSupplier(new LinearInterpolater(0, 0.25, 10));
        costParameter.getValueRange().setIndexedSupplier(new LinearInterpolater(0.2 * pStdDev, pStdDev, 10));
    }

    @Override
    protected Lcss get() {
        return new Lcss();
    }
}
