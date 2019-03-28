package timeseriesweka.classifiers.nn;

import development.go.Indexed.Indexed;
import development.go.Indexed.IndexedValues;

import java.util.List;
import java.util.function.Consumer;

public class ParameterSpaceOld<A> implements IndexedConsumer {
    private final Consumer<A> consumer;

    public ParameterSpaceOld(final Consumer<A> consumer, final Indexed<A> values) {
        this.consumer = consumer;
        this.values = values;
    }

    public ParameterSpaceOld(final Consumer<A> consumer, final List<A> values) {
        this.consumer = consumer;
        setValues(values);
    }

    private Indexed<A> values;

    public Indexed<A> getValues() {
        return values;
    }

    public void setValues(final Indexed<A> values) {
        this.values = values;
    }

    public void setValues(final List<A> values) {
        setValues(new IndexedValues<>(values));
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public void accept(final int i) {
        consumer.accept(values.apply(i));
    }
}
