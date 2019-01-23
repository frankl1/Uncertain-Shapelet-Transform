package timeseriesweka.classifiers.ee.index;

import java.util.ArrayList;
import java.util.List;

public class ElementObtainer<A> implements IndexedSupplier<A> {

    public void setList(final List<A> list) {
        this.list = list;
    }

    private List<A> list = new ArrayList<>();

    public List<A> getList() {
        return list;
    }

    public ElementObtainer() {

    }

    public ElementObtainer(List<A> list) {
        setList(list);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public A get(final int index) {
        return list.get(index);
    }
}
