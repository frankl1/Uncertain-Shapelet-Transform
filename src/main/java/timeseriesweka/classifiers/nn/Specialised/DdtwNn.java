package timeseriesweka.classifiers.nn.Specialised;

import timeseriesweka.classifiers.nn.AbstractNn;
import timeseriesweka.measures.ddtw.Ddtw;
import timeseriesweka.measures.ddtw.DdtwInterface;
import weka.core.Instance;

public class DdtwNn extends AbstractNn implements DdtwInterface {
    private final Ddtw ddtw = new Ddtw();

    @Override
    public String toString() {
        return ddtw.toString() + "nn";
    }

    @Override
    protected double distance(final Instance instanceA, final Instance instanceB, final double cutOff) {
        return ddtw.distance(instanceA, instanceB, cutOff);
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
