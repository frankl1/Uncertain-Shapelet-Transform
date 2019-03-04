package development.go.Constituents.ParameterSpaces;

import development.go.Indexed.DoubleLinearInterpolator;
import development.go.Indexed.IndexConsumer;
import timeseriesweka.measures.wdtw.Wdtw;
import weka.core.Instances;

import java.util.List;

public class WdtwParameterSpace extends ParameterSpace<Wdtw> {
    private final DoubleLinearInterpolator weightValues = new DoubleLinearInterpolator(0d,0d,1);
    private double weightValue;
    private final IndexConsumer<Double> weightParameter = new IndexConsumer<>(weightValues, v -> weightValue = v);
    private DtwParameterSpace dtwParameterSpace = new DtwParameterSpace();

    public List<IndexConsumer<?>> setupParameters(Instances instances) {
        List<IndexConsumer<?>> parameters = dtwParameterSpace.setupParameters(instances);
        double maxWarpingWindow = 1d;
        double minWarpingWindow = 1d;
        DoubleLinearInterpolator warpingWindowValues = dtwParameterSpace.getWarpingWindowValues();
        warpingWindowValues.setMax(maxWarpingWindow);
        warpingWindowValues.setMin(minWarpingWindow);
        warpingWindowValues.setSize(1);
        double maxweight = 100;
        double minweight = 0;
        weightValues.setMin(minweight);
        weightValues.setMax(maxweight);
        weightValues.setSize(101);
        parameters.add(weightParameter);
        return parameters;
    }

    @Override
    protected Wdtw get() {
        return new Wdtw();
    }

    @Override
    protected void configure(final Wdtw subject) {
        subject.setWeight(weightValue);
    }
}
