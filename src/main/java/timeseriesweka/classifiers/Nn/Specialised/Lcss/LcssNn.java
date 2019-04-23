package timeseriesweka.classifiers.Nn.Specialised.Lcss;

import timeseriesweka.classifiers.Nn.AbstractNn;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.lcss.Lcss;
import timeseriesweka.measures.lcss.LcssInterface;

public class LcssNn extends AbstractNn implements LcssInterface {

    protected final Lcss lcss = new Lcss();

    public LcssNn() {
        setDistanceMeasure(lcss);
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
