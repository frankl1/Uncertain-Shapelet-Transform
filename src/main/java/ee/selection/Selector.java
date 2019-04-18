package ee.selection;

import java.util.List;

public interface Selector<A> {
    void considerCandidate(A candidate);
    List<A> getSelected();
}
