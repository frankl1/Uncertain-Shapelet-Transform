package development.go.Ee.ConstituentBuilders.DistanceMeasureBuilders;

import timeseriesweka.measures.wddtw.Wddtw;
import timeseriesweka.measures.wdtw.Wdtw;

public class OldWddtwBuilder extends OldWdtwBuilder {

    @Override
    public Wdtw getDistanceMeasure() {
        return new Wddtw();
    }
}
