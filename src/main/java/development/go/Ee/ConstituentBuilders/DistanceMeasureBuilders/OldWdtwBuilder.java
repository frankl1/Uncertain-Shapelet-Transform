package development.go.Ee.ConstituentBuilders.DistanceMeasureBuilders;

import development.go.Indexed.Indexed;
import weka.core.Instances;

public class OldWdtwBuilder extends WdtwBuilder {
    @Override
    public void setUpParameters(final Instances instances) {
        setWeightValues(new Indexed<Double>() {
            @Override
            public int size() {
                return 100;
            }

            @Override
            public Double apply(final int i) {
                return (double) i / size();
            }
        });
    }
}
