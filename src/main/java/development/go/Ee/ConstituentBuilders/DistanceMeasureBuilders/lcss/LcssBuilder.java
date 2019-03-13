package development.go.Ee.ConstituentBuilders.DistanceMeasureBuilders.lcss;

import timeseriesweka.measures.lcss.Lcss;

public class LcssBuilder extends AbstractLcssBuilder<Lcss> {
    @Override
    protected Lcss get() {
        return new Lcss();
    }
}
