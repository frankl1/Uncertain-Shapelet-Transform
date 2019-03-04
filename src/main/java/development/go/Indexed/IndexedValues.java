package development.go.Indexed;

import java.util.List;

public class IndexedValues<A> implements Indexed<A> {

    public IndexedValues() {

    }

    public IndexedValues(List<? extends A> values) {
        setValues(values);
    }

    public List<? extends A> getValues() {
        return values;
    }

    private List<? extends A> values;

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public A apply(final int i) {
        return values.get(i);
    }

    public void setValues(final List<? extends A> values) {
        if(values == null) throw new NullPointerException();
        this.values = values;
    }
}
