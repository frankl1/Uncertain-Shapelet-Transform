package timeseriesweka.classifiers.Nn.Specialised.Dtw;

import timeseriesweka.classifiers.Nn.AbstractNn;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;
import timeseriesweka.measures.dtw.DtwInterface;

public class DtwNn extends AbstractNn implements DtwInterface {

    private final Dtw dtw = new Dtw();

    @Override
    public double getWarpingWindow() {
        return dtw.getWarpingWindow();
    }

    @Override
    public void setWarpingWindow(final double warpingWindow) {
        dtw.setWarpingWindow(warpingWindow);
    }

    @Override
    public String toString() {
        return dtw.toString() + "Nn";
    }

    @Override
    protected DistanceMeasure getDistanceMeasureInstance() {
        return dtw;
    }
}
