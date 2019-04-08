package timeseriesweka.classifiers.Nn.Specialised.Wdtw;

import development.go.Ee.Tuned;
import evaluation.tuning.ParameterSpace;
import evaluation.tuning.Tuner;
import timeseriesweka.measures.dtw.Dtw;
import timeseriesweka.measures.wdtw.Wdtw;
import utilities.Utilities;
import weka.core.Instances;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TunedWdtwNn extends Tuned {

    public TunedWdtwNn() {
        setClassifierSupplier(WdtwNn::new);
    }

    @Override
    public void useTrainInstances(final Instances trainInstances) {
        ParameterSpace parameterSpace = getParameterSpace();
        parameterSpace.addParameter(Wdtw.WARPING_WINDOW_KEY, Collections.singletonList(1d));
        parameterSpace.addParameter(Wdtw.WEIGHT_KEY, Utilities.linearInterpolate(101, 100));
    }

}
