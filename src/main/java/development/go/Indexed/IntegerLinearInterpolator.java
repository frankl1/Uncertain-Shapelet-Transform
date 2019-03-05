package development.go.Indexed;

public class IntegerLinearInterpolator extends LinearInterpolator<Integer> {
    public IntegerLinearInterpolator(final Integer min, final Integer max, final int size) {
        super(min, max, size);
    }

    @Override
    public Integer apply(final int i) {
        return (int) (getMin() + i / (size() - 1) * ((double) getMax() - getMin()));
    }
}
