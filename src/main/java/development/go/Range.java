package development.go;

public class Range implements Comparable<Range> {
    public Range(final int start, final int end) {
        this.start = start;
        setEnd(end);
    }

    private int start;

    public int getStart() {
        return start;
    }

    public void setStart(final int start) {
        if(start > end) {
            this.start = end;
            end = start;
        } else {
            this.start = start;
        }
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(final int end) {
        if(end < start) {
            this.end = start;
            start = end;
        } else {
            this.end = end;
        }
    }

    private int end;

    @Override
    public int compareTo(final Range range) {
        return Integer.compare(start, range.start);
    }

}
