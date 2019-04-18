package ee.iteration;

import utilities.Utilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class RandomReplacedIterator<A> implements Iterator<A> {

    protected final List<A> list;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    private int limit;
    public static final int NO_LIMIT = -1;

    public RandomReplacedIterator(List<A> list, Random random, int limit) {
        this.limit = limit;
        this.random = random;
        this.list = new ArrayList<>(list);
    }


    public RandomReplacedIterator(List<A> list, Random random) {
        this(list, random, NO_LIMIT);
    }

    protected final Random random;
    protected int count = 0;

    @Override
    public boolean hasNext() {
        return !list.isEmpty() && (limit < 0 || count < limit);
    }

    @Override
    public A next() {
        this.count++;
        return list.get(random.nextInt(list.size()));
    }

}
