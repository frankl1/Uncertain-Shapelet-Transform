package development.go.Ee.ParameterIteration;

import java.util.*;

public class RandomRoundRobinIterator<A> implements SourcedIterator<A, List<A>> {
    private Random random = new Random();
    private final List<A> source = new ArrayList<>();
    private final List<A> unpicked = new ArrayList<>();

    @Override
    public void setSource(final List<A> source) {
        source.clear();
        unpicked.clear();
        source.addAll(source);
        unpicked.addAll(source);
    }

    @Override
    public void setRandom(final Random random) {
        this.random = random;
    }

    @Override
    public void setSeed(final long seed) {
        random.setSeed(seed);
    }

    @Override
    public boolean hasNext() {
        return !source.isEmpty();
    }

    @Override
    public A next() {
        A element = unpicked.remove(random.nextInt(unpicked.size()));
        if(unpicked.isEmpty()) {
            unpicked.addAll(source);
        }
        return element;
    }
}
