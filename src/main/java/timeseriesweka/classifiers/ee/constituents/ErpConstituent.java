package timeseriesweka.classifiers.ee.constituents;

import timeseriesweka.classifiers.Classifier;
import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.classifiers.ee.index.IndexConsumer;
import timeseriesweka.classifiers.ee.range.Range;
import timeseriesweka.classifiers.ee.range.ValueRange;
import timeseriesweka.measures.erp.Erp;

import static timeseriesweka.classifiers.ee.index.LinearInterpolater.SCALED;

public class ErpConstituent extends Constituent {

    private Erp erp;
    private final ValueRange<Double> warpingWindowValueRange = new ValueRange<>(SCALED, new Range());

    public ValueRange<Double> getPenaltyValueRange() {
        return penaltyValueRange;
    }

    private final ValueRange<Double> penaltyValueRange = new ValueRange<>(SCALED, new Range());

    private final IndexConsumer<Double> warpingWindowParameter = new IndexConsumer<>(erp::setWarpingWindow, warpingWindowValueRange);
    private final IndexConsumer<Double> penaltyParameter = new IndexConsumer<>(erp::setPenalty, penaltyValueRange);

    @Override
    protected Classifier build() {
        NearestNeighbour nearestNeighbour = new NearestNeighbour();
        erp = new Erp();
        nearestNeighbour.setDistanceMeasure(erp);
        return nearestNeighbour;
    }

    @Override
    protected IndexConsumer<?>[] getParameters() {
        return new IndexConsumer<?>[] {warpingWindowParameter, penaltyParameter};
    }

    public ValueRange<Double> getWarpingWindowValueRange() {
        return warpingWindowValueRange;
    }
}
