package timeseriesweka.classifiers.ee.abcdef;

import timeseriesweka.classifiers.Classifier;
import timeseriesweka.classifiers.ee.index.IndexConsumer;
import timeseriesweka.classifiers.ee.index.IndexedSupplier;

public class Generator implements IndexedSupplier<Classifier> {

    private IndexConsumer<?> parameter = null;

    @Override
    public int size() {
        return parameter.getValueRange().size();
    }

    @Override
    public Classifier get(final int index) {
        return null;
    }
}
