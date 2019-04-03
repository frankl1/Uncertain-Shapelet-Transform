package timeseriesweka.classifiers.Nn.Specialised.Ddtw;

import development.go.Ee.Tuned;
import evaluation.tuning.ParameterSpace;
import timeseriesweka.measures.ddtw.Ddtw;
import timeseriesweka.measures.dtw.Dtw;
import utilities.Utilities;
import weka.core.Instances;

public class ClassicTunedDdtwNn extends TunedDdtwNn {

    @Override
    public void useTrainInstances(final Instances trainInstances) {
        ParameterSpace parameterSpace = getParameterSpace();
        parameterSpace.addParameter(Ddtw.WARPING_WINDOW_KEY, Utilities.linearInterpolate(100, 100));
    }
}
