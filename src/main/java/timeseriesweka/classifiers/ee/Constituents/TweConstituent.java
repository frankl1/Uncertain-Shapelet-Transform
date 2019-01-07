package timeseriesweka.classifiers.ee.Constituents;

import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.classifiers.ee.Parameter;
import timeseriesweka.classifiers.ee.Range;
import timeseriesweka.classifiers.ee.ValueRange;
import timeseriesweka.measures.lcss.Lcss;
import weka.classifiers.Classifier;

import static timeseriesweka.classifiers.ee.LinearInterpolater.SCALED;

public class TweConstituent extends Constituent {
    private final ValueRange<Double> nuValueRange = new ValueRange<>(SCALED, new Range());
    private final ValueRange<Double> lambdaValueRange = new ValueRange<>(SCALED, new Range());
    private Lcss lcss;

    public ValueRange<Double> getNuValueRange() {
        return nuValueRange;
    }

    public ValueRange<Double> getLambdaValueRange() {
        return lambdaValueRange;
    }

    private final Parameter<Double> nuParameter = new Parameter<>(lcss::setWarpingWindow, nuValueRange);
    private final Parameter<Double> lambdaParameter = new Parameter<>(lcss::setTolerance, lambdaValueRange);

    @Override
    protected Classifier build() {
        NearestNeighbour nearestNeighbour = new NearestNeighbour();
        lcss = new Lcss();
        nearestNeighbour.setDistanceMeasure(lcss);
        return nearestNeighbour;
    }

    @Override
    protected Parameter<?>[] getParameters() {
        return new Parameter<?>[]{nuParameter, lambdaParameter};
    }
}
