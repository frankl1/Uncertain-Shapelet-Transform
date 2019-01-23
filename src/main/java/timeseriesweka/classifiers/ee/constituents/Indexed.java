package timeseriesweka.classifiers.ee.constituents;


public interface Indexed {
    int size();
    void setValueAt(final Integer value);
    Integer getIndex(); // get the current index
}
