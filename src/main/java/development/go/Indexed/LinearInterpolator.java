package development.go.Indexed;

public abstract class LinearInterpolator<A extends Number> extends Interpolator<A> {

    public LinearInterpolator(final A min, final A max, final int size) {
        // todo checks, use setters
        super(size);
        this.min = min;
        this.max = max;
    }

    private A min;

    public A getMin() {
        return min;
    }

    public void setMin(final A min) {
        this.min = min;
    }

    public A getMax() {
        return max;
    }

    public void setMax(final A max) {
        this.max = max;
    }

    private A max;

}
