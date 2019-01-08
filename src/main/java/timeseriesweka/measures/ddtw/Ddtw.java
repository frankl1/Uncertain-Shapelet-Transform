package timeseriesweka.measures.ddtw;

import timeseriesweka.filters.DerivativeFilter;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;
import utilities.ArrayUtilities;

public class Ddtw extends Dtw {

    @Override
    protected double measureDistance(final double[] timeSeriesA, final double[] timeSeriesB, final double cutOff) {
        return super.measureDistance(ArrayUtilities.derivative(timeSeriesA), ArrayUtilities.derivative(timeSeriesB), cutOff);
    }

}
