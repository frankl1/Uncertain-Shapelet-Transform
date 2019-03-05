package development.go.Indexed;

public class DoubleLinearInterpolator extends LinearInterpolator<Double> {

    public DoubleLinearInterpolator(final Double min, final Double max, final int size) {
        super(min, max, size);
    }

    @Override
    public Double apply(final int i) {
        double proportion = (double) i / (size() - 1);
        double max = getMax();
        double min = getMin();
        if(max == 1 && min == 0) {
            return proportion;
        } else {
            return min + proportion * (max - min);
        }
    }
}
