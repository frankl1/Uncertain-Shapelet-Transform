package timeseriesweka.classifiers.ee.abcdef;


public interface Indexed {
    int size();
    void setValueAt(final Integer value);
    Integer getIndex(); // get the current index
}
