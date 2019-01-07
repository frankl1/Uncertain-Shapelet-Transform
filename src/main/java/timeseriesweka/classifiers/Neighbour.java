package timeseriesweka.classifiers;

import weka.core.Instance;

public class Neighbour implements Comparable<Neighbour> {
    private final double distance;

    public double getDistance() {
        return distance;
    }

    public int getClassValue() {
        return classValue;
    }

    private final int classValue;

    public Neighbour(double distance, int classValue) {
        this.distance = distance;
        this.classValue = classValue;
    }

    @Override
    public int compareTo(Neighbour neighbour) {
        double diff = neighbour.distance - distance;
        if(diff > 0) {
            return 1;
        } else if (diff < 0) {
            return -1;
        } else {
            return 0;
        }
    }
}
