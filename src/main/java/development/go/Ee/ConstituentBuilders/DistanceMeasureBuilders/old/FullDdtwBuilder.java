package development.go.Ee.ConstituentBuilders.DistanceMeasureBuilders.old;

import timeseriesweka.measures.ddtw.Ddtw;
import timeseriesweka.measures.dtw.Dtw;

public class FullDdtwBuilder extends FullDtwBuilder {
    @Override
    public Dtw getDistanceMeasure() {
        return new Ddtw();
    }
}
