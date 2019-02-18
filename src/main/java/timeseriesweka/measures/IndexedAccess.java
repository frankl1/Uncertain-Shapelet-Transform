package timeseriesweka.measures;

public interface IndexedAccess<A> {
    A get(int index);
    int size();
}
