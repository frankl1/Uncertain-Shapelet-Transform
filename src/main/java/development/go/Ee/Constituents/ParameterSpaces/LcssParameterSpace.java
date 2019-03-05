package development.go.Ee.Constituents.ParameterSpaces;

import development.go.Indexed.DoubleLinearInterpolator;
import development.go.Indexed.IndexConsumer;
import development.go.Indexed.IndexedValues;
import timeseriesweka.measures.lcss.Lcss;
import utilities.StatisticUtilities;
import utilities.Utilities;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

import static utilities.Utilities.incrementalDiffList;

public class LcssParameterSpace extends ParameterSpace<Lcss> {
    private final IndexedValues<Double> toleranceValues = new IndexedValues<>();
    private double toleranceValue;
    private final IndexConsumer<Double> toleranceParameter = new IndexConsumer<>(toleranceValues, v -> toleranceValue = v);
    private final IndexedValues<Double> warpingWindowValues = new IndexedValues<>();
    private double warpingWindowValue;
    private final IndexConsumer<Double> warpingWindowParameter = new IndexConsumer<>(warpingWindowValues, v -> warpingWindowValue = v);

    public List<IndexConsumer<?>> setupParameters(Instances instances) {
        List<IndexConsumer<?>> parameters = new ArrayList<>();
        int length = instances.numAttributes() - 1;
        int maxWarpingWindow = (length) / 4;
        int minWarpingWindow = 0;
        List<Double> warpingWindowRawValues = incrementalDiffList(minWarpingWindow, maxWarpingWindow, 10);
        List<Double> warpingWindowPercentageValues = new ArrayList<>();
        for(Double d : warpingWindowRawValues) {
            warpingWindowPercentageValues.add((double) Math.round(d) / length);
        }
        this.warpingWindowValues.setValues(warpingWindowPercentageValues);
        parameters.add(warpingWindowParameter);

        double maxTolerance = StatisticUtilities.populationStandardDeviation(instances);
        double minTolerance = 0.2 * maxTolerance;
        toleranceValues.setValues(Utilities.incrementalDiffList(minTolerance, maxTolerance, 10));
        parameters.add(0, toleranceParameter);
        return parameters;
    }

    @Override
    public void configure(final Lcss subject) {
        subject.setTolerance(toleranceValue);
        subject.setWarpingWindow(warpingWindowValue);
    }

    @Override
    protected Lcss get() {
        return new Lcss();
    }
}
