package ee.sampling;

import java.util.HashSet;
import java.util.Set;

public class DistributionRandomRandomSamplerWithoutReplacement<A> extends DistributionRandomSamplerWithReplacement<A> {

    public DistributionRandomRandomSamplerWithoutReplacement(Distribution<A> distribution) {
        super(distribution);
    }

    private Set<A> picked = new HashSet<>();

    @Override
    public A next() {
        A next = super.next();
        while (picked.contains(next)) {
            next = super.next();
        }
        picked.add(next);
        return next;
    }
}
