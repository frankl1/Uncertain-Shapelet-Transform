package timeseriesweka.classifiers.ee.iteration;

import java.util.ArrayList;
import java.util.List;

public class ElementIterator<E> implements Iterator<E> {
    public ElementIterator(AbstractIndexIterator indexIterator) {
        setIndexIterator(indexIterator);
    }

    public ElementIterator(final List<E> elements, AbstractIndexIterator indexIterator) {
        this(elements);
        setIndexIterator(indexIterator);
    }

    public ElementIterator(final List<E> elements) {
        setList(elements);
    }

    public ElementIterator() {

    }

    public void setIndexIterator(final AbstractIndexIterator indexIterator) {
        this.indexIterator = indexIterator;
    }

    public void setList(final List<E> list) {
        this.list.clear();
        this.list.addAll(list);
        indexIterator.getRange().add(this.list);
    }

    private AbstractIndexIterator indexIterator = new LinearIndexIterator();

    public AbstractIndexIterator getIndexIterator() {
        return indexIterator;
    }

    private List<E> list = new ArrayList<>();

    @Override
    public boolean hasNext() {
        return indexIterator.hasNext();
    }

    @Override
    public E next() {
        return list.get(indexIterator.next());
    }

    @Override
    public void remove() {
        indexIterator.remove();
    }

    @Override
    public void reset() {
        indexIterator.reset();
    }

    @Override
    public void setSeed(final long seed) {
        indexIterator.setSeed(seed);
    }
}
