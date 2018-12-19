package timeseriesweka.classifiers.ensembles.ee.Indexing;

import utilities.Supplier;

public class IndexerObtainer<A> extends IndexedObtainer<A> {
    private final Indexer indexer;
    private final Supplier<A> supplier;

    public IndexerObtainer(final Indexer indexer, final Supplier<A> supplier) {
        super(new Supplier<Integer>() {
            @Override
            public Integer supply() {
                return indexer.getSize();
            }
        });
        this.indexer = indexer;
        this.supplier = supplier;
    }

    @Override
    protected A obtainByIndex(final Integer index) {
        A subject = supplier.supply();
        indexer.setIndex(index);
        return subject;
    }
}
