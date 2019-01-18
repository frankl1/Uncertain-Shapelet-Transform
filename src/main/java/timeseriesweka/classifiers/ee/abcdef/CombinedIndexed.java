package timeseriesweka.classifiers.ee.abcdef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CombinedIndexed implements Indexed {
    private final List<Indexed> indexeds = new ArrayList<>();

    public CombinedIndexed(Collection<Indexed> indexeds) {
        this.indexeds.addAll(indexeds);
    }

    public List<Indexed> getAttachedIndexeds() {
        return indexeds;
    }

    @Override
    public <D extends Integer> void setValueAt(final D combination) {
        int combo = combination;
        for(int i = 0; i < indexeds.size(); i++) {
            Indexed indexedMutator = indexeds.get(i);
            int size = indexedMutator.size();
            int value = combo % size;
            combo /= size;
            indexedMutator.setValueAt(value);
        }
    }

    @Override
    public Integer getIndex() {
        throw new UnsupportedOperationException(); // todo get the current index
    }

    @Override
    public int size() {
        int overallSize = 1;
        for(Indexed indexedMutator : indexeds) {
            int size = indexedMutator.size();
            if(size > 0) {
                overallSize *= size;
            }
        }
        return overallSize;
    }
}
