package timeseriesweka.measures.dtw;

import timeseriesweka.classifiers.ee.abcdef.Mutable;
import timeseriesweka.measures.DistanceMeasure;

public class Dtw extends DistanceMeasure {

    public static Mutable<Dtw, Double> WARPING_WINDOW_MUTABLE = new Mutable<Dtw, Double>() {
        @Override
        public <C extends Dtw, D extends Double> void setValue(final C subject, final D value) {
            subject.setWarpingWindow(value);
        }

        @Override
        public <C extends Dtw> Double getValue(final C subject) {
            return subject.getWarpingWindow();
        }
    };

    /**
     * get the current warp percentage
     * @return warp percentage between 0 and 1 inclusive
     */
    public double getWarpingWindow() {
        return warp;
    }

    /**
     * set the warp percentage
     * @param warp warp percentage between 0 and 1 inclusive
     */
    public void setWarpingWindow(double warp) {
        if(warp < 0) {
            throw new IllegalArgumentException("Warp cannot be less than 0");
        } else if(warp > 1) {
            throw new IllegalArgumentException("Warp cannot be more than 1");
        } else {
            this.warp = warp;
        }
    }

    public Dtw(double warp) {
        this.warp = warp;
    }

    public Dtw() {
        this(1);
    }

    private double warp;

    /**
     * find the cost
     *
     * @return the distance between the values, squared
     */
    protected double cost(double[] timeSeriesA, int indexA,
                                          double[] timeSeriesB, int indexB) {
        return Math.pow(timeSeriesA[indexA] - timeSeriesB[indexB], 2);
    }

    @Override
    protected double measureDistance(double[] a,
                                     double[] b,
                                     double cutOff) {
        double minDist;
        boolean tooBig=true; // todo use only window to save mem rather than entire row

        //		System.out.println("\t\t\tIn Efficient with cutoff ="+cutoff);
        // Set the longest series to ee
        double[] temp;
        if(a.length<b.length){
            temp=a;
            a=b;
            b=temp;
        }
        int n=a.length;
        int m=b.length;
        //No Warp: windowSize=1, full warp: windowSize=m
        int windowSize = (int) Math.max(1, Math.round(a.length * getWarpingWindow()));;
        double[] row1=new double[m];
        double[] row2;
        //Set all to max
        row1[0]=(a[0]-b[0])*(a[0]-b[0]);
        if(row1[0]<cutOff)
            tooBig=false;



        for(int j=1;j<n&&j<=windowSize;j++){
            row1[j]=Double.MAX_VALUE;
        }
        //Warp ee[0] onto all b[1]...b[WindowSize]
        for(int j=1;j<windowSize && j<m;j++){
            row1[j]=row1[j-1]+(a[0]-b[j])*(a[0]-b[j]);
            if(row1[j]<cutOff)
                tooBig=false;
        }
        if(tooBig){
            return Double.MAX_VALUE;
        }
        int start,end;

        //For each remaining row, warp row i
        for (int i=1;i<n;i++){
            tooBig=true;
            row2=new double[m];
            //Find point to start from
            if(i-windowSize<1)
                start=0;
            else
                start=i-windowSize+1;
            if(start==0){
                row2[0]=row1[0]+(a[i]-b[0])*(a[i]-b[0]);
                start=1;
            }
            else
                row2[start-1]=Double.MAX_VALUE;
            //Find end point
            if(start+windowSize>=m)
                end=m;
            else
                end=start+windowSize;
            //Warp ee[i] onto b[j=start..end]
            for (int j = start;j<end;j++){
                //Find the min of row2[j-1],row1[j] and row1[j-1]
                minDist=row2[j-1];
                if(row1[j]<minDist)
                    minDist=row1[j];
                if(row1[j-1]<minDist)
                    minDist=row1[j-1];
                row2[j]=minDist+(a[i]-b[j])*(a[i]-b[j]);
                if(tooBig&&row2[j]<cutOff)
                    tooBig=false;
            }

            if(end<m)
                row2[end]=Double.MAX_VALUE;
            //Swap row 2 into row 1.
            row1=row2;
            //Early abandon
            if(tooBig){
                return Double.MAX_VALUE;
            }
        }

        return row1[m-1];
    }

    @Override
    public String getRevision() {
        return "1";
    }

    @Override
    public String toString() {
        return "dtw";
    }

    @Override
    public String getParameters() {
        return "warp=" + warp;
    }


//    float step = (float) (timeSeriesA.length + 1) / timeSeriesB.length; // find step between windows (the shift in columns for each row)
//    int windowSize = Math.max(1,(int) (timeSeriesA.length * getWarpingWindow())); // find window size from warp
//    windowSize = Math.max((int) Math.ceil(step) + 1, windowSize); // window size must be atleast as much as the step between rows todo correct?
//    int windowOneStart = 0; // start window 1 at 0
//    windowSize--; // remove current cell from window (i.e. if window is 10 and at cell 1, window needs to be 9 to reach cell 10
//    int windowOneEnd = windowSize; // end window 1 at half ee window
//    double[] windowOne = new double[windowOneEnd + 1]; // create window 1 at (half ee window + current cell) length
//    windowOne[0] = cost(timeSeriesA, windowOneStart,
//                        timeSeriesB, windowOneStart); // find squared distance between start points of each series
//        if(windowOne[0] > cutOff) { // early abandon
//        return Double.POSITIVE_INFINITY;
//    }
//        for(int columnIndex = 1; columnIndex < windowOne.length; columnIndex++) { // populate first row
//        double cost = cost(timeSeriesA,windowOneStart + columnIndex,
//                timeSeriesB,0); // find distance between current cell and first of series b
//        double distance = windowOne[columnIndex - 1]; // cost from previous cell
//        windowOne[columnIndex] = distance + cost; // overall cost
//    }
//        for(int rowIndex = 1; rowIndex < timeSeriesB.length; rowIndex++) { // for each remaining row
//        int windowTwoCenter = Math.round(step * rowIndex); // setup ee new window at appropriate step
//        int windowTwoStart = Math.max(0, windowTwoCenter - windowSize); // find start, accounting for start of row
//        int windowTwoEnd = Math.min(timeSeriesA.length - 1, windowTwoCenter + windowSize); // find end, accounting for end of row
//        double[] windowTwo = new double[windowTwoEnd - windowTwoStart + 1]; // window 2
//        int columnIndex = 0; // current column
//        if(windowTwoStart == 0) { // set first cell of window 2 if leftmost of row
//            windowTwo[0] = cost(timeSeriesA,0, timeSeriesB,rowIndex);
//            columnIndex++;
//        }
//        double minValueInRow = 0;
//        for(; columnIndex < windowTwo.length; columnIndex++) { // for each column in window 2
//            double minDistance = Double.POSITIVE_INFINITY; // find the min distance
//            if(columnIndex > 0) { // check alignment, previous cell in window
//                // i.e. rowIndex, columnIndex - 1
//                minDistance = Math.min(minDistance, windowTwo[columnIndex - 1]);
//            }
//            if(columnIndex + windowTwoStart <= windowOneEnd && // check alignment
//                    columnIndex + windowTwoStart >= windowOneStart) { // cell above
//                // i.e. rowIndex - 1, columnIndex
//                minDistance = Math.min(minDistance, windowOne[columnIndex + (windowTwoStart - windowOneStart)]);
//            }
//            if(columnIndex + windowTwoStart - 1 <= windowOneEnd && // check alignment
//                    columnIndex + windowTwoStart - 1 >= windowOneStart) { // cell above and left 1
//                // i.e. rowIndex - 1, columnIndex - 1
//                minDistance = Math.min(minDistance, windowOne[columnIndex - 1 + (windowTwoStart - windowOneStart)]);
//            }
//            double cost = cost(timeSeriesA,columnIndex + windowTwoStart,
//                    timeSeriesB,rowIndex); // find cost of current cell
//            windowTwo[columnIndex] = cost + minDistance; // find overall cost
//            minValueInRow = min(minValueInRow, windowTwo[columnIndex]);
//        }
//        if(minValueInRow > cutOff) { // early abandon
//            return Double.POSITIVE_INFINITY;
//        }
//        windowOne = windowTwo; // shift window two to window one, i.e. drop ee row
//        windowOneStart = windowTwoStart;
//        windowOneEnd = windowTwoEnd;
//    }
//        return windowOne[windowOne.length - 1];

}
