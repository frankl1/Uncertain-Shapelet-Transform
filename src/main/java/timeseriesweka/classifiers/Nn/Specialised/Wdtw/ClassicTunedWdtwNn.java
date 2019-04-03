package timeseriesweka.classifiers.Nn.Specialised.Wdtw;

import development.go.Ee.Tuned;
import evaluation.tuning.ParameterSpace;
import timeseriesweka.measures.dtw.Dtw;
import timeseriesweka.measures.wdtw.Wdtw;
import utilities.Utilities;
import weka.core.Instances;

import java.util.Collections;

public class ClassicTunedWdtwNn extends TunedWdtwNn {

    @Override
    public void useTrainInstances(final Instances trainInstances) {
        ParameterSpace parameterSpace = getParameterSpace();
        parameterSpace.addParameter(Dtw.WARPING_WINDOW_KEY, Collections.singletonList(1d));
        parameterSpace.addParameter(Wdtw.WEIGHT_KEY, Utilities.linearInterpolate(100, 100));
    }
}
