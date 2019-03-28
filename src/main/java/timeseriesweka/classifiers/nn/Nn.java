package timeseriesweka.classifiers.nn;

import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;
import weka.core.Instance;

public class Nn extends AbstractNn {
    public DistanceMeasure getDistanceMeasure() {
        return distanceMeasure;
    }

    public void setDistanceMeasure(final DistanceMeasure distanceMeasure) {
        this.distanceMeasure = distanceMeasure;
    }

    private DistanceMeasure distanceMeasure = new Dtw();

    @Override
    public String toString() {
        return distanceMeasure.toString() + "nn";
    }

    @Override
    protected DistanceMeasure getDistanceMeasureInstance() {
        return distanceMeasure;
    }
}
