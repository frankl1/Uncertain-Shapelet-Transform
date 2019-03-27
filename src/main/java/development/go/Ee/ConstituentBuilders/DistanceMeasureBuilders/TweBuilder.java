package development.go.Ee.ConstituentBuilders.DistanceMeasureBuilders;

import development.go.Ee.ConstituentBuilders.ConstituentBuilder;
import development.go.Indexed.IndexedValues;
import timeseriesweka.measures.twe.Twe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TweBuilder extends ConstituentBuilder<Twe> {
    private final IndexedValues<Double> stiffnessValues = new IndexedValues<>(Arrays.asList(
        0.00001,
        0.0001,
        0.0005,
        0.001,
        0.005,
        0.01,
        0.05,
        0.1,
        0.5,
        1d
    ));
    private final IndexedValues<Double> penaltyValues = new IndexedValues<>(Arrays.asList(
        0d,
        0.011111111,
        0.022222222,
        0.033333333,
        0.044444444,
        0.055555556,
        0.066666667,
        0.077777778,
        0.088888889,
        0.1
    ));

    @Override
    public List<Integer> getDistanceMeasureParameterSizes() {
        return new ArrayList<>(Arrays.asList(penaltyValues.size(), stiffnessValues.size()));
    }

    @Override
    public Twe getDistanceMeasure() {
        return new Twe();
    }

    @Override
    public void configureDistanceMeasure(final Twe distanceMeasure, final List<Integer> parameterPermutation) {
        double penalty = penaltyValues.apply(parameterPermutation.get(0));
        distanceMeasure.setPenalty(penalty);
        double stiffness = stiffnessValues.apply(parameterPermutation.get(1));
        distanceMeasure.setStiffness(stiffness);
    }
}
