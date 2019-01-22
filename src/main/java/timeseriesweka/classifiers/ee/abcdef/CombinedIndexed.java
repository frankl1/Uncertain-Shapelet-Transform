package timeseriesweka.classifiers.ee.abcdef;

import timeseriesweka.classifiers.ee.index.CombinedIndexConsumer;
import utilities.Utilities;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CombinedIndexed implements Indexed {
    private final List<Indexed> indexeds = new ArrayList<>();

    public CombinedIndexed(Collection<Indexed> indexeds) {
        this.indexeds.addAll(indexeds);
    }

    public CombinedIndexed() {

    }

    public List<Indexed> getIndexeds() {
        return indexeds;
    }

    private int[] getSizes() {
        int[] sizes = new int[indexeds.size()];
        for(int i = 0; i < sizes.length; i++) {
            sizes[i] = indexeds.get(i).size();
        }
        return sizes;
    }

    @Override
    public void setValueAt(final Integer combination) {
        if(indexeds.size() > 0) {
            int[] values = Utilities.fromCombination(combination, getSizes());
            for(int i = 0; i < indexeds.size(); i++) {
                indexeds.get(i).setValueAt(values[i]);
            }
        } else if(combination != 0) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Integer getIndex() {
        throw new UnsupportedOperationException(); // todo get the current index
    }

    @Override
    public int size() {
        return Utilities.numCombinations(getSizes());
    }

}
