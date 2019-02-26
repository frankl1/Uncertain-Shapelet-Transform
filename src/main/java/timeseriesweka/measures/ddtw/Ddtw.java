package timeseriesweka.measures.ddtw;

import timeseriesweka.filters.DerivativeFilter;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;
import utilities.ArrayUtilities;

public class Ddtw extends Dtw {

    @Override
    protected double measureDistance(final double[] timeSeriesA, final double[] timeSeriesB, final double cutOff) {
        return super.measureDistance(DerivativeFilter.derivative(timeSeriesA), DerivativeFilter.derivative(timeSeriesB), cutOff);
    }

    @Override
    public String toString() {
        return "ddtw";
    }

}
