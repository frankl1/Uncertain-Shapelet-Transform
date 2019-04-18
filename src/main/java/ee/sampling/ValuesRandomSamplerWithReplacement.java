package ee.sampling;

import ee.Randomised;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ValuesRandomSamplerWithReplacement<A> implements Iterator<A>, Randomised {

    Random random = new Random();
    final List<A> values;

    public ValuesRandomSamplerWithReplacement(List<A> values) {
        this.values = new ArrayList<>(values);
    }

    @Override
    public void setRandom(Random random) {
        this.random = random;
    }

    @Override
    public Random getRandom() {
        return random;
    }

    @Override
    public boolean hasNext() {
        return !values.isEmpty();
    }

    @Override
    public A next() {
        return values.get(random.nextInt(values.size()));
    }
}
