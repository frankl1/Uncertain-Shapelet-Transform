package timeseriesweka.classifiers.ee.constituents;

import timeseriesweka.classifiers.Classifier;
import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.classifiers.ee.index.IndexConsumer;
import timeseriesweka.classifiers.ee.range.Range;
import timeseriesweka.classifiers.ee.range.ValueRange;
import timeseriesweka.measures.ddtw.Ddtw;
import timeseriesweka.measures.dtw.Dtw;

import static timeseriesweka.classifiers.ee.index.LinearInterpolater.SCALED;

public class DdtwConstituent extends Constituent {
    public ValueRange<Double> getWarpingWindowValueRange() {
        return warpingWindowValueRange;
    }

    private final ValueRange<Double> warpingWindowValueRange = new ValueRange<>(SCALED, new Range());

    private Ddtw ddtw;
    private final IndexConsumer<Double> warpingWindowParameter = new IndexConsumer<>(ddtw::setWarpingWindow, warpingWindowValueRange);

    @Override
    protected Classifier build() {
        NearestNeighbour nearestNeighbour = new NearestNeighbour();
        ddtw = new Ddtw();
        nearestNeighbour.setDistanceMeasure(ddtw);
        return nearestNeighbour;
    }

    @Override
    protected IndexConsumer<?>[] getParameters() {
        return new IndexConsumer<?>[]{warpingWindowParameter};
    }
}
