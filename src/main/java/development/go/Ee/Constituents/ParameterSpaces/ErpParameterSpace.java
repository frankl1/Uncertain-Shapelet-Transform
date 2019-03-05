package development.go.Ee.Constituents.ParameterSpaces;

import development.go.Indexed.DoubleLinearInterpolator;
import development.go.Indexed.IndexConsumer;
import development.go.Indexed.IndexedValues;
import timeseriesweka.measures.erp.Erp;
import utilities.StatisticUtilities;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

import static utilities.Utilities.incrementalDiffList;

public class ErpParameterSpace extends ParameterSpace<Erp> {

    private final IndexedValues<Double> penaltyValues = new IndexedValues<>();
    private double penaltyValue;
    private final IndexConsumer<Double> penaltyParameter = new IndexConsumer<>(penaltyValues, v -> penaltyValue = v);
    private final IndexedValues<Double> warpingWindowValues = new IndexedValues<>();
    private double warpingWindowValue;
    private final IndexConsumer<Double> warpingWindowParameter = new IndexConsumer<>(warpingWindowValues, v -> warpingWindowValue = v);

    @Override
    protected List<IndexConsumer<?>> setupParameters(final Instances instances) {
        List<IndexConsumer<?>> parameters = new ArrayList<>();
        double maxWarpingWindow = 0.25;
        double minWarpingWindow = 0;
        List<Double> warpingWindowValues = incrementalDiffList(minWarpingWindow, maxWarpingWindow, 10);
        parameters.add(warpingWindowParameter);
        this.warpingWindowValues.setValues(warpingWindowValues);

        double maxPenalty = StatisticUtilities.populationStandardDeviation(instances);
        double minPenalty = 0.2 * maxPenalty;
        List<Double> penaltyValues = incrementalDiffList(minPenalty, maxPenalty, 10);
        this.penaltyValues.setValues(penaltyValues);
        parameters.add(penaltyParameter);

        return parameters;
    }

    @Override
    protected Erp get() {
        return new Erp();
    }

    @Override
    protected void configure(final Erp subject) {
        subject.setPenalty(penaltyValue);
        subject.setWarpingWindow(warpingWindowValue);
    }
}
