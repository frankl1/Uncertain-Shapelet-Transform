package timeseriesweka.measures;

import java.io.Serializable;

public class DistanceMeasurement implements Serializable {
    private final double distance;
    private final long time;

    public double getDistance() {
        return distance;
    }

    public long getTime() {
        return time;
    }

    public DistanceMeasurement(double distance, long time) {
        this.distance = distance;
        this.time = time;
    }
}