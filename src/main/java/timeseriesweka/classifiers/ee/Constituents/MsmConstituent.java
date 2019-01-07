package timeseriesweka.classifiers.ee.Constituents;

import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.classifiers.ee.IndexedObtainer;
import timeseriesweka.classifiers.ee.Parameter;
import timeseriesweka.classifiers.ee.Range;
import timeseriesweka.classifiers.ee.ValueRange;
import timeseriesweka.measures.msm.Msm;
import weka.classifiers.Classifier;

import static timeseriesweka.classifiers.ee.LinearInterpolater.SCALED;

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

    private final Parameter<Double> warpingWindowParameter = new Parameter<>(msm::setWarpingWindow, warpingWindowValueRange);
    private final Parameter<Double> costParameter = new Parameter<>(msm::setCost, costValueRange);

    @Override
    protected Classifier build() {
        NearestNeighbour nearestNeighbour = new NearestNeighbour();
        msm = new Msm();
        nearestNeighbour.setDistanceMeasure(msm);
        return nearestNeighbour;
    }

    @Override
    protected Parameter<?>[] getParameters() {
        return new Parameter<?>[]{warpingWindowParameter, costParameter};
    }
}
