package development.go.Ee;

import java.util.List;

public interface Selector<A, B> {

    void consider(A candidate, B value);
    List<A> getSelected();
}
