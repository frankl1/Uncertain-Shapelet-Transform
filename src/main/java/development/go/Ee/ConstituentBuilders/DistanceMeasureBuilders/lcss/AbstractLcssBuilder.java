package development.go.Ee.ConstituentBuilders.DistanceMeasureBuilders.lcss;

import development.go.Ee.ConstituentBuilders.DistanceMeasureBuilders.dtw.AbstractDtwBuilder;
import development.go.Indexed.Indexed;
import development.go.Indexed.IndexedValues;
import timeseriesweka.measures.lcss.Lcss;
import weka.core.Instances;

import java.util.List;

public abstract class AbstractLcssBuilder<A extends Lcss> extends AbstractDtwBuilder<A> {

    private Indexed<Double> toleranceValues;

    @Override
    protected void setupParameters(final Instances instances) {

        toleranceValues = new IndexedValues<>(); // todo
    }

    @Override
    protected List<Integer> getParameterSizes() {
        List<Integer> parameterSizes = super.getParameterSizes();
        parameterSizes.add(toleranceValues.size());
        return parameterSizes;
    }

    @Override
    protected int getNumParameters() {
        return super.getNumParameters() + 1;
    }

    @Override
    public void configure(final A lcss, List<Integer> parametersPermutation) {
        int numSuperParameters = super.getNumParameters();
        double toleranceValue = parametersPermutation.get(numSuperParameters + 1);
        lcss.setTolerance(toleranceValue);
    }

}
