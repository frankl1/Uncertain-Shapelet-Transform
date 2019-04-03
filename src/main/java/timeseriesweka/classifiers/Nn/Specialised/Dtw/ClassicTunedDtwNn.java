package timeseriesweka.classifiers.Nn.Specialised.Dtw;

import evaluation.tuning.ParameterSpace;
import timeseriesweka.measures.dtw.Dtw;
import utilities.Utilities;
import weka.core.Instances;

import java.util.Collections;

public class ClassicTunedDtwNn extends TunedDtwNn {
    @Override
    public void useTrainInstances(final Instances trainInstances) {
        ParameterSpace parameterSpace = getParameterSpace();
        parameterSpace.addParameter(Dtw.WARPING_WINDOW_KEY, Utilities.linearInterpolate(100, 100));
    }
}
