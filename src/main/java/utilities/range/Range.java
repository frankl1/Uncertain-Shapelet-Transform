package utilities.range;

import timeseriesweka.classifiers.ee.index.IndexedSupplier;

import java.util.ArrayList;
import java.util.List;

public class Range implements IndexedSupplier<Integer> {

    public Range(final Range originalRange) {
        ranges.addAll(originalRange.ranges);
    }

    public Range() {

    }

    public Range(final int from, final int to) {
        add(from, to);
    }

    public Range(List<?> list) {
        add(list);
    }

    public void add(final Range range) {
        for(FixedRange fixedRange : range.ranges) {
            add(fixedRange);
        }
    }

    private void add(FixedRange range) {
        add(range.getStart(), range.getEnd());
    }

    public void add(List<?> list) {
        add(0, list.size() - 1);
    }

    private static class FixedRange implements Comparable<FixedRange> {
        private int start;

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        private int end;

        public FixedRange(int start, int end) {
            if(end < start) {
                int temp = start;
                start = end;
                end = temp;
            }
            this.start = start;
            this.end = end;
        }

        public int get(int index) {
            return start + index;
        }

        public int size() {
            return end - start + 1;
        }

        public boolean contains(FixedRange range) {
            return range.start >= start && range.end <= end;
        }

        public boolean overlapsStartOf(FixedRange range) {
            return start <= range.start && end + 1 >= range.start;
        }

        public boolean overlapsEndOf(FixedRange range) {
            return start - 1 <= range.end && end >= range.end;
        }

        public boolean overlaps(FixedRange range) {
            return overlapsEndOf(range) || overlapsStartOf(range) || contains(range) || range.contains(this);
        }

        public boolean above(FixedRange range) {
            return start > range.end;
        }

        public boolean below(FixedRange range) {
            return end < range.start;
        }

        public boolean isDisjoint(FixedRange range) {
            return !overlapsStartOf(range) && !overlapsEndOf(range);
        }

        public void joinStart(FixedRange range) {
            start = Math.min(range.start, start);
        }

        public void joinEnd(FixedRange range) {
            end = Math.max(range.end, end);
        }

        public void join(FixedRange range) {
            joinEnd(range);
            joinStart(range);
        }

        public void disjoin(FixedRange range) {
            if(range.start == start) {
                start = range.end + 1;
            } else if(range.end == end) {
                end = range.end - 1;
            } else {
                if(overlapsStartOf(range)) {
                    end = range.start - 1;
                }
                if(overlapsEndOf(range)) {
                    start = range.end + 1;
                }
            }
        }

        public boolean bisectedBy(FixedRange range) {
            return range.start > start && range.end < end;
        }

        public FixedRange findLowerBisectionBy(FixedRange range) {
            return new FixedRange(start, range.start - 1);
        }

        public FixedRange findUpperBisectionBy(FixedRange range) {
            return new FixedRange(end, range.end + 1);
        }

        public boolean isEmpty() {
            return start > end;
        }

        @Override
        public String toString() {
            return "(" + start + ", " + end + ")";
        }

        @Override
        public int compareTo(FixedRange fixedRange) {
            if(above(fixedRange)) {
                return 1;
            } else if(below(fixedRange)) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    public boolean isEmpty() {
        return ranges.size() == 0;
    }

    public Integer get(int index) {
        if(index < 0) {
            throw new IllegalArgumentException("index less than zero");
        }
        if(ranges.size() == 0) {
            throw new IllegalArgumentException("No ranges");
        }
        int rangeIndex = 0;
        int numRanges = ranges.size();
        FixedRange range;
        do {
            range = ranges.get(rangeIndex);
            index -= range.size();
            rangeIndex++;
        } while (index >= 0 && rangeIndex < numRanges);
        index += range.size();
        return range.get(index);
    }

    private final List<FixedRange> ranges = new ArrayList<>();

    public int size() {
        int size = 0;
        for(FixedRange range : ranges) {
            size += range.size();
        }
        return size;
    }

    public void add(int from, int to) {
        FixedRange rangeToAdd = new FixedRange(from, to);
        if(ranges.size() == 0) {
            ranges.add(rangeToAdd);
            return;
        }
        int index = binarySearch(ranges, rangeToAdd);
        if(index < 0) {
            ranges.add(0, rangeToAdd);
        } else {
            FixedRange range = ranges.get(index);
            if(range.overlaps(rangeToAdd)) {
                range.join(rangeToAdd);
                boolean overlaps = true;
                index++;
                while (overlaps && index < ranges.size()) {
                    rangeToAdd = ranges.get(index);
                    if(range.overlaps(rangeToAdd)) {
                        range.join(rangeToAdd);
                        ranges.remove(index);
                    } else {
                        overlaps = false;
                    }
                }
                overlaps = true;
                index -= 2;
                while (overlaps && index >= 0) {
                    rangeToAdd = ranges.get(index);
                    if(range.overlaps(rangeToAdd)) {
                        range.join(rangeToAdd);
                        ranges.remove(index);
                        index--;
                    } else {
                        overlaps = false;
                    }
                }
            } else {
                ranges.add(index + 1, rangeToAdd);
            }
        }
    }

    public void clear() {
        ranges.clear();
    }

    private static <A extends Comparable<A>> int binarySearch(List<? extends A> list, A target) {
        int maxIndex = list.size() - 1;
        int minIndex = 0;
        while (maxIndex >= minIndex) {
            int indexRange = minIndex + maxIndex;
            int index = indexRange / 2 + indexRange % 2;
            int comparison = list.get(index).compareTo(target);
            if(comparison < 0) {
                // higher
                minIndex = index + 1;
            } else if(comparison > 0) {
                // lower
                maxIndex = index - 1;
            } else {
                // match
                return index;
            }
        }
        return Math.min(maxIndex, minIndex);
    }

    public void add(int value) {
        add(value, value);
    }

    public static void main(String[] args) {
        Range range = new Range();
        range.add(0,3);
        System.out.println(range);
        range.add(5,9);
        System.out.println(range);
        range.removeAt(0);
        System.out.println(range);
    }

    public void removeAt(int index) {
        remove(get(index)); // todo improve
    }

    public void remove(int value) {
        remove(value, value);
    }
    
    public void remove(int from, int to) {
        FixedRange rangeToRemove = new FixedRange(from, to);
        if(ranges.size() == 0) {
            return;
        }
        int index = binarySearch(ranges, rangeToRemove);
        FixedRange range = ranges.get(index);
        if(range.overlaps(rangeToRemove)) {
            int upperIndex = index + 1;
            int lowerIndex = index - 1;
            if(range.bisectedBy(rangeToRemove)) {
                ranges.remove(index);
                upperIndex++;
                ranges.add(index, range.findUpperBisectionBy(rangeToRemove));
                ranges.add(index, range.findLowerBisectionBy(rangeToRemove));
            } else {
                range.disjoin(rangeToRemove);
                if(range.isEmpty()) {
                    ranges.remove(index);
                }
            }
            boolean overlaps = true;
            index = upperIndex;
            while (overlaps && index < ranges.size()) {
                range = ranges.get(index);
                if(range.overlaps(rangeToRemove)) {
                    if(range.bisectedBy(rangeToRemove)) {
                        ranges.remove(index);
                        index += 2;
                        ranges.add(index, range.findUpperBisectionBy(rangeToRemove));
                        ranges.add(index, range.findLowerBisectionBy(rangeToRemove));
                    } else {
                        range.disjoin(rangeToRemove);
                        if(range.isEmpty()) {
                            ranges.remove(index);
                        } else {
                            index++;
                        }
                    }
                } else {
                    overlaps = false;
                }
            }
            overlaps = true;
            index = lowerIndex;
            while (overlaps && index >= 0) {
                range = ranges.get(index);
                if(range.overlaps(rangeToRemove)) {
                    if(range.bisectedBy(rangeToRemove)) {
                        ranges.remove(index);
                        ranges.add(index, range.findUpperBisectionBy(rangeToRemove));
                        ranges.add(index, range.findLowerBisectionBy(rangeToRemove));
                    } else {
                        range.disjoin(rangeToRemove);
                        if(range.isEmpty()) {
                            ranges.remove(index);
                        } else {
                            index--;
                        }
                    }
                } else {
                    overlaps = false;
                }
            }
        }
    }

    @Override
    public String toString() {
        int size = ranges.size();
        if(size == 0) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < size - 1; i++) {
            stringBuilder.append(ranges.get(i));
            stringBuilder.append(", ");
        }
        stringBuilder.append(ranges.get(size - 1));
        return stringBuilder.toString();
    }
}
