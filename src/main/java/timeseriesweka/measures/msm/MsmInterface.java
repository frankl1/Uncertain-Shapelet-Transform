package timeseriesweka.measures.msm;

import timeseriesweka.measures.dtw.DtwInterface;

public interface MsmInterface extends DtwInterface {

    double getPenalty();

    void setPenalty(double g);
}
