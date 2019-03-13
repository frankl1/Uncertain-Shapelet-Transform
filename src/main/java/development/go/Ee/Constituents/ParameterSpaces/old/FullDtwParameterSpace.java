package development.go.Ee.Constituents.ParameterSpaces.old;

import development.go.Ee.Constituents.ParameterSpaces.DtwParameterSpace;
import development.go.Indexed.DoubleLinearInterpolator;
import development.go.Indexed.IndexConsumer;
import weka.core.Instances;

import java.util.List;

public class FullDtwParameterSpace extends DtwParameterSpace {
    @Override
    public List<IndexConsumer<?>> setupParameters(final Instances instances) {
        List<IndexConsumer<?>> parameters = super.setupParameters(instances);
        DoubleLinearInterpolator warpingWindowValues = getWarpingWindowValues();
        warpingWindowValues.setMin(1.0);
        warpingWindowValues.setMax(1.0);
        warpingWindowValues.setSize(1);
        return parameters;
    }
}
