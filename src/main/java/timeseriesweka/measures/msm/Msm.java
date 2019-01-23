package timeseriesweka.measures.msm;

import timeseriesweka.classifiers.ee.abcdef.Mutable;
import timeseriesweka.measures.DistanceMeasure;

import static utilities.Utilities.positiveCheck;

public class Msm extends DistanceMeasure {

    public static final Mutable<Msm, Double> COST_MUTABLE = new Mutable<Msm, Double>() {
        @Override
        public <C extends Msm, D extends Double> void setValue(final C subject, final D value) {
            subject.setCost(value);
        }

        @Override
        public <C extends Msm> Double getValue(final C subject) {
            return subject.getCost();
        }
    };
    public static final Mutable<Msm, Double> WARPING_WINDOW_MUTABLE = new Mutable<Msm, Double>() {
        @Override
        public <C extends Msm, D extends Double> void setValue(final C subject, final D value) {
            subject.setWarpingWindow(value);
        }

        @Override
        public <C extends Msm> Double getValue(final C subject) {
            return subject.getWarpingWindow();
        }
    };

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        positiveCheck(cost);
        this.cost = cost;
    }

    private double cost = 1; // penalty

    public double getWarpingWindow() {
        return warpingWindow;
    }

    public void setWarpingWindow(double warpingWindow) {
        this.warpingWindow = warpingWindow;
    }

    private double warpingWindow = 1; // todo implement

//    private double findCost(double x, double y, double z) {
//        if((y <= x && x <= z) || (y >= x && x >= z)) {
//            return getCost();
//        } else {
//            return getCost() + min(Math.abs(x - y), Math.abs(x - z));
//        }
//    }

    private double editCost(double new_point, double x, double y){
        double dist = 0;

        if ( ( (x <= new_point) && (new_point <= y) ) ||
                ( (y <= new_point) && (new_point <= x) ) ) {
            dist = getCost();
        }
        else{
            dist = getCost() + Math.min( Math.abs(new_point - x) , Math.abs(new_point - y) );
        }

        return dist;
    }

    @Override
    protected double measureDistance(double[] timeSeriesA, double[] timeSeriesB, double cutOff) {
        int m, n, i, j;
        m = timeSeriesA.length;
        n = timeSeriesB.length;
        int warpingWindow = (int) Math.round(getWarpingWindow() * timeSeriesA.length);
        warpingWindow--;
        if(warpingWindow < 0) {
            warpingWindow = 0;
        }

        double[][] cost = new double[m][n];

        // Initialization
        cost[0][0] = Math.abs(timeSeriesA[0] - timeSeriesB[0]);

        int p = Math.min(timeSeriesB.length - 1, 1 + warpingWindow);
        for (i = 1; i< m; i++) {
            if(i < p) {
                cost[i][0] = cost[i-1][0] + editCost(timeSeriesA[i], timeSeriesA[i-1], timeSeriesB[0]);
            } else {
                cost[i][0] = Double.POSITIVE_INFINITY;
            }
        }

        for (j = 1; j < n; j++) {
            if(j < p) {
                cost[0][j] = cost[0][j-1] + editCost(timeSeriesB[j], timeSeriesA[0], timeSeriesB[j-1]);
            } else {
                cost[0][j] = Double.POSITIVE_INFINITY;
            }
        }

        // Main Loop
        for( i = 1; i < m; i++){
            int warpingWindowStart = Math.max(0, i - warpingWindow);
            int warpingWindowEnd = Math.min(timeSeriesB.length - 1, i + warpingWindow);
            for ( j = 1; j < n; j++){
                if(j < warpingWindowStart || j > warpingWindowEnd) {
                    cost[i][j] = Double.POSITIVE_INFINITY;
                } else {
                    double d1,d2, d3;
                    d1 = cost[i-1][j-1] + Math.abs(timeSeriesA[i] - timeSeriesB[j] );
                    d2 = cost[i-1][j] + editCost(timeSeriesA[i], timeSeriesA[i-1], timeSeriesB[j]);
                    d3 = cost[i][j-1] + editCost(timeSeriesB[j], timeSeriesA[i], timeSeriesB[j-1]);
                    cost[i][j] = Math.min( d1, Math.min(d2,d3) );
                }
            }
        }

        // Output
        return cost[m-1][n-1];

        //        double[] rowOne = new double[timeSeriesB.length];
//        rowOne[0] = Math.abs(timeSeriesA[0] - timeSeriesB[0]);
//        double min = rowOne[0];
//        for(int columnIndex = 1; columnIndex < timeSeriesB.length; columnIndex++) {
//            rowOne[columnIndex] = rowOne[columnIndex - 1] + findCost(timeSeriesB[columnIndex], timeSeriesA[0], timeSeriesB[columnIndex - 1]);
//            min = min(min, rowOne[columnIndex]);
//        }
//        if(min > cutOff) {
//            return Double.POSITIVE_INFINITY;
//        }
//        for(int rowIndex = 1; rowIndex < timeSeriesA.length; rowIndex++) {
//            double[] rowTwo = new double[timeSeriesB.length];
//            rowTwo[0] = rowOne[0] + findCost(timeSeriesA[rowIndex], timeSeriesA[rowIndex - 1], timeSeriesB[0]);
//            min = rowTwo[0];
//            for(int columnIndex = 1; columnIndex < timeSeriesB.length; columnIndex++) {
//                double ee = rowOne[columnIndex - 1] + Math.abs(timeSeriesA[rowIndex] - timeSeriesB[columnIndex]);
//                double b = rowOne[columnIndex] + findCost(timeSeriesA[rowIndex], timeSeriesA[rowIndex - 1], timeSeriesB[columnIndex]);
//                double c = rowTwo[columnIndex - 1] + findCost(timeSeriesB[columnIndex], timeSeriesA[rowIndex], timeSeriesB[columnIndex - 1]);
//                rowTwo[columnIndex] = min(ee,b,c);
//                min = min(min, rowTwo[columnIndex]);
//            }
//            if(min > cutOff) {
//                return Double.POSITIVE_INFINITY;
//            }
//            rowOne = rowTwo;
//        }
//        return rowOne[rowOne.length - 1];

//        double[][] cost = new double[timeSeriesA.length][timeSeriesB.length];
//        cost[0][0] = Math.abs(timeSeriesA[0] - timeSeriesB[0]);
//        for(int i = 1; i < timeSeriesA.length; i++) {
//            cost[i][0] = cost[i - 1][0] + findCost(timeSeriesA[i], timeSeriesA[i - 1], timeSeriesB[0]);
//        }
//        for(int j = 1; j < timeSeriesA.length; j++) {
//            cost[0][j] = cost[0][j - 1] + findCost(timeSeriesB[j], timeSeriesA[0], timeSeriesB[j - 1]);
//        }
//        for(int i = 1; i < timeSeriesA.length; i++) {
//            for(int j = 1; j < timeSeriesB.length; j++) {
//                double ee = cost[i - 1][j - 1] + Math.abs(timeSeriesA[i] - timeSeriesB[j]);
//                double b = cost[i - 1][j] + findCost(timeSeriesA[i], timeSeriesA[i - 1], timeSeriesB[j]);
//                double c = cost[i][j - 1] + findCost(timeSeriesB[j], timeSeriesA[i], timeSeriesB[j - 1]);
//                cost[i][j] = min(ee,b,c);
//            }
//        }
//        return cost[timeSeriesA.length - 1][timeSeriesB.length - 1];
    }

    @Override
    public String getRevision() {
        return "1";
    }

    @Override
    public String getParameters() {
        return "cost=" + cost + "," + "warpingWindow=" + warpingWindow;
    }

    @Override
    public String toString() {
        return "msm";
    }
}
