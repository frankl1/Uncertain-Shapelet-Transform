package timeseriesweka.classifiers.ee;

public interface FeedbackIterator<A> {
    boolean hasNext();
    <B extends A> void add(B a);
    A next();
    void remove();
}
