package timeseriesweka.measures.wddtw;

import timeseriesweka.measures.wdtw.Wdtw;
import utilities.ArrayUtilities;

public class Wddtw extends Wdtw {
    @Override
    protected double measureDistance(final double[] a, final double[] b, final double cutOff) {
        return super.measureDistance(ArrayUtilities.derivative(a), ArrayUtilities.derivative(b), cutOff);
    }

    @Override
    public String toString() {
        return "wddtw";
    }

}
