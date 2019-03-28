package timeseriesweka.classifiers.nn.Specialised;

import timeseriesweka.classifiers.nn.AbstractNn;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;
import timeseriesweka.measures.dtw.DtwInterface;
import weka.core.Instance;

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
        return dtw.toString() + "nn";
    }

    @Override
    protected DistanceMeasure getDistanceMeasureInstance() {
        return dtw;
    }
}
