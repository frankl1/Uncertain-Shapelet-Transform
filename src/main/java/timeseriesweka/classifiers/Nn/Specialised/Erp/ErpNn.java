package timeseriesweka.classifiers.Nn.Specialised.Erp;

import timeseriesweka.classifiers.Nn.AbstractNn;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.erp.Erp;
import timeseriesweka.measures.erp.ErpInterface;

public class ErpNn extends AbstractNn implements ErpInterface {

    private final Erp erp = new Erp();

    public ErpNn() {
        setDistanceMeasure(erp);
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
    public int getWarpingWindow() {
        return erp.getWarpingWindow();
    }

    @Override
    public void setWarpingWindow(final int warpingWindow) {
        erp.setWarpingWindow(warpingWindow);
    }
}
