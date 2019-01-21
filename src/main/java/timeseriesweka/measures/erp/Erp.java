package timeseriesweka.measures.erp;

import timeseriesweka.classifiers.ee.abcdef.Mutable;
import timeseriesweka.measures.DistanceMeasure;
import utilities.instances.TrainTestSplit;

import java.io.IOException;

public class Erp extends DistanceMeasure {

    public static final Mutable<Erp, Double> WARPING_WINDOW_MUTABLE = new Mutable<Erp, Double>() {
        @Override
        public <C extends Erp, D extends Double> void setValue(final C subject, final D value) {
            subject.setWarpingWindow(value);
        }

        @Override
        public <C extends Erp> Double getValue(final C subject) {
            return subject.getWarpingWindow();
        }
    };
    public static final Mutable<Erp, Double> PENALTY_MUTABLE = new Mutable<Erp, Double>() {
        @Override
        public <C extends Erp, D extends Double> void setValue(final C subject, final D value) {
            subject.setPenalty(value);
        }

        @Override
        public <C extends Erp> Double getValue(final C subject) {
            return subject.getPenalty();
        }
    };

    public double getWarpingWindow() {
        return warpingWindow;
    }

    public void setWarpingWindow(double bandSize) {
        this.warpingWindow = bandSize;
    }

    public double getPenalty() {
        return penalty;
    }

    public void setPenalty(double g) {
        this.penalty = g;
    }

    private double warpingWindow; // warp
    private double penalty; // penalty for ee gap, 0 best according to paper

    public Erp(double warpingWindow, double penalty) {
        this.warpingWindow = warpingWindow;
        this.penalty = penalty;
    }

    public Erp() {
        this(1, 0);
    }

    @Override
    protected double measureDistance(double[] timeSeriesA, double[] timeSeriesB, double cutOff) {
        // Current and previous columns of the matrix
        double[] curr = new double[timeSeriesB.length];
        double[] prev = new double[timeSeriesB.length];

        // size of edit distance band
        // bandsize is the maximum allowed distance to the diagonal
//        int band = (int) Math.ceil(v2.getDimensionality() * bandSize);
        int band = (int) Math.ceil(timeSeriesB.length * getWarpingWindow());

        // g parameter for local usage
        double gValue = penalty;

        for (int i = 0; i < timeSeriesA.length; i++) {
            // Swap current and prev arrays. We'll just overwrite the new curr.
            {
                double[] temp = prev;
                prev = curr;
                curr = temp;
            }
            int l = i - (band + 1);
            if (l < 0) {
                l = 0;
            }
            int r = i + (band + 1);
            if (r > (timeSeriesB.length - 1)) {
                r = (timeSeriesB.length - 1);
            }

            for (int j = l; j <= r; j++) {
                if (Math.abs(i - j) <= band) {
                    // compute squared distance of feature vectors
                    double val1 = timeSeriesA[i];
                    double val2 = gValue;
                    double diff = (val1 - val2);
                    final double d1 = Math.sqrt(diff * diff);

                    val1 = gValue;
                    val2 = timeSeriesB[j];
                    diff = (val1 - val2);
                    final double d2 = Math.sqrt(diff * diff);

                    val1 = timeSeriesA[i];
                    val2 = timeSeriesB[j];
                    diff = (val1 - val2);
                    final double d12 = Math.sqrt(diff * diff);

                    final double dist1 = d1 * d1;
                    final double dist2 = d2 * d2;
                    final double dist12 = d12 * d12;

                    final double cost;

                    if ((i + j) != 0) {
                        if ((i == 0) || ((j != 0) && (((prev[j - 1] + dist12) > (curr[j - 1] + dist2)) && ((curr[j - 1] + dist2) < (prev[j] + dist1))))) {
                            // del
                            cost = curr[j - 1] + dist2;
                        } else if ((j == 0) || ((i != 0) && (((prev[j - 1] + dist12) > (prev[j] + dist1)) && ((prev[j] + dist1) < (curr[j - 1] + dist2))))) {
                            // ins
                            cost = prev[j] + dist1;
                        } else {
                            // match
                            cost = prev[j - 1] + dist12;
                        }
                    } else {
                        cost = 0;
                    }

                    curr[j] = cost;
                    // steps[i][j] = step;
                } else {
                    curr[j] = Double.POSITIVE_INFINITY; // outside band
                }
            }
        }

        return Math.sqrt(curr[timeSeriesB.length - 1]);
    }

    @Override
    public String getRevision() {
        return null;
    }

    @Override
    public String getParameters() {
        return "penalty=" + penalty + ",warpingWindow=" + warpingWindow;
    }

    @Override
    public String toString() {
        return "erp";
    }

}
