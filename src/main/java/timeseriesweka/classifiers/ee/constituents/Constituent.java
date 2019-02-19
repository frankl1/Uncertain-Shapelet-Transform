package timeseriesweka.classifiers.ee.constituents;

import timeseriesweka.classifiers.ee.TrainedTickableClassifier;
import timeseriesweka.classifiers.ee.constituents.generators.ParameterisedSupplier;

public class Constituent extends IndexedSupplierIterator<TrainedTickableClassifier, ParameterisedSupplier<TrainedTickableClassifier>> {

    public Constituent(final ParameterisedSupplier<TrainedTickableClassifier> indexedSupplier) {
        super(indexedSupplier);
    }
}
