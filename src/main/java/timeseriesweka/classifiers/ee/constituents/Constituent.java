package timeseriesweka.classifiers.ee.constituents;

import timeseriesweka.classifiers.AdvancedClassifier;
import timeseriesweka.classifiers.ee.constituents.generators.ParameterisedSupplier;
import timeseriesweka.classifiers.ee.index.IndexedSupplier;
import timeseriesweka.classifiers.ee.iteration.AbstractIndexIterator;
import timeseriesweka.classifiers.ee.iteration.Iterator;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class Constituent extends IndexedSupplierIterator<Classifier, ParameterisedSupplier<Classifier>> {

    public Constituent(final ParameterisedSupplier<Classifier> indexedSupplier) {
        super(indexedSupplier);
    }
}
