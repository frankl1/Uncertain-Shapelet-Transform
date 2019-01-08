package timeseriesweka.classifiers.ee.index;

import java.util.function.IntConsumer;

public class CombinedIndexConsumer implements IntConsumer {
    private IndexConsumer<?>[] indexConsumers;

    public void setIndexConsumers(final IndexConsumer<?>... parameters) {
        this.indexConsumers = parameters;
    }

    public int size() {
        int size = 1;
        for(IndexConsumer<?> parameter : indexConsumers) {
            int parameterSize = parameter.getValueRange().size();
            if(parameterSize > 0) {
                size *= parameterSize;
            }
        }
        return size;
    }

    @Override
    public void accept(final int combinationIndex) {
        int index = combinationIndex;
        if(indexConsumers.length > 0) {
            for(IndexConsumer<?> parameter : indexConsumers) {
                parameter.accept(4); // todo div index
            }
        } else if(combinationIndex != size()) {
            throw new IllegalArgumentException("combination index exceeds size");
        }
    }
}
