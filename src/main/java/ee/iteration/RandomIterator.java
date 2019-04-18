package ee.iteration;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RandomIterator<A> extends RandomReplacedIterator<A> {

    public RandomIterator(List<A> list, Random random) {
        super(list, random);
    }

    public RandomIterator(List<A> list, Random random, int limit) {
        super(list, random, limit);
    }

    @Override
    public A next() {
        this.count++;
        return list.remove(random.nextInt(list.size()));
    }
}
