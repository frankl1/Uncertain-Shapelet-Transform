package timeseriesweka.classifiers;

import weka.core.Instances;

import java.util.concurrent.TimeUnit;

public interface Contracted {
    default void setTrainContract(long amount, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }
    void setTrainContract(long nanoseconds);
    long getTrainContract();
    default void setTestContract(long amount, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }
    void setTestContract(long nanoseconds);
    long getTestContract();
    double[][] distributionForInstances(Instances instances) throws Exception;
}
