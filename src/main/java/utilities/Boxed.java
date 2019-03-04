package utilities;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface Boxed<A> extends Consumer<A>, Supplier<A> {
}
