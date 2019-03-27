package timeseriesweka.classifiers.nn;

import weka.core.Instances;

import java.util.List;

public interface ParameterFinder<A> {
    List<A> findParameters(Instances instances);
}
