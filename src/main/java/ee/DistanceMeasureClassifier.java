package timeseriesweka.classifiers.ensembles.ee;

import timeseriesweka.measures.DistanceMeasure;
import utilities.Reproducible;

import java.io.Serializable;

public interface DistanceMeasureClassifier extends Classifier, Serializable, Reproducible {
    void setDistanceMeasure(DistanceMeasure distanceMeasure);
}
