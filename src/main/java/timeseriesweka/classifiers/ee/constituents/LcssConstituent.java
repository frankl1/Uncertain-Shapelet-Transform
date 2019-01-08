package timeseriesweka.classifiers.ee.constituents;

import timeseriesweka.classifiers.Classifier;
import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.classifiers.ee.index.IndexConsumer;
import timeseriesweka.classifiers.ee.range.Range;
import timeseriesweka.classifiers.ee.range.ValueRange;
import timeseriesweka.measures.lcss.Lcss;

import static timeseriesweka.classifiers.ee.index.LinearInterpolater.SCALED;

public class LcssConstituent extends Constituent {
    private final ValueRange<Double> warpingWindowValueRange = new ValueRange<>(SCALED, new Range());
    private final ValueRange<Double> toleranceValueRange = new ValueRange<>(SCALED, new Range());
    private Lcss lcss;

    public ValueRange<Double> getWarpingWindowValueRange() {
        return warpingWindowValueRange;
    }

    public ValueRange<Double> getToleranceValueRange() {
        return toleranceValueRange;
    }

    private final IndexConsumer<Double> warpingWindowParameter = new IndexConsumer<>(lcss::setWarpingWindow, warpingWindowValueRange);
    private final IndexConsumer<Double> toleranceParameter = new IndexConsumer<>(lcss::setTolerance, toleranceValueRange);

    @Override
    protected Classifier build() {
        NearestNeighbour nearestNeighbour = new NearestNeighbour();
        lcss = new Lcss();
        nearestNeighbour.setDistanceMeasure(lcss);
        return nearestNeighbour;
    }

    @Override
    protected IndexConsumer<?>[] getParameters() {
        return new IndexConsumer<?>[]{warpingWindowParameter, toleranceParameter};
    }
}
