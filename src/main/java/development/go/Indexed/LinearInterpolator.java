package development.go.Indexed;

public abstract class LinearInterpolator<A extends Number> implements Indexed<Double> {

    public LinearInterpolator(final A min, final A max, final int size) {
        // todo checks, use setters
        this.min = min;
        this.max = max;
        this.size = size;
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

    public int getSize() {
        return size;
    }

    public void setSize(final int size) {
        this.size = size;
    }

    private A max;
    private int size;

    @Override
    public int size() {
        return size;
    }
}
