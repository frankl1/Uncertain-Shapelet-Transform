package development.go.Ee.ConstituentBuilders.DistanceMeasureBuilders;

import development.go.Ee.ConstituentBuilders.ConstituentBuilder;
import development.go.Indexed.Indexed;
import development.go.Indexed.IndexedValues;
import timeseriesweka.measures.lcss.Lcss;
import timeseriesweka.measures.wdtw.Wdtw;
import utilities.StatisticUtilities;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static utilities.Utilities.incrementalDiffList;

public class WdtwBuilder extends ConstituentBuilder<Wdtw> {
    public Indexed<Double> getWeightValues() {
        return weightValues;
    }

    public void setWeightValues(final Indexed<Double> weightValues) {
        this.weightValues = weightValues;
    }

    private Indexed<Double> weightValues = new Indexed<Double>() {
        @Override
        public int size() {
            return 101;
        }

        @Override
        public Double apply(final int i) {
            return (double) i / 100;
        }
    };
    private Indexed<Double> warpingWindowValues = new IndexedValues<>(Collections.singletonList(1.0));

    @Override
    public List<Integer> getDistanceMeasureParameterSizes() {
        return new ArrayList<>(Arrays.asList(weightValues.size(), warpingWindowValues.size()));
    }

    @Override
    public Wdtw getDistanceMeasure() {
        return new Wdtw();
    }

    @Override
    public void configureDistanceMeasure(final Wdtw wdtw, final List<Integer> parameterPermutation) {
        double weight = weightValues.apply(parameterPermutation.get(0));
        wdtw.setWeight(weight);
        double warpingWindow = warpingWindowValues.apply(parameterPermutation.get(1));
        wdtw.setWarpingWindow(warpingWindow);
    }
}
