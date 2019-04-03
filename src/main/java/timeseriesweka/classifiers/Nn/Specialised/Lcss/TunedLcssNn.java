package timeseriesweka.classifiers.Nn.Specialised.Lcss;

import development.go.Ee.Tuned;
import development.go.Indexed.IndexedValues;
import evaluation.tuning.ParameterSpace;
import timeseriesweka.classifiers.Nn.Specialised.Msm.MsmNn;
import timeseriesweka.measures.dtw.Dtw;
import timeseriesweka.measures.lcss.Lcss;
import timeseriesweka.measures.wdtw.Wdtw;
import utilities.StatisticUtilities;
import utilities.Utilities;
import weka.core.Instances;

import java.util.Collections;

import static utilities.Utilities.incrementalDiffList;

public class TunedLcssNn extends Tuned {

    public TunedLcssNn() {
        setClassifier(new LcssNn());
    }

    @Override
    public void useTrainInstances(final Instances trainInstances) {
        ParameterSpace parameterSpace = getParameterSpace();
        double maxTolerance = StatisticUtilities.populationStandardDeviation(trainInstances);
        double minTolerance = maxTolerance * 0.2;
        parameterSpace.addParameter(Lcss.TOLERANCE_KEY, incrementalDiffList(minTolerance, maxTolerance, 10));
        parameterSpace.addParameter(Lcss.WARPING_WINDOW_KEY, incrementalDiffList(0, 0.25, 10));
    }
}
