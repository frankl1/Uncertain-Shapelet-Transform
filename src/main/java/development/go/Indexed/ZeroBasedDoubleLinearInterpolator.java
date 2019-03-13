package development.go.Indexed;

public class ZeroBasedDoubleLinearInterpolator extends LinearInterpolator<Double> {
    public ZeroBasedDoubleLinearInterpolator(final Double min, final Double max, final int size) {
        super(min, max, size);
    }

    @Override
    public Double apply(final int i) {
        return (double) i / getMax();
    }
}
