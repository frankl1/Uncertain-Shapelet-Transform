package timeseriesweka.classifiers.ee.constituents;

import timeseriesweka.classifiers.Classifier;
import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.classifiers.ee.index.IndexConsumer;
import utilities.range.Range;
import utilities.range.ValueRange;
import timeseriesweka.measures.wddtw.Wddtw;

import static timeseriesweka.classifiers.ee.index.LinearInterpolater.SCALED;

public class WddtwConstituent extends Constituent {

    private final ValueRange<Double> warpingWindowValueRange = new ValueRange<>(SCALED, new Range());
    private final ValueRange<Double> weightValueRange = new ValueRange<>(SCALED, new Range());

    public ValueRange<Double> getWarpingWindowValueRange() {
        return warpingWindowValueRange;
    }

    public ValueRange<Double> getWeightValueRange() {
        return weightValueRange;
    }

    private Wddtw wddtw;
    private final IndexConsumer<Double> warpingWindowParameter = new IndexConsumer<>(wddtw::setWarpingWindow, warpingWindowValueRange);
    private final IndexConsumer<Double> weightParameter = new IndexConsumer<>(wddtw::setWeight, weightValueRange);

    @Override
    protected Classifier build() {
        NearestNeighbour nearestNeighbour = new NearestNeighbour();
        wddtw = new Wddtw();
        nearestNeighbour.setDistanceMeasure(wddtw);
        return nearestNeighbour;
    }

    @Override
    protected IndexConsumer<?>[] getParameters() {
        return new IndexConsumer<?>[]{warpingWindowParameter, weightParameter};
    }
}
