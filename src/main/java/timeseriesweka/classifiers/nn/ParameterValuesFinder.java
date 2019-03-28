package timeseriesweka.classifiers.nn;

import weka.core.Instances;

import java.util.List;

public interface ParameterValuesFinder {
    List<? extends Object> find(Instances trainInstances);
}
