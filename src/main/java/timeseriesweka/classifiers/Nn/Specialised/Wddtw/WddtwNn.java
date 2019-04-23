package timeseriesweka.classifiers.Nn.Specialised.Wddtw;

import timeseriesweka.classifiers.Nn.AbstractNn;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.wddtw.Wddtw;
import timeseriesweka.measures.wddtw.WddtwInterface;

public class WddtwNn extends AbstractNn implements WddtwInterface {

    private final Wddtw wddtw = new Wddtw();

    public WddtwNn() {
        setDistanceMeasure(wddtw);
    }

    @Override
    public double getWeight() {
        return wddtw.getWeight();
    }

    @Override
    public void setWeight(final double weight) {
        wddtw.setWeight(weight);
    }

    @Override
    public double getWarpingWindow() {
        return wddtw.getWarpingWindow();
    }

    @Override
    public void setWarpingWindow(final double warpingWindow) {
        wddtw.setWarpingWindow(warpingWindow);
    }
}
