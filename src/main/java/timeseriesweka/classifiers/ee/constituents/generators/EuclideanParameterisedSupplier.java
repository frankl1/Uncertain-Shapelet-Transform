package timeseriesweka.classifiers.ee.constituents.generators;

import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.euclidean.Euclidean;
import weka.core.Instances;

public class EuclideanParameterisedSupplier extends ParameterisedSupplier<Euclidean> {

    @Override
    protected Euclidean get() {
        return new Euclidean();
    }

    @Override
    public void setParameterRanges(final Instances instances) {

    }
}
