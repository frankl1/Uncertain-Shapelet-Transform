package timeseriesweka.classifiers.ee.constituents;

import timeseriesweka.classifiers.Classifier;
import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.classifiers.ee.index.IndexConsumer;
import timeseriesweka.measures.euclidean.Euclidean;

public class EuclideanConstituent extends Constituent {

    @Override
    protected Classifier build() {
        NearestNeighbour nearestNeighbour = new NearestNeighbour();
        nearestNeighbour.setDistanceMeasure(new Euclidean());
        return nearestNeighbour;
    }

    @Override
    protected IndexConsumer<?>[] getParameters() {
        return new IndexConsumer<?>[0];
    }
}
