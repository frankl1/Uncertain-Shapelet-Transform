package development.go.Ee.Constituents.ParameterSpaces;

import development.go.Indexed.IndexConsumer;
import development.go.Indexed.IndexedValues;
import timeseriesweka.measures.twe.Twe;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TweParameterSpace extends ParameterSpace<Twe> {
    private final IndexedValues<Double> stiffnessValues = new IndexedValues<>(Arrays.asList(
        0.00001,
        0.0001,
        0.0005,
        0.001,
        0.005,
        0.01,
        0.05,
        0.1,
        0.5,
        1d
    ));
    private double stiffnessValue;
    private final IndexConsumer<Double> stiffnessParameter = new IndexConsumer<>(stiffnessValues, v -> stiffnessValue = v);
    private final IndexedValues<Double> penaltyValues = new IndexedValues<>(Arrays.asList(
        0d,
        0.011111111,
        0.022222222,
        0.033333333,
        0.044444444,
        0.055555556,
        0.066666667,
        0.077777778,
        0.088888889,
        0.1
    ));
    private double penaltyValue;
    private final IndexConsumer<Double> penaltyParameter = new IndexConsumer<>(penaltyValues, v -> penaltyValue = v);


    public List<IndexConsumer<?>> setupParameters(Instances instances) {
        List<IndexConsumer<?>> parameters = new ArrayList<>();
        parameters.add(penaltyParameter);
        parameters.add(stiffnessParameter);
        return parameters;
    }

    @Override
    protected Twe get() {
        return new Twe();
    }

    @Override
    protected void configure(final Twe subject) {
        subject.setStiffness(stiffnessValue);
        subject.setPenalty(penaltyValue);
    }
}
