package timeseriesweka.classifiers.ee;

import timeseriesweka.classifiers.Tickable;
import utilities.ClassifierResults;

public class TrainedTickable {
    private final Tickable tickable;
    private ClassifierResults trainResults;

    public TrainedTickable(final Tickable tickable) {
        this.tickable = tickable;
        findTrainResults();
    }

    public Tickable getTickable() {
        return tickable;
    }

    public ClassifierResults getTrainResults() {
        return trainResults;
    }

    public void findTrainResults() {
        trainResults = tickable.findTrainResults();
    }
}
