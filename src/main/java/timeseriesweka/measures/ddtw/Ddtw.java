package timeseriesweka.measures.ddtw;

import timeseriesweka.filters.DerivativeFilter;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;
import utilities.ArrayUtilities;

public class Ddtw extends Dtw {

    @Override
    protected double measureDistance(final double[] timeSeriesA, final double[] timeSeriesB, final double cutOff) {
        return super.measureDistance(DerivativeFilter.derivative(timeSeriesA), DerivativeFilter.derivative(timeSeriesB), cutOff); // todo little hacky with static func, need to make all the filters inherit from a parent filter which can take array of doubles like the static func
    }

}
