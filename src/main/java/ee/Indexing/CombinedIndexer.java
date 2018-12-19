package timeseriesweka.classifiers.ensembles.ee.Indexing;

import java.util.HashSet;
import java.util.Set;

public class CombinedIndexer implements Indexer {

    private Integer combinationIndex = null;
    private final Set<Indexer> indexers = new HashSet<>();

    public Set<Indexer> getIndexers() {
        return indexers;
    }

    public Integer getParameterValueCombination() {
        return combinationIndex;
    }

    @Override
    public void setIndex(int combinationIndex) {
        if(combinationIndex > getSize() || combinationIndex < 0) {
            throw new IllegalArgumentException("out of range");
        }
        this.combinationIndex = combinationIndex;
        for(Indexer indexer : indexers) {
            int numValues = indexer.getSize();
            if(numValues != 0) {
                int valueIndex = combinationIndex % numValues;
                indexer.setIndex(valueIndex);
                combinationIndex /= numValues;
            }
        }
    }

    @Override
    public int getIndex() {
        return combinationIndex;
    }

    @Override
    public int getSize() {
        int size = 1;
        for(Indexer indexer : indexers) {
            int indexSize = indexer.getSize();
            if(indexSize != 0) {
                size *= indexSize;
            }
        }
        return size;
    }
}
