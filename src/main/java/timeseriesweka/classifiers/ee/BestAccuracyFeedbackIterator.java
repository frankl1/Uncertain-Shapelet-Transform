package timeseriesweka.classifiers.ee;

import java.util.Comparator;
import java.util.TreeSet;

public class BestAccuracyFeedbackIterator implements FeedbackIterator<TrainedTickable> {

    public boolean hasNext() {
        return !trainedTrainedTickableList.isEmpty();
    }

    public TrainedTickable next() {
        current = trainedTrainedTickableList.pollFirst();
        return current;
    }

    @Override
    public void remove() {
        trainedTrainedTickableList.remove(current);
    }

    private TrainedTickable current = null;

    public void add(TrainedTickable trainedTickable) {
        trainedTrainedTickableList.add(trainedTickable);
    }

    private TreeSet<TrainedTickable> trainedTrainedTickableList = new TreeSet<>(Comparator.comparingDouble(a -> a.getTrainResults().acc));
}
