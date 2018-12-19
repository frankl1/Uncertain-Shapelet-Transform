package timeseriesweka.classifiers.ensembles.ee.Parameter;

import timeseriesweka.classifiers.ensembles.ee.Indexing.Indexer;
import timeseriesweka.classifiers.ensembles.ee.Indexing.IndexedObtainer;

public class PopulatedParameter<B> implements Indexer {
    private final Parameter<?, B> parameter;
    private final IndexedObtainer<B> indexedObtainer;

    public IndexedObtainer<B> getIndexedObtainer() {
        return indexedObtainer;
    }

    public PopulatedParameter(final Parameter<?, B> parameter, final IndexedObtainer<B> indexedObtainer) {
        this.parameter = parameter;
        this.indexedObtainer = indexedObtainer;
        setIndex(index);
    }

    private int index = 0;

    @Override
    public void setIndex(final int index) {
        this.index = index;
        parameter.set(indexedObtainer.obtain(index));
    }

    @Override
    public int getIndex() {
        return index;
    }

    public int getSize() {
        return indexedObtainer.getSize();
    }

}
