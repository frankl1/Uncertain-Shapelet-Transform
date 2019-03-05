package development.go.Ee.Constituents.ParameterSpaces;

import timeseriesweka.measures.ddtw.Ddtw;
import timeseriesweka.measures.dtw.Dtw;

public class DdtwParameterSpace extends DtwParameterSpace {
    @Override
    protected Dtw get() {
        return new Ddtw();
    }
}
