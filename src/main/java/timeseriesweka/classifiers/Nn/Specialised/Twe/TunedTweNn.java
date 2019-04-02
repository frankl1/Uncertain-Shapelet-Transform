package timeseriesweka.classifiers.Nn.Specialised.Twe;

import Tuning.Tuned;
import Tuning.ParameterSpaces.ParameterSpace;
import Tuning.ParameterSpaces.ParameterSpaces;

import java.util.Arrays;

public class TunedTweNn extends Tuned<TweNn> {
    private final TweNn tweNn = new TweNn();
    private final ParameterSpace<Double> penalty = new ParameterSpace<>(tweNn::setPenalty, Arrays.asList(
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
    private final ParameterSpace<Double> stiffness = new ParameterSpace<>(tweNn::setStiffness, Arrays.asList(
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

    public TunedTweNn() {
        ParameterSpaces parameterSpaces = getParameterSpaces();
        parameterSpaces.add(penalty);
        parameterSpaces.add(stiffness);
    }

    @Override
    protected TweNn getClassifierInstance() {
        return tweNn;
    }
}
