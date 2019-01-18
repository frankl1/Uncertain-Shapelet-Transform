package timeseriesweka.classifiers.ee.abcdef;

public interface Indexed {
    int size();
    <D extends Integer> void setValueAt(final D value);
    Integer getIndex(); // get the current index
}
