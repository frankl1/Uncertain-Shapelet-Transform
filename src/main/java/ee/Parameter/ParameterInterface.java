package timeseriesweka.classifiers.ensembles.ee.Parameter;

import java.io.Serializable;

public interface ParameterInterface<A, B> extends Serializable {
    void setParameterValue(A object, B value);
    B getParameterValue(A object);
}
