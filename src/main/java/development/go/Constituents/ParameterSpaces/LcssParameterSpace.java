package development.go.Constituents.ParameterSpaces;

import development.go.Indexed.DoubleLinearInterpolator;
import development.go.Indexed.IndexConsumer;
import timeseriesweka.measures.lcss.Lcss;
import utilities.StatisticUtilities;
import weka.core.Instances;

import java.util.List;

public class LcssParameterSpace extends ParameterSpace<Lcss> {
    private final DoubleLinearInterpolator toleranceValues = new DoubleLinearInterpolator(0d,0d,1);
    private double toleranceValue;
    private final IndexConsumer<Double> toleranceParameter = new IndexConsumer<>(toleranceValues, v -> toleranceValue = v);
    private DtwParameterSpace dtwParameterSpace = new DtwParameterSpace();

    public List<IndexConsumer<?>> setupParameters(Instances instances) {
        List<IndexConsumer<?>> parameters = dtwParameterSpace.setupParameters(instances);
        double maxWarpingWindow = 0.25;
        double minWarpingWindow = 0;
        DoubleLinearInterpolator warpingWindowValues = dtwParameterSpace.getWarpingWindowValues();
        warpingWindowValues.setMax(maxWarpingWindow);
        warpingWindowValues.setMin(minWarpingWindow);
        warpingWindowValues.setSize(10);
        double maxTolerance = StatisticUtilities.populationStandardDeviation(instances);
        double minTolerance = 0.2 * maxTolerance;
        toleranceValues.setMin(minTolerance);
        toleranceValues.setMax(maxTolerance);
        toleranceValues.setSize(10);
        parameters.add(toleranceParameter);
        return parameters;
    }

    @Override
    public void configure(final Lcss subject) {
        subject.setTolerance(toleranceValue);
        dtwParameterSpace.configure(subject);
    }

    @Override
    protected Lcss get() {
        return new Lcss();
    }
}
