package development.go.Ee.ConstituentBuilders.DistanceMeasureBuilders;

import development.go.Ee.ConstituentBuilders.ConstituentBuilder;
import development.go.Indexed.Indexed;
import development.go.Indexed.IndexedValues;
import timeseriesweka.classifiers.nn.Nn;
import timeseriesweka.measures.dtw.Dtw;
import timeseriesweka.measures.lcss.Lcss;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DtwBuilder extends ConstituentBuilder<Dtw> {
    public Indexed<Double> getWarpingWindowValues() {
        return warpingWindowValues;
    }

    public void setWarpingWindowValues(final Indexed<Double> warpingWindowValues) {
        this.warpingWindowValues = warpingWindowValues;
    }

    private Indexed<Double> warpingWindowValues;

    @Override
    public void setUpParameters(final Instances instances) {
         warpingWindowValues = new Indexed<Double>() {

            @Override
            public Double apply(final int i) {
                return (double) i / 100;
            }

            @Override
            public int size() {
                return 101;
            }
        };
    }

    @Override
    public List<Integer> getDistanceMeasureParameterSizes() {
        return new ArrayList<>(Arrays.asList(warpingWindowValues.size()));
    }

    @Override
    public Dtw getDistanceMeasure() {
        return new Dtw();
    }

    @Override
    public void configureDistanceMeasure(final Dtw distanceMeasure, List<Integer> parameterPermutation) {
        double warpingWindow = warpingWindowValues.apply(parameterPermutation.get(0));
        distanceMeasure.setWarpingWindow(warpingWindow);
    }

}
