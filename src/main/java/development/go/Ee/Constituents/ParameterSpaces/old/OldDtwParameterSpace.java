package development.go.Ee.Constituents.ParameterSpaces.old;

import development.go.Ee.Constituents.ParameterSpaces.DtwParameterSpace;
import development.go.Indexed.DoubleLinearInterpolator;
import development.go.Indexed.IndexConsumer;
import weka.core.Instances;

import java.util.List;

public class OldDtwParameterSpace extends DtwParameterSpace {
    @Override
    public List<IndexConsumer<?>> setupParameters(final Instances instances) {
        List<IndexConsumer<?>> parameters = super.setupParameters(instances);
        DoubleLinearInterpolator warpingWindowValues = getWarpingWindowValues();
        warpingWindowValues.setMin(0.0);
        warpingWindowValues.setMax(0.99);
        warpingWindowValues.setSize(100);
        return parameters;
    }
}
