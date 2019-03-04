package utilities;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Box<A> implements Boxed<A> {
    private A contents;

    public Box(A contents) {accept(contents);
    }

    public Box() {
        this(null);
    }
    @Override
    public void accept(final A contents) {
        this.contents = contents;
    }

    @Override
    public A get() {
        return contents;
    }
}
