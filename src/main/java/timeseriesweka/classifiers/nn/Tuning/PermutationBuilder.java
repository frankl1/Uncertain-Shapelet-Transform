package timeseriesweka.classifiers.nn.Tuning;

import timeseriesweka.classifiers.AdvancedAbstractClassifier.AdvancedAbstractClassifier;
import timeseriesweka.classifiers.nn.ParameterSpace;
import utilities.Utilities;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

public abstract class PermutationBuilder {

    private List<Integer> getParameterSpaceSizes() {
        List<Integer> parameterSpaceSizes = new ArrayList<>();
        List<ParameterSpace> parameterSpaces = getParameterSpaces();
        for(ParameterSpace parameterSpace : parameterSpaces) {
            parameterSpaceSizes.add(parameterSpace.size());
        }
        return parameterSpaceSizes;
    }

    public final void setParameterPermutation(int i) {
        setParameterPermutation(Utilities.fromPermutation(i, getParameterSpaceSizes()));
    }

    protected abstract List<ParameterSpace> getParameterSpaces();

    public final void setParameterPermutation(List<Integer> permutation) {
        List<ParameterSpace> parameterSpaces = getParameterSpaces();
        for(int i = 0; i < permutation.size(); i++) {
            parameterSpaces.get(i).accept(permutation.get(i));
        }
    }

    public int size() {
        return Utilities.numPermutations(getParameterSpaceSizes());
    }

    protected abstract AdvancedAbstractClassifier build();
}
