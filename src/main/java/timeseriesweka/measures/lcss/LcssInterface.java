package timeseriesweka.measures.lcss;

import timeseriesweka.measures.MeasurableDistance;
import timeseriesweka.measures.dtw.DtwInterface;

public interface LcssInterface extends DtwInterface {
    double getTolerance();
    void setTolerance(double tolerance);
}
