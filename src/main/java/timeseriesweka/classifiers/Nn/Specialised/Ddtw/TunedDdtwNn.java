package timeseriesweka.classifiers.Nn.Specialised.Ddtw;

import Tuning.Tuned;
import Tuning.ParameterSpaces.ParameterSpace;
import Tuning.ParameterSpaces.ParameterSpaces;

import static utilities.Utilities.incrementalDiffList;

public class TunedDdtwNn extends Tuned<DdtwNn> {
    private final DdtwNn ddtwNn = new DdtwNn();
    private final ParameterSpace<Double> warpingWindow = new ParameterSpace<>(ddtwNn::setWarpingWindow, incrementalDiffList(0, 1, 100));

    public TunedDdtwNn() {
        ParameterSpaces parameterSpaces = getParameterSpaces();
        parameterSpaces.add(warpingWindow);
    }

    @Override
    protected DdtwNn getClassifierInstance() {
        return ddtwNn;
    }
}
