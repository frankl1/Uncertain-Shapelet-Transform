package development.go.Indexed;

public class IntegerLinearInterpolator extends LinearInterpolator<Integer> {
    public IntegerLinearInterpolator(final Integer min, final Integer max, final int size) {
        super(min, max, size);
    }

    @Override
    public Double apply(final int i) {
        return getMin() + i / (getSize() - 1) * ((double) getMax() - getMin());
    }
}
