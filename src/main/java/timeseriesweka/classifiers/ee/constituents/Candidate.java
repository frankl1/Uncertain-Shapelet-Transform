//package timeseriesweka.classifiers.ee.constituents;
//
//import timeseriesweka.classifiers.Tickable;
//import timeseriesweka.classifiers.ee.TrainedTickableClassifier;
//import timeseriesweka.classifiers.ee.index.IndexedSupplier;
//import weka.core.Instances;
//
//public class Candidate implements Tickable {
//    public Candidate(final IndexedSupplier<TrainedTickableClassifier> indexedSupplier) {
//        this.indexedSupplier = indexedSupplier;
//    }
//
//    private IndexedSupplier<TrainedTickableClassifier> indexedSupplier;
//
//    @Override
//    public boolean hasNextTrainTick() {
//        return false;
//    }
//
//    @Override
//    public void trainTick() {
//
//    }
//
//    @Override
//    public void setTrain(final Instances trainInstances) {
//
//    }
//
//    @Override
//    public boolean hasNextTestTick() {
//        return false;
//    }
//
//    @Override
//    public void testTick() {
//
//    }
//
//    @Override
//    public void setTest(final Instances testInstances) {
//
//    }
//
////    @Override
////    public double[][] predict() {
////        return new double[0][];
////    }
//}
