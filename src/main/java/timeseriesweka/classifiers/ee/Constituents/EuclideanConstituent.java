package timeseriesweka.classifiers.ee.Constituents;

import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.classifiers.ee.Parameter;
import timeseriesweka.measures.euclidean.Euclidean;
import weka.classifiers.Classifier;

public class EuclideanConstituent extends Constituent {

    @Override
    protected Classifier build() {
        NearestNeighbour nearestNeighbour = new NearestNeighbour();
        nearestNeighbour.setDistanceMeasure(new Euclidean());
        return nearestNeighbour;
    }

    @Override
    protected Parameter<?>[] getParameters() {
        return new Parameter<?>[0];
    }
}
