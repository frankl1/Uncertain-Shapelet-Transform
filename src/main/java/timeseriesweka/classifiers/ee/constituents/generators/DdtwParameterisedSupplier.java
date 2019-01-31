package timeseriesweka.classifiers.ee.constituents.generators;

import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.ddtw.Ddtw;
import timeseriesweka.measures.dtw.Dtw;

public class DdtwParameterisedSupplier extends DtwParameterisedSupplier {
    @Override
    protected Dtw get() {
        return new Ddtw();
    }
}
