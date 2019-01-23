package timeseriesweka.classifiers.ee.constituents.generators;

import timeseriesweka.classifiers.ee.constituents.Indexed;
import timeseriesweka.classifiers.ee.constituents.IndexedMutator;
import timeseriesweka.classifiers.ee.constituents.TargetedMutator;
import timeseriesweka.classifiers.ee.index.LinearInterpolater;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.erp.Erp;
import utilities.Box;
import utilities.StatisticUtilities;
import weka.core.Instances;

import java.util.List;

public class ErpGenerator extends NnGenerator {
    private final Box<Erp> distanceMeasureBox = new Box<>();
    private final IndexedMutator<Erp, Double> warpingWindowParameter = new IndexedMutator<>(Erp.WARPING_WINDOW_MUTABLE);
    private final TargetedMutator<Erp> warpingWindowMutator = new TargetedMutator<>(warpingWindowParameter, distanceMeasureBox);
    private final IndexedMutator<Erp, Double> penaltyParameter = new IndexedMutator<>(Erp.PENALTY_MUTABLE);
    private final TargetedMutator<Erp> penaltyMutator = new TargetedMutator<>(penaltyParameter, distanceMeasureBox);

    public ErpGenerator() {
        List<Indexed> parameters = getParameters().getIndexeds();
        parameters.add(warpingWindowMutator);
        parameters.add(penaltyMutator);
    }

    @Override
    protected DistanceMeasure getDistanceMeasure() {
        distanceMeasureBox.setContents(new Erp());
        return distanceMeasureBox.getContents();
    }

    @Override
    public void setParameterRanges(final Instances instances) {
        double pStdDev = StatisticUtilities.populationStandardDeviation(instances);
        warpingWindowParameter.getValueRange().setIndexedSupplier(new LinearInterpolater(0, 0.25, 10));
        penaltyParameter.getValueRange().setIndexedSupplier(new LinearInterpolater(0.2 * pStdDev, pStdDev, 10));
    }
}
