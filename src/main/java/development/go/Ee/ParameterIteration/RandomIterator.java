package development.go.Ee.ParameterIteration;

import java.util.*;

public class RandomIterator<A> implements Iterator<A> {

    private Random random = new Random();
    private final List<A> source = new ArrayList<>();

    @Override
    public void setSource(final List<A> source) {
        this.source.clear();
        this.source.addAll(source);
    }

    @Override
    public boolean hasNext() {
        return !source.isEmpty();
    }

    @Override
    public A next() {
        return source.remove(0);
    }

    @Override
    public void setRandom(final Random random) {
        this.random = random;
    }

    @Override
    public void setSeed(final long seed) {
        random.setSeed(seed);
    }
}
