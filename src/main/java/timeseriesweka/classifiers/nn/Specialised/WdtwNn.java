package timeseriesweka.classifiers.nn.Specialised;

import timeseriesweka.classifiers.nn.AbstractNn;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.wddtw.Wddtw;
import timeseriesweka.measures.wdtw.Wdtw;
import timeseriesweka.measures.wdtw.WdtwInterface;
import weka.core.Instance;

public class WdtwNn extends AbstractNn implements WdtwInterface {
    private final Wdtw wdtw = new Wdtw();

    @Override
    public String toString() {
        return wdtw.toString() + "nn";
    }

    @Override
    protected DistanceMeasure getDistanceMeasureInstance() {
        return wdtw;
    }

    @Override
    public double getWeight() {
        return wdtw.getWeight();
    }

    @Override
    public void setWeight(final double weight) {
        wdtw.setWeight(weight);
    }

    @Override
    public double getWarpingWindow() {
        return wdtw.getWarpingWindow();
    }

    @Override
    public void setWarpingWindow(final double warpingWindow) {
        wdtw.setWarpingWindow(warpingWindow);
    }
}
