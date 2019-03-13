package development.go.Ee.Constituents.ParameterSpaces.old;

import development.go.Ee.Constituents.ParameterSpaces.WdtwParameterSpace;
import development.go.Indexed.DoubleLinearInterpolator;
import development.go.Indexed.IndexConsumer;
import weka.core.Instances;

import java.util.List;

public class OldWdtwParameterSpace extends WdtwParameterSpace {
    @Override
    public List<IndexConsumer<?>> setupParameters(final Instances instances) {
        List<IndexConsumer<?>> parameters = super.setupParameters(instances);
        DoubleLinearInterpolator weightValues = getWeightValues();
        weightValues.setMin(0.0);
        weightValues.setMax(0.99);
        weightValues.setSize(100);
        return parameters;
    }
}
