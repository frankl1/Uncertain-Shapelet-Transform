package utilities.instances;

import weka.core.Instances;

public class TrainTestSplit {
    private final Instances train;

    public Instances getTrain() {
        return train;
    }

    public Instances getTest() {
        return test;
    }

    private final Instances test;

    public int getNumInstances() {
        return train.numInstances() + test.numInstances();
    }

    public int getNumClasses() {
        return train.numClasses(); // todo make sure train and test have same num classes
    }

    public TrainTestSplit(Instances train, Instances test) {
        this.train = train;
        this.test = test;
    }
}
