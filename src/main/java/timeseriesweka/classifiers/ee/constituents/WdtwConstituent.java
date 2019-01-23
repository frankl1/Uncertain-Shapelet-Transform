package timeseriesweka.classifiers.ee.constituents;

import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.classifiers.Classifier;
import timeseriesweka.classifiers.ee.index.IndexConsumer;
import utilities.range.Range;
import utilities.range.ValueRange;
import timeseriesweka.measures.wdtw.Wdtw;

import static timeseriesweka.classifiers.ee.index.LinearInterpolater.SCALED;

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
    private final IndexConsumer<Double> warpingWindowParameter = new IndexConsumer<>(wdtw::setWarpingWindow, warpingWindowValueRange);
    private final IndexConsumer<Double> weightParameter = new IndexConsumer<>(wdtw::setWeight, weightValueRange);

    @Override
    protected Classifier build() {
        NearestNeighbour nearestNeighbour = new NearestNeighbour();
        wdtw = new Wdtw();
        nearestNeighbour.setDistanceMeasure(wdtw);
        return nearestNeighbour;
    }

    @Override
    protected IndexConsumer<?>[] getParameters() {
        return new IndexConsumer<?>[]{warpingWindowParameter, weightParameter};
    }
}
