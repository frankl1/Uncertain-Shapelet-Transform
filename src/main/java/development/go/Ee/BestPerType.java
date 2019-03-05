package development.go.Ee;

import java.util.*;
import java.util.function.Function;

public class BestPerType<A, B> implements Selector<A> {

    @Override
    public void consider(final A candidate) {
        B type = getType.apply(candidate);
        A best = bestMap.get(type);
        if(best == null || comparator.compare(candidate, best) > 0) {
            bestMap.put(type, candidate);
        }
    }

    @Override
    public List<A> getSelected() {
        return new ArrayList<>(bestMap.values());
    }

    public BestPerType(final Function<A, B> getType, final Comparator<A> comparator) {
        this.getType = getType;
        this.comparator = comparator;
    }

    private Map<B, A> bestMap = new HashMap<>();
    private final Function<A, B> getType;
    private final Comparator<A> comparator; // todo getters setters
}
