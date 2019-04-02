package timeseriesweka.classifiers.Nn.Specialised.Dtw;

import Tuning.Tuned;
import Tuning.ParameterSpaces.ParameterSpace;
import Tuning.ParameterSpaces.ParameterSpaces;
import utilities.Utilities;

public class TunedDtwNn extends Tuned<DtwNn> {
    private final DtwNn dtwNn = new DtwNn();
    private final ParameterSpace<Double> warpingWindow = new ParameterSpace<>(dtwNn::setWarpingWindow, Utilities.linearInterpolate(0, 1, 101));

    public TunedDtwNn() {
        ParameterSpaces parameterSpaces = getParameterSpaces();
        parameterSpaces.add(warpingWindow);
    }

    @Override
    protected DtwNn getClassifierInstance() {
        return dtwNn;
    }
}
