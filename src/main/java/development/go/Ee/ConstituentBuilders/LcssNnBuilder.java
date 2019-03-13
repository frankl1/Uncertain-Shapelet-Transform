package development.go.Ee.ConstituentBuilders;

import development.go.Ee.ConstituentBuilders.DistanceMeasureBuilders.lcss.LcssBuilder;
import timeseriesweka.measures.lcss.Lcss;

public class LcssNnBuilder extends DistanceMeasureNnBuilder<Lcss> {

    public LcssNnBuilder() {
        super(new LcssBuilder());
    }

}
