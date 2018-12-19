package timeseriesweka.classifiers.ensembles.ee.Parameter;

import utilities.Supplier;

public class Parameter<A, B> {
    private Supplier<A> supplier;
    private final ParameterInterface<A, B> parameterInterface;

    public Parameter(Supplier<A> supplier, final ParameterInterface<A, B> parameterInterface) {
        this.supplier = supplier;
        this.parameterInterface = parameterInterface;
    }

    public void set(B value) {
        parameterInterface.setParameterValue(supplier.supply(), value);
    }

    public B get() {
        return parameterInterface.getParameterValue(supplier.supply());
    }

}
