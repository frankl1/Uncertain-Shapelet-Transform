package development.go.Ee.ConstituentBuilders.DistanceMeasureBuilders.old;

import development.go.Indexed.IndexedValues;
import weka.core.Instances;

import java.util.Arrays;
import java.util.Collections;

public class EdBuilder extends DtwBuilder {
    @Override
    protected void setUpParameters(final Instances instances) {
        setWarpingWindowValues(new IndexedValues<>(Collections.singletonList(0.0)));
    }
}
