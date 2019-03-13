package development.go.Ee.Constituents.ParameterSpaces.old;

import timeseriesweka.measures.wddtw.Wddtw;
import timeseriesweka.measures.wdtw.Wdtw;

public class OldWddtwParameterSpace extends OldWdtwParameterSpace {
    @Override
    protected Wdtw get() {
        return new Wddtw();
    }
}
