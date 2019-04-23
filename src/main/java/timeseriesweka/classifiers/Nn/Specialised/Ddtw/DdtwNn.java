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
    public double getWarpingWindow() {
        return ddtw.getWarpingWindow();
    }

    @Override
    public void setWarpingWindow(final double warpingWindow) {
        ddtw.setWarpingWindow(warpingWindow);
    }

}
