package development.go.Indexed;

import java.util.function.Function;
import java.util.function.IntFunction;

public interface Indexed<A> extends IntFunction<A> {
    int size();
}
