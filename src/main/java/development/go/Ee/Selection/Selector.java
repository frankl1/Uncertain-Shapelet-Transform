package development.go.Ee.Selection;

import java.util.List;

public interface Selector<A> {

    void consider(A candidate, Object value);
    List<A> getSelected();
}
