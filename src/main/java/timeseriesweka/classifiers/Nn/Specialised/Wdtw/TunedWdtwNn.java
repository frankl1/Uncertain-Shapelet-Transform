package timeseriesweka.classifiers.Nn.Specialised.Wdtw;

import Tuning.Tuned;
import Tuning.ParameterSpaces.ParameterSpace;
import Tuning.ParameterSpaces.ParameterSpaces;
import utilities.Utilities;

public class TunedWdtwNn extends Tuned<WdtwNn> {
    private final WdtwNn wdtwNn = new WdtwNn();
    private final ParameterSpace<Double> weight = new ParameterSpace<>(wdtwNn::setWarpingWindow, Utilities.linearInterpolate(0, 1, 101));
    private final ParameterSpace<Double> warpingWindow = new ParameterSpace<>(wdtwNn::setWarpingWindow, Utilities.linearInterpolate(1, 1, 1));

    public TunedWdtwNn() {
        ParameterSpaces parameterSpaces = getParameterSpaces();
        parameterSpaces.add(weight);
        parameterSpaces.add(warpingWindow);
    }

    @Override
    protected WdtwNn getClassifierInstance() {
        return wdtwNn;
    }
}
