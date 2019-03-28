package timeseriesweka.classifiers.nn.Specialised;

import timeseriesweka.classifiers.nn.AbstractNn;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.msm.Msm;
import timeseriesweka.measures.msm.MsmInterface;
import weka.core.Instance;

public class MsmNn extends AbstractNn implements MsmInterface {

    private final Msm msm = new Msm();

    @Override
    public String toString() {
        return msm.toString() + "nn";
    }

    @Override
    protected DistanceMeasure getDistanceMeasureInstance() {
        return msm;
    }

    @Override
    public double getPenalty() {
        return msm.getPenalty();
    }

    @Override
    public void setPenalty(final double g) {
        msm.setPenalty(g);
    }

    @Override
    public double getWarpingWindow() {
        return msm.getWarpingWindow();
    }

    @Override
    public void setWarpingWindow(final double warpingWindow) {
        msm.setWarpingWindow(warpingWindow);
    }
}
