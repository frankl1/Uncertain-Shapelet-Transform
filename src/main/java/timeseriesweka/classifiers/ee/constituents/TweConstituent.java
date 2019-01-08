package timeseriesweka.classifiers.ee.constituents;

import timeseriesweka.classifiers.Classifier;
import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.classifiers.ee.index.IndexConsumer;
import timeseriesweka.classifiers.ee.range.Range;
import timeseriesweka.classifiers.ee.range.ValueRange;
import timeseriesweka.measures.lcss.Lcss;

import static timeseriesweka.classifiers.ee.index.LinearInterpolater.SCALED;

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

    private final IndexConsumer<Double> nuParameter = new IndexConsumer<>(lcss::setWarpingWindow, nuValueRange);
    private final IndexConsumer<Double> lambdaParameter = new IndexConsumer<>(lcss::setTolerance, lambdaValueRange);

    @Override
    protected Classifier build() {
        NearestNeighbour nearestNeighbour = new NearestNeighbour();
        lcss = new Lcss();
        nearestNeighbour.setDistanceMeasure(lcss);
        return nearestNeighbour;
    }

    @Override
    protected IndexConsumer<?>[] getParameters() {
        return new IndexConsumer<?>[]{nuParameter, lambdaParameter};
    }
}
