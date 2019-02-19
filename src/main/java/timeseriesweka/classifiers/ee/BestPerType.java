package timeseriesweka.classifiers.ee;

import java.util.*;
import java.util.function.Function;

public class BestPerType<A, B> implements Selector<A> {
    private final Map<B, TreeSet<A>> map = new HashMap<>();

    private final Function<A, B> getType;
    private final Comparator<A> comparator;

    public int getNumPerType() {
        return numPerType;
    }

    public void setNumPerType(final int numPerType) {
        this.numPerType = numPerType;
    }

    private int numPerType = 1;

    public BestPerType(final Function<A, B> getType, final Comparator<A> comparator) {
        this.getType = getType;
        this.comparator = comparator;
    }

    @Override
    public boolean add(final A a) {
        boolean result = false;
        TreeSet<A> set = map.computeIfAbsent(getType.apply(a), key -> new TreeSet<>(comparator));
        if(comparator.compare(set.last(), a) == 0) {
            if(set.size() > numPerType) {
                set.pollLast();
                result = true;
            }
        }
        set.add(a);
        return result;
    }

    @Override
    public A[] getSelected() {
        return null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + numPerType;
    }
}
