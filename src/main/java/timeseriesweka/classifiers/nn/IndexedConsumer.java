package timeseriesweka.classifiers.nn;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

public interface IndexedConsumer extends IntConsumer {
    int size();
}
