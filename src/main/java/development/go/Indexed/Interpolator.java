package development.go.Indexed;

public abstract class Interpolator<A> implements Indexed<A> {

    public void setSize(final int size) {
        if(size < 1) {
            throw new IllegalArgumentException("size too low: " + size);
        }
        this.size = size;
    }

    public Interpolator(final int size) {
        this.size = size;
    }

    private int size = 1;

    @Override
    public int size() {
        return size;
    }

}
