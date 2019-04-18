package ee.iteration;

import java.util.List;
import java.util.Random;

public class RandomReplacedIndexIterator extends RandomReplacedIterator<Integer> {
    public RandomReplacedIndexIterator(final List<Integer> list, final Random random, final int limit) {
        super(list, random, limit);
    }

    public RandomReplacedIndexIterator(final List<Integer> list, final Random random) {
        super(list, random);
    }
}
