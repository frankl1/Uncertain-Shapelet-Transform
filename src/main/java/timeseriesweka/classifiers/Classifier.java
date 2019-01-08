package timeseriesweka.classifiers;

import utilities.Reproducible;
import utilities.SaveParameterInfo;

public interface Classifier extends weka.classifiers.Classifier, Reproducible, ContractClassifier, CheckpointClassifier, SaveParameterInfo {
}
