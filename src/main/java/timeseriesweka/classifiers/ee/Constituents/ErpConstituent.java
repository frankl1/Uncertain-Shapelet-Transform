package timeseriesweka.classifiers.ee.Constituents;

import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.classifiers.ee.*;
import timeseriesweka.classifiers.ee.Iteration.AbstractIndexIterator;
import timeseriesweka.classifiers.ee.Iteration.IndexIterator;
import timeseriesweka.classifiers.ee.Iteration.Iterator;
import timeseriesweka.classifiers.ee.Iteration.LinearIndexIterator;
import timeseriesweka.measures.erp.Erp;
import weka.classifiers.Classifier;

import java.util.LinkedList;
import java.util.List;

import static timeseriesweka.classifiers.ee.LinearInterpolater.SCALED;

public class ErpConstituent extends Constituent {

    private Erp erp;
    private final ValueRange<Double> warpingWindowValueRange = new ValueRange<>(SCALED, new Range());

    public ValueRange<Double> getPenaltyValueRange() {
        return penaltyValueRange;
    }

    private final ValueRange<Double> penaltyValueRange = new ValueRange<>(SCALED, new Range());

    private final Parameter<Double> warpingWindowParameter = new Parameter<>(erp::setWarpingWindow, warpingWindowValueRange);
    private final Parameter<Double> penaltyParameter = new Parameter<>(erp::setPenalty, penaltyValueRange);

    @Override
    protected Classifier build() {
        NearestNeighbour nearestNeighbour = new NearestNeighbour();
        erp = new Erp();
        nearestNeighbour.setDistanceMeasure(erp);
        return nearestNeighbour;
    }

    @Override
    protected Parameter<?>[] getParameters() {
        return new Parameter<?>[] {warpingWindowParameter, penaltyParameter};
    }

    public ValueRange<Double> getWarpingWindowValueRange() {
        return warpingWindowValueRange;
    }
}
