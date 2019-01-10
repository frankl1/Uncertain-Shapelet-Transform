package timeseriesweka.classifiers.ee.selection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BestPerTypeSelector<A> implements Selector<A> {

    private final Map<String, Weighted<A>> selected = new HashMap<>();

    @Override
    public void consider(final A a, double stat) {
        String key = a.toString();
        Weighted<A> weighted = selected.get(key);
        if(weighted == null || stat > weighted.getWeight()) {
            // replace
            selected.put(key, new Weighted<>(a, stat));
        }
    }

    @Override
    public Collection<Weighted<A>> getSelected() {
        return selected.values();
    }

    @Override
    public void reset() {
        selected.clear();
    }

    @Override
    public void setSeed(final long seed) {

    }
}
