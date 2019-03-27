package timeseriesweka.measures.erp;

import timeseriesweka.measures.dtw.DtwInterface;

public interface ErpInterface extends DtwInterface {

    double getPenalty();

    void setPenalty(double g);

}
