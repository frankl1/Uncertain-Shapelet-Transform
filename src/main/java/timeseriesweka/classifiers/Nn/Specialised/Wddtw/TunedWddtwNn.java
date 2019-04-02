package timeseriesweka.classifiers.Nn.Specialised.Wddtw;

import Tuning.Tuned;
import Tuning.ParameterSpaces.ParameterSpace;
import Tuning.ParameterSpaces.ParameterSpaces;
import utilities.Utilities;

public class TunedWddtwNn extends Tuned<WddtwNn> {
    private final WddtwNn wddtwNn = new WddtwNn();
    private final ParameterSpace<Double> weight = new ParameterSpace<>(wddtwNn::setWarpingWindow, Utilities.linearInterpolate(0, 1, 101));
    private final ParameterSpace<Double> warpingWindow = new ParameterSpace<>(wddtwNn::setWarpingWindow, Utilities.linearInterpolate(1, 1, 1));


    public TunedWddtwNn() {
        ParameterSpaces parameterSpaces = getParameterSpaces();
        parameterSpaces.add(weight);
        parameterSpaces.add(warpingWindow);
    }

    @Override
    protected WddtwNn getClassifierInstance() {
        return wddtwNn;
    }
}
