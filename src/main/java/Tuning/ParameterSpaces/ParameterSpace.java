package Tuning.ParameterSpaces;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ParameterSpace<A> {

    public ParameterSpace(final Consumer<A> parameterSetter) {
        setParameterSetter(parameterSetter);
    }

    public ParameterSpace(final Consumer<A> parameterSetter, List<? extends A> values) {
        this(parameterSetter);
        setValues(values);
    }

    public Consumer<A> getParameterSetter() {
        return parameterSetter;
    }

    public void setParameterSetter(final Consumer<A> parameterSetter) {
        this.parameterSetter = parameterSetter;
    }

    private Consumer<A> parameterSetter;

    private List<? extends A> values = new ArrayList<>(); // values of some type for the parameter

    public List<? extends A> getValues() {
        return values;
    }

    public void setValues(final List<? extends A> values) {
        this.values = values;
    }

    public int size() {
        return values.size();
    }

    public void setValueAtIndex(final Integer integer) {
        parameterSetter.accept(values.get(integer));
    }
}
