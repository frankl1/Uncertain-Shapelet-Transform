package development.go;

import java.util.List;
import java.util.function.BiConsumer;

public class ParameterSpace<A, B> {
    private BiConsumer<A, B> setter;

    public BiConsumer<A, B> getSetter() {
        return setter;
    }

    public void setSetter(final BiConsumer<A, B> setter) {
        this.setter = setter;
    }

    public List<? extends B> getSpace() {
        return space;
    }

    public void setSpace(final List<? extends B> space) {
        this.space = space;
    }

    private List<? extends B> space;

    public ParameterSpace(final BiConsumer<A, B> setter, final List<? extends B> space) {
        this.setter = setter;
        this.space = space;
    }

    public <C extends A> void setByIndex(C subject, int index) {
        setter.accept(subject, space.get(index));
    }
}
