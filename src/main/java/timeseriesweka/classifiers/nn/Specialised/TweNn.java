package timeseriesweka.classifiers.nn.Specialised;

import timeseriesweka.classifiers.nn.AbstractNn;
import timeseriesweka.measures.twe.Twe;
import timeseriesweka.measures.twe.TweInterface;
import weka.core.Instance;

public class TweNn extends AbstractNn implements TweInterface {

    private final Twe twe = new Twe();

    @Override
    public String toString() {
        return twe.toString() + "nn";
    }

    @Override
    protected double distance(final Instance instanceA, final Instance instanceB, final double cutOff) {
        return twe.distance(instanceA, instanceB, cutOff);
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
