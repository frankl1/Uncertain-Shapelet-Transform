package timeseriesweka.classifiers.ee.Constituents;

import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.classifiers.ee.*;
import timeseriesweka.measures.dtw.Dtw;
import timeseriesweka.measures.lcss.Lcss;
import weka.classifiers.Classifier;

import static timeseriesweka.classifiers.ee.LinearInterpolater.SCALED;

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

    private final Parameter<Double> warpingWindowParameter = new Parameter<>(lcss::setWarpingWindow, warpingWindowValueRange);
    private final Parameter<Double> toleranceParameter = new Parameter<>(lcss::setTolerance, toleranceValueRange);

    @Override
    protected Classifier build() {
        NearestNeighbour nearestNeighbour = new NearestNeighbour();
        lcss = new Lcss();
        nearestNeighbour.setDistanceMeasure(lcss);
        return nearestNeighbour;
    }

    @Override
    protected Parameter<?>[] getParameters() {
        return new Parameter<?>[]{warpingWindowParameter, toleranceParameter};
    }
}
