package development.go.Indexed;

import utilities.Utilities;

public class DoubleLinearInterpolator extends LinearInterpolator<Double> {

    public DoubleLinearInterpolator(final Double min, final Double max, final int size) {
        super(min, max, size);
    }

    @Override
    public Double apply(final int i) {
        int size = size();
        if(i >= size || i < 0) {
            throw new IllegalArgumentException("out of range: " + i);
        }
        double min = getMin();
        if(size == 1) {
            return min;
        }
        double max = getMax();
        double proportion = (double) i / (size - 1);
        if(min == 0) {
            return proportion * max;
        }
        return min + proportion * (max - min);
    }
}
