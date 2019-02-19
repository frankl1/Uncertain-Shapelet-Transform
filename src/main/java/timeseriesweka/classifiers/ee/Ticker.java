//package timeseriesweka.classifiers.ee;
//
//import timeseriesweka.classifiers.Tickable;
//import timeseriesweka.classifiers.ee.iteration.IndexIterator;
//import timeseriesweka.classifiers.ee.iteration.RandomIndexIterator;
//import weka.core.Instances;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class Ticker implements Tickable {
//    private List<Tickable> trainedTickableList = new ArrayList<>();
//    private IndexIterator trainIterator = new RandomIndexIterator();
//    private IndexIterator testIterator = new RandomIndexIterator();
//
//    @Override
//    public boolean hasNextTrainTick() {
//        return trainIterator.hasNext();
//    }
//
//    @Override
//    public void trainTick() {
//
//    }
//
//    @Override
//    public void setTrain(final Instances trainInstances) {
//        for(Tickable trainedTickable : trainedTickableList) {
//            trainedTickable.setTrain(trainInstances);
//        }
//    }
//
//    @Override
//    public boolean hasNextTestTick() {
//        return testIterator.hasNext();
//    }
//
//    @Override
//    public void testTick() {
//
//    }
//
//    @Override
//    public void setTest(final Instances testInstances) {
//        for(Tickable trainedTickable : trainedTickableList) {
//            trainedTickable.setTest(testInstances);
//        }
//    }
//}
