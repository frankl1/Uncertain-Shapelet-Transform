package timeseriesweka.classifiers.Nn.Specialised.Ddtw;

import timeseriesweka.classifiers.Nn.AbstractNn;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.ddtw.Ddtw;
import timeseriesweka.measures.ddtw.DdtwInterface;

public class DdtwNn extends AbstractNn implements DdtwInterface {
    private final Ddtw ddtw = new Ddtw();

    public DdtwNn() {
        setDistanceMeasure(ddtw);
    }

    @Override
    public int getWarpingWindow() {
        return ddtw.getWarpingWindow();
    }

    @Override
    public void setWarpingWindow(final int warpingWindow) {
        ddtw.setWarpingWindow(warpingWindow);
    }

}
