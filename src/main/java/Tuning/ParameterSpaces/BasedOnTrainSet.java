package Tuning.ParameterSpaces;

import weka.core.Instances;

/**
 * interface to model behaviour of requiring the train set for something, usually prior to buildClassifier. Intention is to set parameter spaces based upon the train set, which must be setup before buildClassifier calls.
 */
public interface BasedOnTrainSet {
    default void useTrainInstances(Instances trainInstances) {

    }
}
