package timeseriesweka.measures.wdtw;

import timeseriesweka.measures.dtw.DtwInterface;

public interface WdtwInterface extends DtwInterface {

    double getWeight();

    void setWeight(double weight);

}
