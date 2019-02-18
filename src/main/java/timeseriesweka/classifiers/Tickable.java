package timeseriesweka.classifiers;

import weka.core.Instances;

public interface Tickable {
    boolean remainingTrainTicks();
    void trainTick();
    default void train() {
        while (remainingTrainTicks()) {
            trainTick();
        }
    }
    void setTrainInstances(Instances trainInstances);
    boolean remainingTestTicks();
    void testTick();
    default void test() {
        while (remainingTestTicks()) {
            testTick();
        }
    }
    void setTestInstances(Instances testInstances);
//    double[][] predict(); todo change this to return classifier results + diff version for cv
}
