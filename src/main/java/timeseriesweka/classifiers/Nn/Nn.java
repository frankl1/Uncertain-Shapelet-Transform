package timeseriesweka.classifiers.Nn;

import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;

public class Nn extends AbstractNn {
    public DistanceMeasure getDistanceMeasure() {
        return distanceMeasure;
    }

    public void setDistanceMeasure(final DistanceMeasure distanceMeasure) {
        this.distanceMeasure = distanceMeasure;
    }

    private DistanceMeasure distanceMeasure = new Dtw();

    @Override
    protected DistanceMeasure getDistanceMeasureInstance() {
        return distanceMeasure;
    }
}
