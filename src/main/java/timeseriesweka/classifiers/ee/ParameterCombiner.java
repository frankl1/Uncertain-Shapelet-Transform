package timeseriesweka.classifiers.ee;

public class ParameterCombiner {
    private Parameter<?>[] parameters;

    public void setParameters(final Parameter<?>... parameters) {
        this.parameters = parameters;
    }

    public void setParameterCombination(int combinationIndex) {
        int parameterIndex = combinationIndex;
        if(parameters.length > 0) {
            for(Parameter<?> parameter : parameters) {
                parameter.accept(4); // todo
            }
        } else if(combinationIndex != size()) {
            throw new IllegalArgumentException("combination index exceeds size");
        }
    }

    public int size() {
        int size = 1;
        for(Parameter<?> parameter : parameters) {
            int parameterSize = parameter.getValueRange().size();
            if(parameterSize > 0) {
                size *= parameterSize;
            }
        }
        return size;
    }
}
