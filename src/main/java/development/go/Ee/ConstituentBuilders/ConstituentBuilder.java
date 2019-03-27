package development.go.Ee.ConstituentBuilders;

import timeseriesweka.classifiers.nn.AbstractNn;
import timeseriesweka.classifiers.nn.Nn;
import timeseriesweka.measures.DistanceMeasure;
import utilities.Utilities;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

public abstract class ConstituentBuilder<A extends DistanceMeasure> implements PermutedBuilder<AbstractNn> {

    private List<Integer> parameterPermutation;
    private Integer parameterPermutationSingular;

    private void setParameterPermutation(List<Integer> parameterPermutation) {
        this.parameterPermutation = parameterPermutation;
        parameterPermutationSingular = null;
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
        parameterPermutationSingular = permutation;
        parameterPermutation = null;
    }

    protected List<Integer> getParameterPermutation() {
        return new ArrayList<>(parameterPermutation);
    }

    public void setUpParameters(final Instances instances) {

    }

    public abstract A getDistanceMeasure();

    public void configureDistanceMeasure(A distanceMeasure, List<Integer> parameterPermutation) {

    }

    public Nn getNn() {
        return new Nn();
    }

    public void configureNn(AbstractNn nn, List<Integer> parameterPermutation) {

    }

    public int size() {
        return Utilities.numPermutations(getParameterSizes());
    }

    @Override
    public final void setPermutation(final int permutation) {
        setParameterPermutation(permutation);
    }

    @Override
    public final void useInstances(final Instances instances) {
        setUpParameters(instances);
    }

    @Override
    public AbstractNn build() {
        if(parameterPermutation == null) {
            parameterPermutation = Utilities.fromPermutation(parameterPermutationSingular, getParameterSizes());
            parameterPermutationSingular = null;
        }
        ArrayList<Integer> distanceMeasureParameterPermutation = new ArrayList<>(parameterPermutation);
        ArrayList<Integer> nnParameterPermutation = new ArrayList<>();
        int numNnParameters = getNnParameterSizes().size();
        for(int i = 0; i < numNnParameters; i++) {
            nnParameterPermutation.add(distanceMeasureParameterPermutation.remove(i));
        }
        Nn nn = getNn();
        configureNn(nn, new ArrayList<>(nnParameterPermutation));
        A distanceMeasure = getDistanceMeasure();
        configureDistanceMeasure(distanceMeasure, new ArrayList<>(distanceMeasureParameterPermutation));
        nn.setDistanceMeasure(distanceMeasure);
        return nn;
    }
}
