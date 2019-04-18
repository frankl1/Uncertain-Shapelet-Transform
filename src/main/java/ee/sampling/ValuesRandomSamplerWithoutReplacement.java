package ee.sampling;

import java.util.List;

public class ValuesRandomSamplerWithoutReplacement<A> extends ValuesRandomSamplerWithReplacement<A> {

    public ValuesRandomSamplerWithoutReplacement(List<A> values) {
        super(values);
    }

    @Override
    public A next() {
        return values.remove(random.nextInt(values.size()));
    }
}
