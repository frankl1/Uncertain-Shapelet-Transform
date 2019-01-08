package timeseriesweka.classifiers.ee.constituents;

import timeseriesweka.classifiers.Classifier;
import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.classifiers.ee.index.IndexConsumer;
import timeseriesweka.classifiers.ee.range.Range;
import timeseriesweka.classifiers.ee.range.ValueRange;
import timeseriesweka.measures.msm.Msm;

import static timeseriesweka.classifiers.ee.index.LinearInterpolater.SCALED;

public class MsmConstituent extends Constituent {
    private final ValueRange<Double> warpingWindowValueRange = new ValueRange<>(SCALED, new Range());
    private final ValueRange<Double> costValueRange = new ValueRange<>(SCALED, new Range());

    public ValueRange<Double> getWarpingWindowValueRange() {
        return warpingWindowValueRange;
    }

    public ValueRange<Double> getCostValueRange() {
        return costValueRange;
    }

    private Msm msm;

    private final IndexConsumer<Double> warpingWindowParameter = new IndexConsumer<>(msm::setWarpingWindow, warpingWindowValueRange);
    private final IndexConsumer<Double> costParameter = new IndexConsumer<>(msm::setCost, costValueRange);

    @Override
    protected Classifier build() {
        NearestNeighbour nearestNeighbour = new NearestNeighbour();
        msm = new Msm();
        nearestNeighbour.setDistanceMeasure(msm);
        return nearestNeighbour;
    }

    @Override
    protected IndexConsumer<?>[] getParameters() {
        return new IndexConsumer<?>[]{warpingWindowParameter, costParameter};
    }
}
