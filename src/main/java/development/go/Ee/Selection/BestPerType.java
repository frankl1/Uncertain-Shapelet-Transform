package development.go.Ee.Selection;

import java.util.*;

public class BestPerType<A> implements Selector<A> {

    @Override
    public void consider(final A candidate, final Object type) {
        List<A> bestList = bestMap.computeIfAbsent(type, key -> new ArrayList<>());
        if(bestList.isEmpty()) {
            bestList.add(candidate);
        } else {
            int comparison = comparator.compare(candidate, bestList.get(0)); // todo abstract to utility
            if(comparison >= 0) {
                if(comparison > 0) {
                    bestList.clear();
                }
                bestList.add(candidate);
            }
        }
    }

    @Override
    public List<A> getSelected() {
        List<A> list = new ArrayList<>();
        for(Map.Entry<Object, List<A>> entry : bestMap.entrySet()) {
            List<A> values = entry.getValue();
            list.add(values.get(random.nextInt(values.size())));
        }
        return list;
    }

    public BestPerType(final Comparator<A> comparator) {
        this.comparator = comparator;
    }

    private Random random = new Random(); // todo set seed / random
    private Map<Object, List<A>> bestMap = new HashMap<>();
    private final Comparator<A> comparator; // todo getters setters
}
