package timeseriesweka.measures;

import java.util.Arrays;
import java.util.stream.DoubleStream;

/**
 * Class to abstract ee measurement between two time series
 */
public abstract class Measure {
    public double distance(double[] timeSeriesA,
                           double[] timeSeriesB) {
        return distance(DoubleStream.of(timeSeriesA),
                        DoubleStream.of(timeSeriesB));
    }

    public abstract double distance(DoubleStream timeSeriesA,
                                    DoubleStream timeSeriesB);
}
