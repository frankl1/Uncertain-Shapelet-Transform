package development.go.Indexed;

public class DoubleLinearInterpolator extends LinearInterpolator<Double> {

    public DoubleLinearInterpolator(final Double min, final Double max, final int size) {
        super(min, max, size);
    }

    @Override
    public Double apply(final int i) {
        return getMin() + i / (getSize() - 1) * (getMax() - getMin()); // todo div zero
    }
}
