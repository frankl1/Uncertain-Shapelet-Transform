package development.go.Ee.Constituents.ParameterSpaces;

import timeseriesweka.measures.wddtw.Wddtw;
import timeseriesweka.measures.wdtw.Wdtw;

public class WddtwParameterSpace extends WdtwParameterSpace {
    @Override
    protected Wdtw get() {
        return new Wddtw();
    }
}
