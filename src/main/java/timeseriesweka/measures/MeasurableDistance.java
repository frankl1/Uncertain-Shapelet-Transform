package timeseriesweka.measures;

import timeseriesweka.classifiers.SaveParameterInfoOptions;
import utilities.OptionsSetter;
import weka.core.Instance;

import java.io.Serializable;

import static utilities.Utilities.extractTimeSeries;

public interface MeasurableDistance extends SaveParameterInfoOptions, Serializable, OptionsSetter {
    double distance(double[] timeSeriesA, double[] timeSeriesB, double cutOff);

    /**
     * measures distance between time series, swapping the two time series so A is always the longest
     * @param timeSeriesA time series
     * @param timeSeriesB time series
     * @return distance between two time series
     */
    default double distance(double[] timeSeriesA, double[] timeSeriesB) {
        return distance(timeSeriesA, timeSeriesB, Double.POSITIVE_INFINITY);
    }

    /**
     * find distance between two instances
     * @param instanceA first instance
     * @param instanceB second instance
     * @return distance between the two instances
     */
    default double distance(Instance instanceA, Instance instanceB) {
        return distance(instanceA, instanceB, Double.POSITIVE_INFINITY);
    }

    /**
     * find distance between two instances
     * @param instanceA first instance
     * @param instanceB second instance
     * @param cutOff cut off value to abandon distance measurement early
     * @return distance between the two instances
     */
    default double distance(Instance instanceA, Instance instanceB, double cutOff) {
        return distance(extractTimeSeries(instanceA), extractTimeSeries(instanceB), cutOff);
    }

}
