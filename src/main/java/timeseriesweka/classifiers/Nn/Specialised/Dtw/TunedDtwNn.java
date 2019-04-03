package timeseriesweka.classifiers.Nn.Specialised.Dtw;

import development.go.Ee.Tuned;
import evaluation.tuning.ParameterSpace;
import timeseriesweka.measures.dtw.Dtw;
import utilities.Utilities;
import weka.core.Instances;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TunedDtwNn extends Tuned {

    public TunedDtwNn() {
        setClassifier(new DtwNn());
    }

    @Override
    public void useTrainInstances(final Instances trainInstances) {
        ParameterSpace parameterSpace = getParameterSpace();
        parameterSpace.addParameter(Dtw.WARPING_WINDOW_KEY, Utilities.linearInterpolate(101, 100));
    }
}
