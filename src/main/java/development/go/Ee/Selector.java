package development.go.Ee;

import java.util.List;

public interface Selector<A> {

    void consider(A candidate);
    List<A> getSelected();
}
