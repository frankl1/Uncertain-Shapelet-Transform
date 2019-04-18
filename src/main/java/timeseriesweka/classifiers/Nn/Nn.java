package timeseriesweka.classifiers.Nn;

import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;

public class Nn extends AbstractNn {
    public void setDistanceMeasure(final DistanceMeasure distanceMeasure) {
        super.setDistanceMeasure(distanceMeasure);
    }

}
