package ee.dump.DistanceMeasures.dtw;

import weka.core.Instances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class DtwPredefinedDeltaValuesGenerator implements Function<Instances, List<Double>> {

    private List<Double> values = new ArrayList<>(Arrays.asList(.0, .1, .2, .3, .4, .5, .6, .7, .8, .9, 1.0));

    public DtwPredefinedDeltaValuesGenerator() {}

    public DtwPredefinedDeltaValuesGenerator(List<Double> values) {
        setValues(values);
    }

    @Override
    public List<Double> apply(Instances instances) {
        List<Double> list = new ArrayList<>();
        for(Double value : values) {
            list.add(value * instances.size());
        }
        return list;
    }

    public List<Double> getValues() {
        return values;
    }

    public void setValues(List<Double> values) {
        this.values = values;
    }
}
