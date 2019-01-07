package utilities.instances;

import utilities.ClassifierResults;
import weka.classifiers.Classifier;
import weka.core.Instance;

public class Experimenter {

    private Experimenter() {}

    public static void experiment(Classifier classifier, TrainTestSplit trainTestSplit, ClassifierResults classifierResults) throws Exception {
        classifier.buildClassifier(trainTestSplit.getTrain());
        for(Instance testInstance : trainTestSplit.getTest()) {
            classifierResults.storeSingleResult(testInstance.classValue(), classifier.distributionForInstance(testInstance));
        }
    }

    public static ClassifierResults experiment(Classifier classifier, Folds folds) throws Exception {
        ClassifierResults classifierResults = new ClassifierResults();
        for(TrainTestSplit trainTestSplit : folds) {
            experiment(classifier, trainTestSplit, classifierResults);
        }
        return classifierResults;
    }

}
