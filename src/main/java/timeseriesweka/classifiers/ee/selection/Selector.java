package timeseriesweka.classifiers.ee.selection;

import java.util.List;
import java.util.Random;

public interface Selector<A> {
    void considerCandidate(A candidate);
    List<A> getSelected();
    void setRandom(Random random);
    Random getRandom();
    void clear();
}
