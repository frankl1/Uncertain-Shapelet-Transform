package development.go.Ee.Constituents.ParameterSpaces.old;

import development.go.Ee.Constituents.ParameterSpaces.DtwParameterSpace;
import development.go.Indexed.IndexConsumer;
import weka.core.Instances;

import java.util.List;

public class EdParameterSpace extends DtwParameterSpace {

    @Override
    public List<IndexConsumer<?>> setupParameters(final Instances instances) {
        List<IndexConsumer<?>> parameters = super.setupParameters(instances);
        getWarpingWindowValues().setSize(1);
        return parameters;
    }
}
