package timeseriesweka.classifiers.ee;

import timeseriesweka.classifiers.Tickable;
import timeseriesweka.classifiers.ee.iteration.IndexIterator;
import timeseriesweka.classifiers.ee.iteration.RandomIndexIterator;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

public class Ticker implements Tickable {
    private List<Tickable> tickableList = new ArrayList<>();
    private IndexIterator trainIterator = new RandomIndexIterator();
    private IndexIterator testIterator = new RandomIndexIterator();

    @Override
    public boolean remainingTrainTicks() {
        return trainIterator.hasNext();
    }

    @Override
    public void trainTick() {

    }

    @Override
    public void setTrainInstances(final Instances trainInstances) {
        for(Tickable tickable : tickableList) {
            tickable.setTrainInstances(trainInstances);
        }
    }

    @Override
    public boolean remainingTestTicks() {
        return testIterator.hasNext();
    }

    @Override
    public void testTick() {

    }

    @Override
    public void setTestInstances(final Instances testInstances) {
        for(Tickable tickable : tickableList) {
            tickable.setTestInstances(testInstances);
        }
    }
}
