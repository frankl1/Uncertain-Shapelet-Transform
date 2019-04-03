package timeseriesweka.classifiers.Nn.Specialised.Dtw;

import evaluation.tuning.ParameterSpace;
import timeseriesweka.measures.dtw.Dtw;
import weka.core.Instances;

import java.util.Collections;

public class FullWindowTunedDtwNn extends TunedDtwNn {
    @Override
    public void useTrainInstances(final Instances trainInstances) {
        ParameterSpace parameterSpace = getParameterSpace();
        parameterSpace.addParameter(Dtw.WARPING_WINDOW_KEY, Collections.singletonList(1d));
    }
}
