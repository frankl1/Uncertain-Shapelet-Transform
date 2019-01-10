package timeseriesweka.measures.euclidean;

import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;

public class Euclidean extends DistanceMeasure {

    @Override
    protected double measureDistance(double[] timeSeriesA, double[] timeSeriesB, double cutOff) {
        double sum = 0;
        for(int i = 0; i < timeSeriesA.length; i++) {
            sum += Math.pow(timeSeriesA[i] - timeSeriesB[i], 2);
            if(sum > cutOff) {
                return Double.POSITIVE_INFINITY;
            }
        }
        return sum;
    }

    @Override
    public String getRevision() {
        return null;
    }

    @Override
    public String getParameters() {
        return "";
    }

    @Override
    public String toString() {
        return "euclidean";
    }

}
