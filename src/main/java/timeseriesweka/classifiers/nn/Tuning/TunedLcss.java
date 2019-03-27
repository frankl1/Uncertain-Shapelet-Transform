package timeseriesweka.classifiers.nn.Tuning;

import evaluation.storage.ClassifierResults;
import timeseriesweka.classifiers.nn.LcssBuilder;
import utilities.ClassifierTools;
import utilities.InstanceTools;
import utilities.StatisticUtilities;
import utilities.Utilities;
import weka.core.Instance;
import weka.core.Instances;

import java.util.function.Function;

import static utilities.Utilities.incrementalDiffList;

public class TunedLcss extends AbstractTuned {

    @Override
    protected Function<Instances, PermutationBuilder> getPermutationBuilderFunction() {
        return instances -> {
            double maxTolerance = StatisticUtilities.populationStandardDeviation(instances);
            double minTolerance = maxTolerance * 0.2;
            LcssBuilder lcssBuilder = new LcssBuilder();
            lcssBuilder.setToleranceValues(incrementalDiffList(minTolerance, maxTolerance, 10));
            lcssBuilder.setWarpingValues(incrementalDiffList(0, 1, 10));
            return lcssBuilder;
        };
    }

    public static void main(String[] args) throws Exception {
        String datasetsDir = "/scratch/Datasets/TSCProblems2015";
        String datasetName = "GunPoint";
        int seed = 0;
        Instances trainInstances = ClassifierTools.loadData(datasetsDir + "/" + datasetName + "/" + datasetName + "_TRAIN.arff");
        Instances testInstances = ClassifierTools.loadData(datasetsDir + "/" + datasetName + "/" + datasetName + "_TEST.arff");
        Instances[] splitInstances = InstanceTools.resampleTrainAndTestInstances(trainInstances, testInstances, seed);
        trainInstances = splitInstances[0];
        testInstances = splitInstances[1];
        TunedLcss tunedLcss = new TunedLcss();
        for(int i = 0; i < tunedLcss.size(); i++) {
            System.out.println(i);
            tunedLcss.setParametersFromIndex(i);
            tunedLcss.buildClassifier(trainInstances);
        }
        tunedLcss.setParamSearch(false);
        tunedLcss.buildClassifier(trainInstances);
        ClassifierResults results = new ClassifierResults();
        for(Instance testInstance : testInstances) {
            double[] distribution = tunedLcss.distributionForInstance(testInstance);
            results.addPrediction(distribution, Utilities.argMax(distribution)[0], 0, null);
        }
        results.findAllStats();
        System.out.println(results.getAcc());
    }
}
