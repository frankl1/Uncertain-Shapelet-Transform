package timeseriesweka.classifiers.ee;

import java.util.function.Consumer;

public class Parameter<A> implements Consumer<Integer> {
    public Parameter(final Consumer<A> setter, final ValueRange<A> valueRange) {
        this.setter = setter;
        this.valueRange = valueRange;
    }

    @Override
    public void accept(final Integer index) {
        setter.accept(valueRange.get(index));
    }

    private final Consumer<A> setter;

    private final ValueRange<A> valueRange;

    public ValueRange<A> getValueRange() {
        return valueRange;
    }
}
