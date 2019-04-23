package timeseriesweka.classifiers.Nn.Specialised.Wdtw;

import timeseriesweka.classifiers.Nn.AbstractNn;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.wdtw.Wdtw;
import timeseriesweka.measures.wdtw.WdtwInterface;

public class WdtwNn extends AbstractNn implements WdtwInterface {
    private final Wdtw wdtw = new Wdtw();

    public WdtwNn() {
        setDistanceMeasure(wdtw);
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
