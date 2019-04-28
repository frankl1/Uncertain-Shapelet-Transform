package timeseriesweka.measures.dtw;

import timeseriesweka.measures.DistanceMeasure;

public class Dtw extends DistanceMeasure implements DtwInterface {

    public static double DEFAULT_WARPING_WINDOW = 1;

    /**
     * get the current warpingWindowPercentage percentage
     * @return warpingWindowPercentage percentage between 0 and 1 inclusive
     */
    public double getWarpingWindow() {
        return warpingWindowPercentage;
    }

    /**
     * set the percentage percentage
     * @param percentage percentage percentage between 0 and 1 inclusive
     */
    public void setWarpingWindow(double percentage) {
        if(percentage < 0) {
            throw new IllegalArgumentException("Warp cannot be less than 0");
        } else if(percentage > 1) {
            throw new IllegalArgumentException("Warp cannot be more than 1");
        } else {
            this.warpingWindowPercentage = percentage;
        }
    }

    public Dtw(double warpingWindowPercentage) {

        setWarpingWindow(warpingWindowPercentage);
    }

    public Dtw() {
        this(1);
    }

    private double warpingWindowPercentage;

    /**
     * find the cost
     *
     * @return the distance between the values, squared
     */
    protected double findCost(double[] timeSeriesA, int indexA,
                                          double[] timeSeriesB, int indexB) {
        return Math.pow(timeSeriesA[indexA] - timeSeriesB[indexB], 2);
    }

    protected int findWindowSize(int length) { // todo scale window size correctly, current duplicates n (i.e. a instance length of 10 would have 10 for the window size for both 100% and 90% window
        int size =(int)(warpingWindowPercentage * length); // round window down
        // no warp = windowSize=1
        if(size < 1) size = 1; // full warp : windowSize=n, otherwise scale between
        else if(size<length)
            size++;
        return size;
    }



    @Override
    protected double measureDistance(double[] timeSeriesA,
                                     double[] timeSeriesB,
                                     double cutOff) { // todo missing vals / variable length
        // timeSeriesA across the top (i.e. columns)
        // timeSeriesB down (i.e. rows)
//        int windowSize = findWindowSize(timeSeriesA.length); // todo cleanedup version
//        double[] currentWindow = new double[windowSize];
//        currentWindow[0] = findCost(timeSeriesA, 0, timeSeriesB, 0);
//        for(int j = 1; j < currentWindow.length; j++) {
//            double cost = findCost(timeSeriesA, j, timeSeriesB, 0);
//            currentWindow[j] = currentWindow[j - 1] + cost;
//        }
//        for(int i = 1; i < timeSeriesB.length; i++) {
//            int windowStart = Math.max(0, i - (windowSize - 1));
//            int windowEnd = Math.min(timeSeriesA.length - 1, i + (windowSize - 1));
//            int diff = windowEnd - windowStart + 1;
//            double[] nextWindow = new double[diff];
//            double minDistance = Double.POSITIVE_INFINITY;
//            int shift = -1;
//            if(i >= windowSize) {
//                shift = 0;
//            }
//            for(int j = 0; j < nextWindow.length; j++) {
//                double distance = Double.POSITIVE_INFINITY;
//                int leftIndex = j - 1;
//                if(leftIndex >= 0) {
//                    distance = Math.min(distance, nextWindow[leftIndex]); // left
//                }
//                int topIndex = shift + j + 1;
//                if(topIndex < currentWindow.length) {
//                    distance = Math.min(distance, currentWindow[topIndex]); // top or top left with shortened window (at start)
//                }
//                int topLeftIndex = shift + j;
//                if(topLeftIndex < currentWindow.length && topLeftIndex >= 0) {
//                    distance = Math.min(distance, currentWindow[topLeftIndex]); // top left
//                }
//                double cost = findCost(timeSeriesA, windowStart + j, timeSeriesB, i);
//                distance += cost;
//                minDistance = Math.min(minDistance, distance);
//                nextWindow[j] = distance;
//            }
//            if(minDistance > cutOff) {
//                return Double.POSITIVE_INFINITY;
//            }
//            currentWindow = nextWindow;
//        }
//        return currentWindow[currentWindow.length - 1];

//        System.out.println("i1: " + asString(a));
//        System.out.println("i2: " + asString(b));

//        double[] a = timeSeriesA;
//        double[] b = timeSeriesB;

//        double minDist;
//        boolean tooBig=true; // todo use only window to save mem rather than entire row
//
//        //		System.out.println("\t\t\tIn Efficient with cutoff ="+cutoff);
//        // Set the longest series to timeseriesweka.classifiers.ee
//        double[] temp;
//        if(a.length<b.length){
//            temp=a;
//            a=b;
//            b=temp;
//        }
//        int n=a.length;
//        int m=b.length;
//        //No Warp: windowSize=1, full warpingWindowPercentage: windowSize=m
//        int windowSize = (int) Math.max(1, Math.round(a.length * getWarpingWindow()));;
//        double[] row1=new double[m];
//        double[] row2;
//        //Set all to max
//        row1[0]=(a[0]-b[0])*(a[0]-b[0]);
//        if(row1[0]<cutOff)
//            tooBig=false;
//
//
//
//        for(int j=1;j<n&&j<=windowSize;j++){
//            row1[j]=Double.MAX_VALUE;
//        }
//        //Warp timeseriesweka.classifiers.ee[0] onto all b[1]...b[WindowSize]
//        for(int j=1;j<windowSize && j<m;j++){
//            row1[j]=row1[j-1]+(a[0]-b[j])*(a[0]-b[j]);
//            if(row1[j]<cutOff)
//                tooBig=false;
//        }
//        if(tooBig){
//            return Double.MAX_VALUE;
//        }
//        int start,end;
//
////        System.out.println();
////        System.out.println(asString(row1));
//
//        //For each remaining row, warpingWindowPercentage row i
//        for (int i=1;i<n;i++){
//            tooBig=true;
//            row2=new double[m];
//            //Find point to start from
//            if(i-windowSize<1)
//                start=0;
//            else
//                start=i-windowSize+1;
//            if(start==0){
//                row2[0]=row1[0]+(a[i]-b[0])*(a[i]-b[0]);
//                start=1;
//            }
//            else
//                row2[start-1]=Double.MAX_VALUE;
//            //Find end point
//            if(start+windowSize>=m)
//                end=m;
//            else
//                end=start+windowSize;
//            if(row2[0]<cutOff)
//                tooBig=false;
//            //Warp a[i] onto b[j=start..end]
//            for (int j = start;j<end;j++){
//                //Find the min of row2[j-1],row1[j] and row1[j-1]
//                minDist=row2[j-1];
//                if(row1[j]<minDist)
//                    minDist=row1[j];
//                if(row1[j-1]<minDist)
//                    minDist=row1[j-1];
//                row2[j]=minDist+(a[i]-b[j])*(a[i]-b[j]);
//                if(tooBig&&row2[j]<cutOff)
//                    tooBig=false;
//            }
//
//            if(end<m)
//                row2[end]=Double.MAX_VALUE;
//            //Swap row 2 into row 1.
//            row1=row2;
//            //Early abandon
//            if(tooBig){
////                System.out.println("---");
//                return Double.MAX_VALUE;
//            }
//
////            System.out.println(asString(row2));
//
//        }
////        System.out.println("---");
//
//        return row1[m-1];

        // todo cleanup
        // todo trim memory to window by window
        // todo early abandon

        double[] first = timeSeriesA;
        double[] second = timeSeriesB;
        
        double minDist;
        boolean tooBig;

        int n = first.length;
        int m = second.length;
        /*  Parameter 0<=r<=1. 0 == no warpingWindowPercentage, 1 == full warpingWindowPercentage
         generalised for variable window size
         * */
        int windowSize = findWindowSize(n);
//Extra memory than required, could limit to windowsize,
//        but avoids having to recreate during CV 
//for varying window sizes        
        double[][] matrixD = new double[n][m];
        
        /*
         //Set boundary elements to max. 
         */
        int start, end;
        for (int i = 0; i < n; i++) {
            start = windowSize < i ? i - windowSize : 0;
            end = i + windowSize + 1 < m ? i + windowSize + 1 : m;
            for (int j = start; j < end; j++) {
                matrixD[i][j] = Double.MAX_VALUE;
            }
        }
        matrixD[0][0] = (first[0] - second[0]) * (first[0] - second[0]);
//a is the longer series. 
//Base cases for warping 0 to all with max interval	r	
//Warp first[0] onto all second[1]...second[r+1]
        for (int j = 1; j < windowSize && j < m; j++) {
            matrixD[0][j] = matrixD[0][j - 1] + (first[0] - second[j]) * (first[0] - second[j]);
        }

//	Warp second[0] onto all first[1]...first[r+1]
        for (int i = 1; i < windowSize && i < n; i++) {
            matrixD[i][0] = matrixD[i - 1][0] + (first[i] - second[0]) * (first[i] - second[0]);
        }
//Warp the rest,
//        System.out.println(Utilities.asString(matrixD[0]));
        for (int i = 1; i < n; i++) {
            tooBig = true;
            start = windowSize < i ? i - windowSize + 1 : 1;
            end = i + windowSize < m ? i + windowSize : m;
            if(matrixD[i][start - 1] < cutOff) {
                tooBig = false;
            }
            for (int j = start; j < end; j++) {
                minDist = matrixD[i][j - 1];
                if (matrixD[i - 1][j] < minDist) {
                    minDist = matrixD[i - 1][j];
                }
                if (matrixD[i - 1][j - 1] < minDist) {
                    minDist = matrixD[i - 1][j - 1];
                }
                matrixD[i][j] = minDist + (first[i] - second[j]) * (first[i] - second[j]);
                if (tooBig && matrixD[i][j] < cutOff) {
                    tooBig = false;
                }
            }
//            System.out.println(Utilities.asString(matrixD[i]));
            //Early abandon
            if (tooBig) {
//                System.out.println("---");
                return Double.MAX_VALUE;
            }
        }
//        System.out.println("---");
//Find the minimum distance at the end points, within the warping window.
        return matrixD[n-1][m-1];
    }

    public static void main(String[] args) {
        Dtw dtw = new Dtw();
        dtw.setWarpingWindow(0.3);
        double[] a = new double[] {4, 6, 3, 23, 15, 6, 4, 13, 21, 12};
        double[] b = new double[] {7, 9, 15, 17, 6, 17, 13, 12, 19, 20};
        System.out.println(dtw.distance(a,b));
    }

    @Override
    public String[] getOptions() {
        return new String[] {WARPING_WINDOW_KEY, String.valueOf(warpingWindowPercentage)};
    }

    @Override
    public String getRevision() {
        return "1";
    }

    public static final String WARPING_WINDOW_KEY = "warpingWindow";

    @Override
    public boolean setOption(final String key, final String value) {
        if(key.equals(WARPING_WINDOW_KEY)) {
            setWarpingWindow(Double.parseDouble(value));
            return true;
        }
        return false;
    }
}
