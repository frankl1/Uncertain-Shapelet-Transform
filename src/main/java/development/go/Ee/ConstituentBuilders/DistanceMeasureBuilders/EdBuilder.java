package development.go.Ee.ConstituentBuilders.DistanceMeasureBuilders;

import development.go.Indexed.IndexedValues;
import weka.core.Instances;

import java.util.Arrays;
import java.util.Collections;

public class EdBuilder extends DtwBuilder {
    @Override
    public void setUpParameters(final Instances instances) {
        setWarpingWindowValues(new IndexedValues<>(Collections.singletonList(0.0)));
    }
}
