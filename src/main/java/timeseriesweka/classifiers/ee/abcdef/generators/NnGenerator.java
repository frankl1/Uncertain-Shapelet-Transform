package timeseriesweka.classifiers.ee.abcdef.generators;

import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.classifiers.ee.abcdef.CombinedIndexed;
import timeseriesweka.classifiers.ee.index.IndexedSupplier;
import timeseriesweka.classifiers.ee.index.LinearInterpolater;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.lcss.Lcss;
import utilities.Box;
import utilities.StatisticUtilities;
import utilities.range.ValueRange;
import weka.core.DistanceFunction;
import weka.core.Instances;

import java.util.Arrays;

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
