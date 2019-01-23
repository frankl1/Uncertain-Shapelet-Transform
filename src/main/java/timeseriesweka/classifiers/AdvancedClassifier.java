package timeseriesweka.classifiers;

import utilities.ClassifierResults;
import utilities.Reproducible;
import utilities.SaveParameterInfo;
import weka.core.Instances;

public interface AdvancedClassifier extends weka.classifiers.Classifier, Reproducible, ContractClassifier, CheckpointClassifier, SaveParameterInfo {
    ClassifierResults predict(Instances testFold);
}
