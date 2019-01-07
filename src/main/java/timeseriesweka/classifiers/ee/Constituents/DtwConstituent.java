package timeseriesweka.classifiers.ee.Constituents;

import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.classifiers.ee.IndexedObtainer;
import timeseriesweka.classifiers.ee.Parameter;
import timeseriesweka.classifiers.ee.Range;
import timeseriesweka.classifiers.ee.ValueRange;
import timeseriesweka.measures.dtw.Dtw;
import weka.classifiers.Classifier;

import static timeseriesweka.classifiers.ee.LinearInterpolater.SCALED;

public class DtwConstituent extends Constituent {

    public ValueRange<Double> getWarpingWindowValueRange() {
        return warpingWindowValueRange;
    }

    private final ValueRange<Double> warpingWindowValueRange = new ValueRange<>(SCALED, new Range());

    private Dtw dtw;
    private final Parameter<Double> warpingWindowParameter = new Parameter<>(dtw::setWarpingWindow, warpingWindowValueRange);

    @Override
    protected Classifier build() {
        NearestNeighbour nearestNeighbour = new NearestNeighbour();
        dtw = new Dtw();
        nearestNeighbour.setDistanceMeasure(dtw);
        return nearestNeighbour;
    }

    @Override
    protected Parameter<?>[] getParameters() {
        return new Parameter<?>[]{warpingWindowParameter};
    }
}
