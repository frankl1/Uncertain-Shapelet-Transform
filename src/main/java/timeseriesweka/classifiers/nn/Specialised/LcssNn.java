package timeseriesweka.classifiers.nn.Specialised;

import timeseriesweka.classifiers.nn.AbstractNn;
import timeseriesweka.measures.lcss.Lcss;
import timeseriesweka.measures.lcss.LcssInterface;
import weka.core.Instance;

public class LcssNn extends AbstractNn implements LcssInterface {

    protected final Lcss lcss = new Lcss();

    @Override
    public String toString() {
        return lcss.toString() + "nn";
    }

    @Override
    protected double distance(final Instance instanceA, final Instance instanceB, final double cutOff) {
        return lcss.distance(instanceA, instanceB, cutOff);
    }

    @Override
    public double getTolerance() {
        return lcss.getTolerance();
    }

    @Override
    public void setTolerance(final double tolerance) {
        lcss.setTolerance(tolerance);
    }

    @Override
    public double getWarpingWindow() {
        return lcss.getWarpingWindow();
    }

    @Override
    public void setWarpingWindow(final double warpingWindow) {
        lcss.setWarpingWindow(warpingWindow);
    }
}
