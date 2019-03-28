package timeseriesweka.classifiers.Nn.Specialised.Twe;

import timeseriesweka.classifiers.Nn.AbstractNn;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.twe.Twe;
import timeseriesweka.measures.twe.TweInterface;

public class TweNn extends AbstractNn implements TweInterface {

    private final Twe twe = new Twe();

    @Override
    public String toString() {
        return twe.toString() + "Nn";
    }

    @Override
    protected DistanceMeasure getDistanceMeasureInstance() {
        return twe;
    }


    @Override
    public double getPenalty() {
        return twe.getPenalty();
    }

    @Override
    public void setPenalty(final double penalty) {
        twe.setPenalty(penalty);
    }

    @Override
    public double getStiffness() {
        return twe.getStiffness();
    }

    @Override
    public void setStiffness(final double stiffness) {
        twe.setStiffness(stiffness);
    }
}
