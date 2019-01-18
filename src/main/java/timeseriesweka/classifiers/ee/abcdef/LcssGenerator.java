package timeseriesweka.classifiers.ee.abcdef;

import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.classifiers.ee.index.IndexedSupplier;
import timeseriesweka.classifiers.ee.index.IndexedSupplierObtainer;
import timeseriesweka.measures.lcss.Lcss;
import utilities.Box;
import utilities.range.ValueRange;
import weka.core.Instances;

import java.util.Arrays;

public class LcssGenerator implements IndexedSupplier<NearestNeighbour> {
    private final IndexedMutator<Lcss, Double> toleranceParameter = new IndexedMutator<>(new Mutable<Lcss, Double>() {
        @Override
        public <C extends Lcss, D extends Double> void setValue(final C subject, final D value) {
            subject.setTolerance(value);
        }

        @Override
        public <C extends Lcss> Double getValue(final C subject) {
            return subject.getTolerance();
        }
    });
    private final IndexedMutator<Lcss, Double> warpingWindowParameter = new IndexedMutator<>(new Mutable<Lcss, Double>() {
        @Override
        public <C extends Lcss, D extends Double> void setValue(final C subject, final D value) {
            subject.setWarpingWindow(value);
        }

        @Override
        public <C extends Lcss> Double getValue(final C subject) {
            return subject.getWarpingWindow();
        }
    });
    private final Box<Lcss> lcssBox = new Box<>();
    private final Box<NearestNeighbour> nearestNeighbourBox = new Box<>();
    private final TargetedMutator<Lcss> toleranceMutator = new TargetedMutator<>(toleranceParameter, lcssBox);
    private final TargetedMutator<Lcss> warpingWindowMutator = new TargetedMutator<>(warpingWindowParameter, lcssBox);
    private final CombinedIndexed parameters = new CombinedIndexed(Arrays.asList(toleranceMutator, warpingWindowMutator));

    public ValueRange<Double> getToleranceRange() {
        return toleranceParameter.getValueRange();
    }

    public ValueRange<Double> getWarpingWindowRange() {
        return warpingWindowParameter.getValueRange();
    }

    @Override
    public int size() {
        return parameters.size();
    }

    @Override
    public NearestNeighbour get(final int index) {
        NearestNeighbour nearestNeighbour = new NearestNeighbour();
        Lcss lcss = new Lcss();
        nearestNeighbour.setDistanceMeasure(lcss);
        lcssBox.setContents(lcss);
        nearestNeighbourBox.setContents(nearestNeighbour);
        parameters.setValueAt(index);
        return nearestNeighbour;
    }

    public void setRanges(Instances instances) { // todo this should probs go in ee / some static method which sets ranges based on instances
        ValueRange<Double> tolerance = getToleranceRange();
        tolerance.setIndexedSupplier(new IndexedSupplierObtainer<Double>() {
            @Override
            protected Double obtain(final double value) {
                return null; // todo scale between instance popstd
            }
        });
        // todo warpingWindow
    }
}
