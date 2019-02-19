package timeseriesweka.classifiers;

import utilities.ClassifierResults;
import weka.core.Instances;

public interface Tickable {
    boolean hasNextTrainTick();
    void trainTick();
    default void train() {
        while (hasNextTrainTick()) {
            trainTick();
        }
    }
    void setTrain(Instances trainInstances);
    boolean hasNextTestTick();
    void testTick();
    default void test() {
        while (hasNextTestTick()) {
            testTick();
        }
    }
    void setTest(Instances testInstances);
    double[][] predictTrain();
    double[][] predictTest();
    ClassifierResults findTrainResults();
    ClassifierResults findTestResults();
//    double[][] predict(); todo change this to return classifier results + diff version for cv
}
