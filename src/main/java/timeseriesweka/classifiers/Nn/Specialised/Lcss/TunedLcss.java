package timeseriesweka.classifiers.Nn.Specialised.Lcss;

import Tuning.AbstractTuned;
import Tuning.ParameterSpace;
import Tuning.ParameterValuesFinder;
import Tuning.ParametersSpace;
import evaluation.storage.ClassifierResults;
import utilities.ClassifierTools;
import utilities.InstanceTools;
import utilities.StatisticUtilities;
import utilities.Utilities;
import weka.core.Instance;
import weka.core.Instances;

import static utilities.Utilities.incrementalDiffList;

public class TunedLcss extends AbstractTuned<LcssNn> {

    private final LcssNn lcssNn = new LcssNn();
    private final ParameterSpace<Double> tolerance = new ParameterSpace<>(lcssNn::setTolerance);
    private final ParameterSpace<Double> warpingWindow = new ParameterSpace<>(lcssNn::setWarpingWindow, incrementalDiffList(0, 0.25, 10));
    private ParameterValuesFinder<Double> toleranceValuesFinder = trainInstances -> {
        double maxTolerance = StatisticUtilities.populationStandardDeviation(trainInstances);
        double minTolerance = maxTolerance * 0.2;
        return incrementalDiffList(minTolerance, maxTolerance, 10);
    };

    public TunedLcss() {
        ParametersSpace parametersSpace = getParametersSpace();
        parametersSpace.add(tolerance);
        parametersSpace.add(warpingWindow);
    }

    public static void main(String[] args) throws Exception {
        String datasetsDir = "/scratch/Datasets/TSCProblems2015";
        String datasetName = "GunPoint";
        int seed = 0;
        String resultsPath = "/scratch/expResults/LCSSNN/GunPoint";
        Instances trainInstances = ClassifierTools.loadData(datasetsDir + "/" + datasetName + "/" + datasetName + "_TRAIN.arff");
        Instances testInstances = ClassifierTools.loadData(datasetsDir + "/" + datasetName + "/" + datasetName + "_TEST.arff");
        Instances[] splitInstances = InstanceTools.resampleTrainAndTestInstances(trainInstances, testInstances, seed);
        trainInstances = splitInstances[0];
        testInstances = splitInstances[1];
        TunedLcss tunedLcss = new TunedLcss();
        tunedLcss.setSeed(seed);
        tunedLcss.setUpParameters(trainInstances);
        for(int i = 0; i < tunedLcss.size(); i++) {
            System.out.println(i);
            tunedLcss.setParamSearch(true);
            tunedLcss.writeCVTrainToFile(resultsPath + "/fold" + seed + "_" + i + ".csv");
            tunedLcss.setParametersFromIndex(i);
            tunedLcss.buildClassifier(trainInstances);
            tunedLcss.reset();
        }
        tunedLcss.writeCVTrainToFile(resultsPath + "/trainFold" + seed + ".csv");
        tunedLcss.setParamSearch(false);
        tunedLcss.buildClassifier(trainInstances);
        ClassifierResults results = new ClassifierResults();
        for(Instance testInstance : testInstances) {
            double[] distribution = tunedLcss.distributionForInstance(testInstance);
            results.addPrediction(testInstance.classValue(), distribution, Utilities.argMax(distribution)[0], 0, null);
        }
        results.findAllStats();
        results.writeFullResultsToFile(resultsPath + "/testFold" + seed + ".csv");
        System.out.println("acc: " + results.getAcc());
    }

    @Override
    public void setUpParameters(final Instances trainInstances) {
        if(toleranceValuesFinder != null) {
            tolerance.setValues(toleranceValuesFinder.find(trainInstances));
        }
    }

    @Override
    protected LcssNn getClassifierInstance() {
        return lcssNn;
    }
}
