package timeseriesweka.classifiers.ee.constituents.generators;

import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.wddtw.Wddtw;
import timeseriesweka.measures.wdtw.Wdtw;

public class WddtwParameterisedSupplier extends WdtwParameterisedSupplier {

    @Override
    protected Wdtw get() {
        return new Wddtw();
    }
}
