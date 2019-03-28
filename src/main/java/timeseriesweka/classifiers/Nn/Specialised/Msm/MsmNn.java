package timeseriesweka.classifiers.Nn.Specialised.Msm;

import timeseriesweka.classifiers.Nn.AbstractNn;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.msm.Msm;
import timeseriesweka.measures.msm.MsmInterface;

public class MsmNn extends AbstractNn implements MsmInterface {

    private final Msm msm = new Msm();

    @Override
    public String toString() {
        return msm.toString() + "Nn";
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
