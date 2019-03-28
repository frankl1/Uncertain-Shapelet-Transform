package timeseriesweka.classifiers.nn.Tuning;

import timeseriesweka.classifiers.AdvancedAbstractClassifier.AdvancedAbstractClassifier;
import timeseriesweka.classifiers.nn.ParameterSpaceOld;
import utilities.Utilities;

import java.util.ArrayList;
import java.util.List;

public abstract class PermutationBuilder {

    private List<Integer> getParameterSpaceSizes() {
        List<Integer> parameterSpaceSizes = new ArrayList<>();
        List<ParameterSpaceOld> parameterSpaces = getParameterSpaces();
        for(ParameterSpaceOld parameterSpace : parameterSpaces) {
            parameterSpaceSizes.add(parameterSpace.size());
        }
        return parameterSpaceSizes;
    }

    public final void setParameterPermutation(int i) {
        setParameterPermutation(Utilities.fromPermutation(i, getParameterSpaceSizes()));
    }

    protected abstract List<ParameterSpaceOld> getParameterSpaces();

    public final void setParameterPermutation(List<Integer> permutation) {
        List<ParameterSpaceOld> parameterSpaces = getParameterSpaces();
        for(int i = 0; i < permutation.size(); i++) {
            parameterSpaces.get(i).accept(permutation.get(i));
        }
    }

    public int size() {
        return Utilities.numPermutations(getParameterSpaceSizes());
    }

    protected abstract AdvancedAbstractClassifier build();
}
