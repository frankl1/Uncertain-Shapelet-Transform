package timeseriesweka.classifiers.ee.constituents.generators;

import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.classifiers.ee.constituents.CombinedIndexed;
import timeseriesweka.measures.DistanceMeasure;
import weka.core.Instances;

public class NnParameterisedSupplier extends ParameterisedSupplier<NearestNeighbour> {

    public ParameterisedSupplier<? extends DistanceMeasure> getDistanceMeasureParameterisedSupplier() {
        return distanceMeasureParameterisedSupplier;
    }

    public void setDistanceMeasureParameterisedSupplier(final ParameterisedSupplier<? extends DistanceMeasure> distanceMeasureParameterisedSupplier) {
        this.distanceMeasureParameterisedSupplier = distanceMeasureParameterisedSupplier;
    }

    public NnParameterisedSupplier(final ParameterisedSupplier<? extends DistanceMeasure> distanceMeasureParameterisedSupplier) {
        setDistanceMeasureParameterisedSupplier(distanceMeasureParameterisedSupplier);
    }

    private ParameterisedSupplier<? extends DistanceMeasure> distanceMeasureParameterisedSupplier;

    @Override
    protected CombinedIndexed getParameters() {
        CombinedIndexed parameters = super.getParameters();
        parameters.getIndexeds().addAll(distanceMeasureParameterisedSupplier.getParameters().getIndexeds()); // todo improve this structure
        return parameters;
    }

    @Override
    protected NearestNeighbour get() {
        NearestNeighbour nearestNeighbour = new NearestNeighbour();
        nearestNeighbour.setDistanceMeasure(distanceMeasureParameterisedSupplier.supply());
        return nearestNeighbour;
    }

    @Override
    public void setParameterRanges(final Instances instances) {
        distanceMeasureParameterisedSupplier.setParameterRanges(instances);
    }
}
