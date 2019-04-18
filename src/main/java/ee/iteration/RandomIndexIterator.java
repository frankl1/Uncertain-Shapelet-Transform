package ee.iteration;

import java.util.List;
import java.util.Random;

public class RandomIndexIterator extends RandomIterator<Integer> {

    public RandomIndexIterator(final List<Integer> list, final Random random) {
        super(list, random);
    }

    public RandomIndexIterator(final List<Integer> list, final Random random, final int limit) {
        super(list, random, limit);
    }
}
