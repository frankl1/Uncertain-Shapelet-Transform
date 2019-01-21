package timeseriesweka.classifiers.ee.abcdef.generators;

import timeseriesweka.classifiers.ee.abcdef.Indexed;
import timeseriesweka.classifiers.ee.abcdef.IndexedMutator;
import timeseriesweka.classifiers.ee.abcdef.TargetedMutator;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;
import timeseriesweka.measures.euclidean.Euclidean;
import timeseriesweka.measures.wdtw.Wdtw;

import java.util.List;

public class EuclideanGenerator extends NnGenerator {

    @Override
    protected DistanceMeasure getDistanceMeasure() {
        return new Euclidean();
    }
}
