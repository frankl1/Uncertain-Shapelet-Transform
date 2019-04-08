package development.go.Ee.Selection;

import java.util.*;

public class FirstBestPerType<A> implements Selector<A> {


    @Override
    public void consider(final A candidate, final Object type) {
        A best = bestMap.get(type);
        if(best == null) {
            bestMap.put(type, candidate);
        } else {
            int comparison = comparator.compare(candidate, best);
            if(comparison > 0) {
                bestMap.put(type, candidate);
            }
        }
    }

    @Override
    public List<A> getSelected() {
        return new ArrayList<>(bestMap.values());
    }

    public FirstBestPerType(final Comparator<A> comparator) {
        this.comparator = comparator;
    }

    private Map<Object, A> bestMap = new HashMap<>();
    private final Comparator<A> comparator; // todo getters setters
}
