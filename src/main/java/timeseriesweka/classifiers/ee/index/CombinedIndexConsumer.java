package timeseriesweka.classifiers.ee.index;

import utilities.Utilities;

import java.util.function.IntConsumer;

public class CombinedIndexConsumer implements IntConsumer {
    private IndexConsumer<?>[] indexConsumers;

    public void setIndexConsumers(final IndexConsumer<?>... parameters) {
        this.indexConsumers = parameters;
    }

    public int size() {
        return Utilities.numCombinations(getSizes());
    }

    private int[] getSizes() {
        int[] sizes = new int[indexConsumers.length];
        for(int i = 0; i < sizes.length; i++) {
            sizes[i] = indexConsumers[i].getValueRange().size();
        }
        return sizes;
    }

    @Override
    public void accept(int combinationIndex) {
        int[] values = Utilities.fromCombination(combinationIndex, getSizes());
        for(int i = 0; i < values.length; i++) {
            indexConsumers[i].accept(values[i]);
        }
    }
}
