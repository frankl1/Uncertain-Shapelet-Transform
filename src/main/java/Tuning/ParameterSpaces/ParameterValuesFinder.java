package Tuning.ParameterSpaces;

import weka.core.Instances;

import java.util.List;

public interface ParameterValuesFinder<A> {
    List<? extends A> find(Instances trainInstances);
}
