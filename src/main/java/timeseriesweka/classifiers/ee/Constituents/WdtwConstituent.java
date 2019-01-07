package timeseriesweka.classifiers.ee.Constituents;

import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.classifiers.ee.*;
import timeseriesweka.measures.dtw.Dtw;
import timeseriesweka.measures.wdtw.Wdtw;
import weka.classifiers.Classifier;

import static timeseriesweka.classifiers.ee.LinearInterpolater.SCALED;

public class WdtwConstituent extends Constituent {

    private final ValueRange<Double> warpingWindowValueRange = new ValueRange<>(SCALED, new Range());
    private final ValueRange<Double> weightValueRange = new ValueRange<>(SCALED, new Range());

    public ValueRange<Double> getWarpingWindowValueRange() {
        return warpingWindowValueRange;
    }

    public ValueRange<Double> getWeightValueRange() {
        return weightValueRange;
    }

    private Wdtw wdtw;
    private final Parameter<Double> warpingWindowParameter = new Parameter<>(wdtw::setWarpingWindow, warpingWindowValueRange);
    private final Parameter<Double> weightParameter = new Parameter<>(wdtw::setWeight, weightValueRange);

    @Override
    protected Classifier build() {
        NearestNeighbour nearestNeighbour = new NearestNeighbour();
        wdtw = new Wdtw();
        nearestNeighbour.setDistanceMeasure(wdtw);
        return nearestNeighbour;
    }

    @Override
    protected Parameter<?>[] getParameters() {
        return new Parameter<?>[]{warpingWindowParameter, weightParameter};
    }
}
