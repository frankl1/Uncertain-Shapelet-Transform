package timeseriesweka.classifiers.Nn.Specialised.Erp;

import Tuning.Tuned;
import Tuning.ParameterSpaces.ParameterSpace;
import Tuning.ParameterSpaces.ParameterValuesFinder;
import Tuning.ParameterSpaces.ParameterSpaces;
import utilities.StatisticUtilities;
import weka.core.Instances;

import static utilities.Utilities.incrementalDiffList;

public class TunedErpNn extends Tuned<ErpNn> {
    private final ErpNn erpNn = new ErpNn();
    private final ParameterSpace<Double> penalty = new ParameterSpace<>(erpNn::setPenalty);
    private final ParameterSpace<Double> warpingWindow = new ParameterSpace<>(erpNn::setWarpingWindow, incrementalDiffList(0, 0.25, 10));
    private ParameterValuesFinder<Double> penaltyValuesFinder = trainInstances -> {
        double maxTolerance = StatisticUtilities.populationStandardDeviation(trainInstances);
        double minTolerance = maxTolerance * 0.2;
        return incrementalDiffList(minTolerance, maxTolerance, 10);
    };

    public TunedErpNn() {
        ParameterSpaces parameterSpaces = getParameterSpaces();
        parameterSpaces.add(penalty);
        parameterSpaces.add(warpingWindow);
    }

    @Override
    public void useTrainInstances(final Instances trainInstances) {
        penalty.setValues(penaltyValuesFinder.find(trainInstances));
    }

    @Override
    protected ErpNn getClassifierInstance() {
        return erpNn;
    }
}
