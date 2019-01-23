package timeseriesweka.classifiers.ee.constituents;

import timeseriesweka.classifiers.Classifier;
import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.classifiers.ee.index.IndexConsumer;
import utilities.range.Range;
import utilities.range.ValueRange;
import timeseriesweka.measures.dtw.Dtw;

import static timeseriesweka.classifiers.ee.index.LinearInterpolater.SCALED;

public class DtwConstituent extends Constituent {

    public ValueRange<Double> getWarpingWindowValueRange() {
        return warpingWindowValueRange;
    }

    private final ValueRange<Double> warpingWindowValueRange = new ValueRange<>(SCALED, new Range());

    private Dtw dtw;
    private final IndexConsumer<Double> warpingWindowParameter = new IndexConsumer<>(dtw::setWarpingWindow, warpingWindowValueRange);

    @Override
    protected Classifier build() {
        NearestNeighbour nearestNeighbour = new NearestNeighbour();
        dtw = new Dtw();
        nearestNeighbour.setDistanceMeasure(dtw);
        return nearestNeighbour;
    }

    @Override
    protected IndexConsumer<?>[] getParameters() {
        return new IndexConsumer<?>[]{warpingWindowParameter};
    }
}
