package utilities;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Box<A> implements Supplier<A>, Consumer<A> {
    private A contents;

    public Box(A contents) {
        setContents(contents);
    }

    public Box() {
        this(null);
    }

    public void setContents(A contents) {
        this.contents = contents;
    }

    public A getContents() {
        return contents;
    }

    @Override
    public void accept(final A a) {
        setContents(a);
    }

    @Override
    public A get() {
        return getContents();
    }
}
