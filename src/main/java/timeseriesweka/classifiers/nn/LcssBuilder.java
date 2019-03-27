package timeseriesweka.classifiers.nn;

import development.go.Indexed.Indexed;
import development.go.Indexed.IndexedValues;
import timeseriesweka.classifiers.AdvancedAbstractClassifier.AdvancedAbstractClassifier;
import timeseriesweka.classifiers.nn.Specialised.LcssNn;
import timeseriesweka.classifiers.nn.Tuning.PermutationBuilder;
import timeseriesweka.measures.lcss.Lcss;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LcssBuilder extends PermutationBuilder {

    private double toleranceValue = Lcss.DEFAULT_TOLERANCE;
    private double warpingWindowValue = Lcss.DEFAULT_WARPING_WINDOW;
    private final ParameterSpace<Double> toleranceSpace = new ParameterSpace<>(v -> toleranceValue = v, new IndexedValues<>());
    private final ParameterSpace<Double> warpingWindowSpace = new ParameterSpace<>(v -> warpingWindowValue = v, new IndexedValues<>());

    public void setToleranceValues(Indexed<Double> toleranceValues) {
        toleranceSpace.setValues(toleranceValues);
    }

    public Indexed<Double> getToleranceValues() {
        return toleranceSpace.getValues();
    }

    public void setToleranceValues(List<Double> toleranceValues) {
        setToleranceValues(new IndexedValues<>(toleranceValues));
    }

    public void setWarpingWindowValues(Indexed<Double> warpingWindowValues) {
        warpingWindowSpace.setValues(warpingWindowValues);
    }

    public void setWarpingValues(List<Double> warpingWindowValues) {
        setWarpingWindowValues(new IndexedValues<>(warpingWindowValues));
    }

    public Indexed<Double> getWarpingWindowValues() {
        return warpingWindowSpace.getValues();
    }

    @Override
    protected List<ParameterSpace> getParameterSpaces() {
        return new ArrayList<>(Arrays.asList(warpingWindowSpace, toleranceSpace));
    }

    @Override
    protected AdvancedAbstractClassifier build() {
        LcssNn nn = new LcssNn();
        nn.setTolerance(toleranceValue);
        nn.setWarpingWindow(warpingWindowValue);
        return nn;
    }

}
