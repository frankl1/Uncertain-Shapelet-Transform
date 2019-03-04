package utilities;

import java.util.function.Supplier;

public abstract class Cached<A> implements Supplier<A> {

    private A value;
    private boolean valid = true;

    protected abstract A supply();

    @Override
    public A get() {
        if(valid) {
            return value;
        } else {
            value = supply();
            valid = true;
            return value;
        }
    }

    public void invalidate() {
        valid = false;
    }
}
