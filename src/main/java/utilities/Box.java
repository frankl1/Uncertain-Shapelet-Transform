package utilities;

public class Box<A> {
    private A contents;

    public Box(A contents) {
        setContents(contents);
    }

    public Box() {
        this(null);
    }

    public void setContents(A contents) {
        this.contents = contents;
    }

    public A getContents() {
        return contents;
    }
}
