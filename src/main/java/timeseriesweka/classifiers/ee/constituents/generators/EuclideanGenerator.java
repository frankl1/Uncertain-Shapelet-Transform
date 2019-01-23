package timeseriesweka.classifiers.ee.constituents.generators;

import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.euclidean.Euclidean;
import weka.core.Instances;

public class EuclideanGenerator extends NnGenerator {

    @Override
    protected DistanceMeasure getDistanceMeasure() {
        return new Euclidean();
    }

    @Override
    public void setParameterRanges(final Instances instances) {

    }
}
