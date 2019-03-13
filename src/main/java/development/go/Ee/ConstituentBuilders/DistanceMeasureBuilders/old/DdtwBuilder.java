package development.go.Ee.ConstituentBuilders.DistanceMeasureBuilders.old;

import timeseriesweka.measures.ddtw.Ddtw;
import timeseriesweka.measures.dtw.Dtw;

public class DdtwBuilder extends DtwBuilder {
    @Override
    public Dtw getDistanceMeasure() {
        return new Ddtw();
    }
}
