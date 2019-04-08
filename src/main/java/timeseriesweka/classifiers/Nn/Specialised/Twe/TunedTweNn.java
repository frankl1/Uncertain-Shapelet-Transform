package timeseriesweka.classifiers.Nn.Specialised.Twe;

import development.go.Ee.Tuned;
import evaluation.tuning.ParameterSpace;
import timeseriesweka.classifiers.Nn.Specialised.Wdtw.WdtwNn;
import timeseriesweka.measures.dtw.Dtw;
import timeseriesweka.measures.twe.Twe;
import timeseriesweka.measures.wdtw.Wdtw;
import utilities.Utilities;
import weka.core.Instances;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TunedTweNn extends Tuned {
    public TunedTweNn() {
        setClassifierSupplier(TweNn::new);
    }

    @Override
    public void useTrainInstances(final Instances trainInstances) {
        ParameterSpace parameterSpace = getParameterSpace();
        parameterSpace.addParameter(Twe.PENALTY_KEY, Arrays.asList(0d,
            0.011111111,
            0.022222222,
            0.033333333,
            0.044444444,
            0.055555556,
            0.066666667,
            0.077777778,
            0.088888889,
            0.1));
        parameterSpace.addParameter(Twe.STIFFNESS_KEY, Arrays.asList(0.00001,
            0.0001,
            0.0005,
            0.001,
            0.005,
            0.01,
            0.05,
            0.1,
            0.5,
            1d));
    }

}
