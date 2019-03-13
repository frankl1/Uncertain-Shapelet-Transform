package development.go.Ee.ConstituentBuilders;

import timeseriesweka.classifiers.nn.Nn;
import timeseriesweka.measures.DistanceMeasure;
import utilities.Utilities;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ConstituentBuilder<A extends DistanceMeasure> implements Builder<Nn> {

    private List<Integer> nnParameterPermutation;
    private List<Integer> distanceMeasureParameterPermutation;

    private void setParameterPermutation(List<Integer> parameterPermutation) {
        distanceMeasureParameterPermutation = new ArrayList<>(parameterPermutation);
        nnParameterPermutation = new ArrayList<>();
        int numNnParameters = getNnParameterSizes().size();
        for(int i = 0; i < numNnParameters; i++) {
            nnParameterPermutation.add(distanceMeasureParameterPermutation.remove(i));
        }
    }

    public final List<Integer> getParameterSizes() {
        List<Integer> parameterSizes = getNnParameterSizes();
        parameterSizes.addAll(getDistanceMeasureParameterSizes());
        return parameterSizes;
    }

    public List<Integer> getNnParameterSizes() {
        return new ArrayList<>();
    }

    public abstract List<Integer> getDistanceMeasureParameterSizes();

    public void setParameterPermutation(int permutation) {
        setParameterPermutation(Utilities.fromCombination(permutation, getParameterSizes()));
    }

    protected List<Integer> getParameterPermutation() {
        List<Integer> parameterPermutation = new ArrayList<>(nnParameterPermutation);
        parameterPermutation.addAll(distanceMeasureParameterPermutation);
        return parameterPermutation;
    }

    public void setUpParameters(final Instances instances) {

    }

    public abstract A getDistanceMeasure();

    public void configureDistanceMeasure(A distanceMeasure, List<Integer> parameterPermutation) {

    }

    public Nn getNn() {
        return new Nn();
    }

    public void configureNn(Nn nn, List<Integer> parameterPermutation) {

    }

    public int size() {
        return Utilities.numPermutations(getParameterSizes());
    }

    @Override
    public Nn build() {
        Nn nn = getNn();
        configureNn(nn, new ArrayList<>(nnParameterPermutation));
        A distanceMeasure = getDistanceMeasure();
        configureDistanceMeasure(distanceMeasure, new ArrayList<>(distanceMeasureParameterPermutation));
        nn.setDistanceMeasure(distanceMeasure);
        return nn;
    }
}
