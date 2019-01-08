package timeseriesweka.classifiers.ee.index;

import timeseriesweka.classifiers.ee.index.IndexedObtainer;

public class LinearInterpolater extends IndexedObtainer<Double> {

    public static final IndexedObtainer<Double> SCALED = new IndexedObtainer<Double>() {
        @Override
        protected Double obtain(final double value) {
            return value;
        }
    };

    private double min = 0;

    public double getMin() {
        return min;
    }

    public void setMin(final double min) {
        this.min = min;
    }

    public double getRange() {
        return range;
    }

    public void setRange(final double range) {
        this.range = range;
    }

    public void setMax(final double max) {
        setRange(max - min);
    }

    public double getMax() {
        return min + range;
    }

    private double range = 1;

    public LinearInterpolater(double min, double max) {
        setMin(min);
        setMax(max);
    }

    @Override
    protected Double obtain(final double value) {
        return min + value * range;
    }
}
