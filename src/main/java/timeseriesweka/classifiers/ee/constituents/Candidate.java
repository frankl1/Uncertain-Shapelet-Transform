package timeseriesweka.classifiers.ee.constituents;

import timeseriesweka.classifiers.Tickable;
import timeseriesweka.classifiers.ee.TickableClassifier;
import timeseriesweka.classifiers.ee.index.IndexedSupplier;
import weka.core.Instances;

public class Candidate implements Tickable {
    public Candidate(final IndexedSupplier<TickableClassifier> indexedSupplier) {
        this.indexedSupplier = indexedSupplier;
    }

    private IndexedSupplier<TickableClassifier> indexedSupplier;

    @Override
    public boolean remainingTrainTicks() {
        return false;
    }

    @Override
    public void trainTick() {

    }

    @Override
    public void setTrainInstances(final Instances trainInstances) {

    }

    @Override
    public boolean remainingTestTicks() {
        return false;
    }

    @Override
    public void testTick() {

    }

    @Override
    public void setTestInstances(final Instances testInstances) {

    }

//    @Override
//    public double[][] predict() {
//        return new double[0][];
//    }
}
