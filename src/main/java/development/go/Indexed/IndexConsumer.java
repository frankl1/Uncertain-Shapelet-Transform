package development.go.Indexed;

import java.util.function.*;

public class IndexConsumer<A> implements IntConsumer, Consumer<A> {
    public IndexConsumer(final Indexed<A> indexed, final Consumer<A> consumer) {
        this.indexed = indexed; // todo checks and use setters
        this.consumer = consumer;
    }

    public Indexed<A> getIndexed() {
        return indexed;
    }

    public void setIndexed(final Indexed<A> indexed) {
        this.indexed = indexed;
    }

    private Indexed<A> indexed;
    private Consumer<A> consumer;

    public int size() {
        return indexed.size();
    }

    @Override
    public final void accept(final int i) {
        accept(indexed.apply(i));
    }

    @Override
    public void accept(final A a) {
        consumer.accept(a);
    }

    public void setConsumer(final Consumer<A> consumer) { // todo generic extends
        this.consumer = consumer;
    }
}
