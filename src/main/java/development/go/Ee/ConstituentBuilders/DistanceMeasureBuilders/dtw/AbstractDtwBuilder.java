package development.go.Ee.ConstituentBuilders.DistanceMeasureBuilders.dtw;

import development.go.Ee.ConstituentBuilders.ConfiguredBuilder;
import development.go.Indexed.Indexed;
import timeseriesweka.measures.dtw.Dtw;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractDtwBuilder<A extends Dtw> extends ConfiguredBuilder<A> {

    private Indexed<Double> warpingWindowValues;

    @Override
    protected void setupParameters(final Instances instances) {
        warpingWindowValues = new Indexed<Double>() {
            @Override
            public int size() {
                return 101;
            }

            @Override
            public Double apply(final int i) {
                return (double) i / size();
            }
        };
    }

    @Override
    protected List<Integer> getParameterSizes() {
        return new ArrayList<>(Collections.singletonList(warpingWindowValues.size()));
    }

    @Override
    protected int getNumParameters() {
        return 1;
    }

    @Override
    public void configure(final A dtw, List<Integer> parameterPermutation) {
        double warpingWindowValue = warpingWindowValues.apply(parameterPermutation.get(0));
        dtw.setWarpingWindow(warpingWindowValue);
    }
}
