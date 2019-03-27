package timeseriesweka.classifiers.nn.Specialised;

import timeseriesweka.classifiers.nn.AbstractNn;
import timeseriesweka.measures.erp.Erp;
import timeseriesweka.measures.erp.ErpInterface;
import weka.core.Instance;

public class ErpNn extends AbstractNn implements ErpInterface {

    private final Erp erp = new Erp();

    @Override
    public String toString() {
        return erp.toString() + "nn";
    }

    @Override
    protected double distance(final Instance instanceA, final Instance instanceB, final double cutOff) {
        return erp.distance(instanceA, instanceB, cutOff);
    }

    @Override
    public double getPenalty() {
        return erp.getPenalty();
    }

    @Override
    public void setPenalty(final double g) {
        erp.setPenalty(g);
    }

    @Override
    public double getWarpingWindow() {
        return erp.getWarpingWindow();
    }

    @Override
    public void setWarpingWindow(final double warpingWindow) {
        erp.setWarpingWindow(warpingWindow);
    }
}
