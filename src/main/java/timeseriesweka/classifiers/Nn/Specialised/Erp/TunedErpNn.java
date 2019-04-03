package timeseriesweka.classifiers.Nn.Specialised.Erp;

import development.go.Ee.Tuned;
import development.go.Indexed.IndexedValues;
import evaluation.tuning.ParameterSpace;
import timeseriesweka.classifiers.Nn.Specialised.Wdtw.WdtwNn;
import timeseriesweka.measures.erp.Erp;
import timeseriesweka.measures.twe.Twe;
import utilities.StatisticUtilities;
import utilities.Utilities;
import weka.core.Instances;

import java.util.Arrays;
import java.util.List;

import static utilities.Utilities.incrementalDiffList;

public class TunedErpNn extends Tuned {

    public TunedErpNn() {
        setClassifier(new ErpNn());
    }

    @Override
    public void useTrainInstances(final Instances trainInstances) {
        ParameterSpace parameterSpace = getParameterSpace();
        double maxTolerance = StatisticUtilities.populationStandardDeviation(trainInstances);
        double minTolerance = maxTolerance * 0.2;
        parameterSpace.addParameter(Erp.PENALTY_KEY, incrementalDiffList(minTolerance, maxTolerance, 10));
        parameterSpace.addParameter(Erp.WARPING_WINDOW_KEY, incrementalDiffList(0, 0.25, 10));
    }
}
