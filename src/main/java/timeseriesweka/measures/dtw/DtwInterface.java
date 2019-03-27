package timeseriesweka.measures.dtw;

import timeseriesweka.measures.MeasurableDistance;

public interface DtwInterface {
    double getWarpingWindow();
    void setWarpingWindow(double warpingWindow);
}
