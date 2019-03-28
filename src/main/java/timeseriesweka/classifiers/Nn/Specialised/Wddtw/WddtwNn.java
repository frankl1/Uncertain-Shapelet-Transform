package timeseriesweka.classifiers.Nn.Specialised.Wddtw;

import timeseriesweka.classifiers.Nn.AbstractNn;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.wddtw.Wddtw;
import timeseriesweka.measures.wddtw.WddtwInterface;

public class WddtwNn extends AbstractNn implements WddtwInterface {

    private final Wddtw wddtw = new Wddtw();

    @Override
    public String toString() {
        return wddtw.toString() + "Nn";
    }

    @Override
    protected DistanceMeasure getDistanceMeasureInstance() {
        return wddtw;
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
