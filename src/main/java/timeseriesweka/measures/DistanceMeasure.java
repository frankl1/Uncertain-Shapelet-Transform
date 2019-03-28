package timeseriesweka.measures;

import timeseriesweka.classifiers.SaveParameterInfo;
import timeseriesweka.measures.dtw.Dtw;
import timeseriesweka.measures.msm.Msm;
import utilities.OptionsSetter;
import weka.core.Instance;
import weka.core.NormalizableDistance;
import weka.core.TechnicalInformationHandler;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static utilities.Utilities.extractTimeSeries;

import static utilities.Utilities.*;

// todo summary for each measure / relate to paper
// auth
// d Itakura Parallelogram



public abstract class DistanceMeasure extends NormalizableDistance implements MeasurableDistance {

    public DistanceMeasure() {
        setDontNormalize(true); // disable WEKA's normalisation - shouldn't use it anyway but just in case!
    }

    @Override
    public String globalInfo() {
        throw new UnsupportedOperationException("Haven't done this yet"); // todo
    }

    @Override
    protected double updateDistance(double currDist, double diff) {
        throw new UnsupportedOperationException("Haven't done this yet"); // todo
    }

    /**
     * measures distance between time series
     * @param timeSeriesA longest time series of the two
     * @param timeSeriesB shortest time series of the two
     * @param cutOff cut off value to abandon distance measurement early
     * @return distance between two time series
     */
    protected abstract double measureDistance(double[] timeSeriesA, double[] timeSeriesB, double cutOff); // todo ends up copying instance to double[] many times, perhaps just use accessors?

    /**
     * measures distance between time series, swapping the two time series so A is always the longest
     * @param timeSeriesA time series
     * @param timeSeriesB time series
     * @param cutOff cut off value to abandon distance measurement early
     * @return distance between two time series
     */
    public final double distance(double[] timeSeriesA, double[] timeSeriesB, double cutOff) {
        if(timeSeriesA.length < timeSeriesB.length) {
            double[] temp = timeSeriesA;
            timeSeriesA = timeSeriesB;
            timeSeriesB = temp;
        }
        return measureDistance(timeSeriesA, timeSeriesB, cutOff);
    }

    /**
     * measures distance between time series, swapping the two time series so A is always the longest
     * @param timeSeriesA time series
     * @param timeSeriesB time series
     * @return distance between two time series
     */
    public double distance(double[] timeSeriesA, double[] timeSeriesB) {
        return distance(timeSeriesA, timeSeriesB, Double.POSITIVE_INFINITY);
    }

    /**
     * find distance between two instances
     * @param instanceA first instance
     * @param instanceB second instance
     * @return distance between the two instances
     */
    public double distance(Instance instanceA, Instance instanceB) {
        return distance(instanceA, instanceB, Double.POSITIVE_INFINITY);
    }

    /**
     * find distance between two instances
     * @param instanceA first instance
     * @param instanceB second instance
     * @param cutOff cut off value to abandon distance measurement early
     * @return distance between the two instances
     */
    public double distance(Instance instanceA, Instance instanceB, double cutOff) {
        return distance(extractTimeSeries(instanceA), extractTimeSeries(instanceB), cutOff);
    }

    /**
     * string representation of this class, i.e. the distance measure name
     * @return distance measure name
     */
    @Override
    public String toString() {
        return getClass().getSimpleName().toUpperCase();
    }
}
