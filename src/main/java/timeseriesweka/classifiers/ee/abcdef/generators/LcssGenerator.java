package timeseriesweka.classifiers.ee.abcdef.generators;

import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.classifiers.ee.abcdef.*;
import timeseriesweka.classifiers.ee.index.IndexedSupplier;
import timeseriesweka.classifiers.ee.index.LinearInterpolater;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.lcss.Lcss;
import utilities.Box;
import utilities.StatisticUtilities;
import utilities.Utilities;
import utilities.range.ValueRange;
import weka.core.Instances;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class LcssGenerator extends NnGenerator {

    private final Box<Lcss> distanceMeasureBox = new Box<>();
    private final IndexedMutator<Lcss, Double> warpingWindowParameter = new IndexedMutator<>(Lcss.WARPING_WINDOW_MUTABLE);
    private final TargetedMutator<Lcss> warpingWindowMutator = new TargetedMutator<>(warpingWindowParameter, distanceMeasureBox);
    private final IndexedMutator<Lcss, Double> costParameter = new IndexedMutator<>(Lcss.COST_MUTABLE);
    private final TargetedMutator<Lcss> costMutator = new TargetedMutator<>(costParameter, distanceMeasureBox);

    public LcssGenerator() {
        List<Indexed> parameters = getParameters().getIndexeds();
        parameters.add(costMutator);
        parameters.add(warpingWindowMutator);
    }

    @Override
    protected DistanceMeasure getDistanceMeasure() {
        distanceMeasureBox.setContents(new Lcss());
        return distanceMeasureBox.getContents();
    }

    @Override
    public void setParameterRanges(final Instances instances) {
        double pStdDev = StatisticUtilities.populationStandardDeviation(instances);
        warpingWindowParameter.getValueRange().setIndexedSupplier(new LinearInterpolater(0, 0.25, 10));
        costParameter.getValueRange().setIndexedSupplier(new LinearInterpolater(0.2 * pStdDev, pStdDev, 10));
    }
}
