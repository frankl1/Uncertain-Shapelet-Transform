package development.go.Ee.ConstituentBuilders.DistanceMeasureBuilders.dtw;

import timeseriesweka.measures.dtw.Dtw;

public class DtwBuilder extends AbstractDtwBuilder<Dtw> {
    @Override
    protected Dtw get() {
        return new Dtw();
    }
}
