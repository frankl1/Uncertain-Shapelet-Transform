package timeseriesweka.classifiers.nn.Specialised;

import timeseriesweka.classifiers.nn.AbstractNn;
import timeseriesweka.measures.wddtw.Wddtw;
import timeseriesweka.measures.wddtw.WddtwInterface;
import weka.core.Instance;

public class WddtwNn extends AbstractNn implements WddtwInterface {

    private final Wddtw wddtw = new Wddtw();

    @Override
    public String toString() {
        return wddtw.toString() + "nn";
    }

    @Override
    protected double distance(final Instance instanceA, final Instance instanceB, final double cutOff) {
        return wddtw.distance(instanceA, instanceB, cutOff);
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
