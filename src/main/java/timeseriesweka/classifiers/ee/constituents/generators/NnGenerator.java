package timeseriesweka.classifiers.ee.constituents.generators;

import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.classifiers.ee.constituents.CombinedIndexed;
import timeseriesweka.classifiers.ee.index.IndexedSupplier;
import timeseriesweka.measures.DistanceMeasure;
import utilities.Box;
import weka.core.Instances;

public abstract class NnGenerator implements IndexedSupplier<NearestNeighbour> {
    private final Box<NearestNeighbour> nearestNeighbourBox = new Box<>();
    private final CombinedIndexed parameters = new CombinedIndexed();

    @Override
    public final int size() {
        return parameters.size();
    }

    @Override
    public final NearestNeighbour get(final int index) {
        NearestNeighbour nearestNeighbour = new NearestNeighbour();
        nearestNeighbour.setDistanceMeasure(getDistanceMeasure());
        nearestNeighbourBox.setContents(nearestNeighbour);
        parameters.setValueAt(index);
        return nearestNeighbour;
    }

    protected final CombinedIndexed getParameters() {
        return parameters;
    }

    protected abstract DistanceMeasure getDistanceMeasure();

    public abstract void setParameterRanges(Instances instances);
}
