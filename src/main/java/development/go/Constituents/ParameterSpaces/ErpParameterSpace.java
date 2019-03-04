package development.go.Constituents.ParameterSpaces;

import development.go.Indexed.DoubleLinearInterpolator;
import development.go.Indexed.IndexConsumer;
import timeseriesweka.measures.erp.Erp;
import utilities.StatisticUtilities;
import weka.core.Instances;

import java.util.List;

public class ErpParameterSpace extends ParameterSpace<Erp> {

    private final DoubleLinearInterpolator penaltyValues = new DoubleLinearInterpolator(0d,0d,1);
    private double penaltyValue;
    private final IndexConsumer<Double> penaltyParameter = new IndexConsumer<>(penaltyValues, v -> penaltyValue = v);
    private DtwParameterSpace dtwParameterSpace = new DtwParameterSpace();


    @Override
    protected List<IndexConsumer<?>> setupParameters(final Instances instances) {
        List<IndexConsumer<?>> parameters = dtwParameterSpace.setupParameters(instances);
        double maxWarpingWindow = 0.25;
        double minWarpingWindow = 0;
        DoubleLinearInterpolator warpingWindowValues = dtwParameterSpace.getWarpingWindowValues();
        warpingWindowValues.setMax(maxWarpingWindow);
        warpingWindowValues.setMin(minWarpingWindow);
        warpingWindowValues.setSize(10);
        double maxpenalty = StatisticUtilities.populationStandardDeviation(instances);
        double minpenalty = 0.2 * maxpenalty;
        penaltyValues.setMin(minpenalty);
        penaltyValues.setMax(maxpenalty);
        penaltyValues.setSize(10);
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
        dtwParameterSpace.configure(subject);
    }
}
