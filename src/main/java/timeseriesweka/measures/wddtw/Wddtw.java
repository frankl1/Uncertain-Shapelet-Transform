package timeseriesweka.measures.wddtw;

import timeseriesweka.filters.DerivativeFilter;
import timeseriesweka.measures.wdtw.Wdtw;
import utilities.ArrayUtilities;

public class Wddtw extends Wdtw {
    @Override
    protected double measureDistance(final double[] a, final double[] b, final double cutOff) {
        return super.measureDistance(DerivativeFilter.derivative(a), DerivativeFilter.derivative(b), cutOff); // todo little hacky with static func, need to make all the filters inherit from a parent filter which can take array of doubles like the static func
    }

}
