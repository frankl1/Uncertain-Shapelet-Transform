package development.go.Ee.Constituents.ParameterSpaces.old;

import timeseriesweka.measures.ddtw.Ddtw;
import timeseriesweka.measures.dtw.Dtw;

public class OldDdtwParameterSpace extends OldDtwParameterSpace {

    @Override
    protected Dtw get() {
        return new Ddtw();
    }
}
