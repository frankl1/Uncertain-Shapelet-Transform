package timeseriesweka.classifiers.ensembles.ee.Indexing;

import utilities.Supplier;

import java.util.List;

public class ListElementObtainer<A> extends IndexedObtainer<A> {

    private final List<A> list;

    public ListElementObtainer(List<A> list) {
        super((Supplier<Integer>) list::size);
        this.list = list;
    }

    @Override
    protected A obtainByIndex(final Integer index) {
        return list.get(index);
    }
}
