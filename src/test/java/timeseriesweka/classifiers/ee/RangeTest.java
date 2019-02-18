package timeseriesweka.classifiers.ee;

import org.junit.Test;
import utilities.range.Range;


import static org.junit.Assert.*;

public class RangeTest {

    @Test
    public void getTest() {
        Range range = new Range();
        range.add(1, 5);
        range.add(10,15);
        range.add(20, 30);
        assertEquals(range.get(2), new Integer(3));
        assertEquals(range.get(5) ,new Integer(10));
        assertEquals(range.get(21), new Integer(30));
    }

    @Test
    public void sizeTest() {
        Range range = new Range();
        range.add(1, 5);
        range.add(10,15);
        range.add(20, 30);
        assertEquals(22, range.size());
    }

    @Test
    public void addContinuityTest() {
        Range range = new Range();
        range.add(1, 5);
        range.add(10,15);
        range.add(20, 30);
        range.add(5, 20);
        assertEquals("(1, 30)", range.toString());
    }

    @Test
    public void removeTest() {
        Range range = new Range();
        range.add(1, 5);
        range.add(10,15);
        range.add(20, 30);
        range.remove(10, 15);
        assertEquals("(1, 5), (20, 30)", range.toString());
    }

    @Test
    public void removeValueTest() {
        Range range = new Range();
        range.add(1, 5);
        range.add(10,15);
        range.add(20, 30);
        range.remove(13);
        assertEquals("(1, 5), (10, 12), (14, 15), (20, 30)", range.toString());
    }

    @Test
    public void removeDiscontinuityTest() {
        Range range = new Range();
        range.add(1, 5);
        range.add(10,15);
        range.add(20, 30);
        range.remove(12, 25);
        assertEquals("(1, 5), (10, 11), (26, 30)", range.toString());
    }

    @Test
    public void clearTest() {
        Range range = new Range();
        range.add(1, 5);
        range.add(10,15);
        range.add(20, 30);
        range.clear();
        assertTrue(range.isEmpty());
    }

    @Test
    public void emptyTest() {
        Range range = new Range();
        assertTrue(range.isEmpty());
    }

    @Test
    public void nonEmptyTest() {
        Range range = new Range();
        range.add(2);
        assertFalse(range.isEmpty());
    }

    @Test
    public void toStringSingleTest() {
        Range range = new Range();
        range.add(1, 5);
        assertEquals("(1, 5)", range.toString());
    }

    @Test
    public void toStringEmptyTest() {
        Range range = new Range();
        assertEquals("", range.toString());
    }

    @Test
    public void toStringMultipleTest() {
        Range range = new Range();
        range.add(1, 5);
        range.add(10,15);
        range.add(20, 30);
        assertEquals("(1, 5)" + ", " +
                "(10, 15)" + ", " +
                "(20, 30)", range.toString());
    }

    @Test
    public void continuityTest() {
        Range range = new Range();
        range.add(1);
        range.add(2);
        range.add(3);
        assertEquals("(1, 3)", range.toString());
    }

    @Test
    public void discontinuityTestMax() {
        Range range = new Range();
        range.add(1, 2);
        range.add(8, 10);
        assertEquals("(1, 2)" + ", " +
                "(8, 10)", range.toString());
    }

    @Test
    public void discontinuityTestMin() {
        Range range = new Range();
        range.add(1, 2);
        range.add(8, 10);
        range.add(-1,-1);
        assertEquals("(-1, -1), (1, 2), (8, 10)", range.toString());
    }
}