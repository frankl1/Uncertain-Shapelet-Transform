package timeseriesweka.classifiers.nn;

import utilities.Utilities;

import java.util.*;
import java.util.function.Consumer;

public class ParametersSpace {
    private final List<ParameterSpace<?>> parameterSpaceSet = new ArrayList<>();

    public boolean add(ParameterSpace<?> parameterSpace) {
        return parameterSpaceSet.add(parameterSpace);
    }

    public boolean remove(ParameterSpace<?> parameterSpace) {
        return parameterSpaceSet.remove(parameterSpace);
    }

    private List<Integer> getParameterSizes() {
        List<Integer> sizes = new ArrayList<>();
        for(ParameterSpace parameterSpace : parameterSpaceSet) {
            sizes.add(parameterSpace.size());
        }
        return sizes;
    }

    public int size() {
        return Utilities.numPermutations(getParameterSizes());
    }

    public int numParameters() {
        return parameterSpaceSet.size();
    }

    public void setParameterPermutation(final int index) {
        List<Integer> indices = Utilities.fromPermutation(index, getParameterSizes());
        for(int i = 0; i < parameterSpaceSet.size(); i++) {
            ParameterSpace<?> parameterSpace = parameterSpaceSet.get(i);
            parameterSpace.setValueAtIndex(indices.get(i));
        }
    }
}
