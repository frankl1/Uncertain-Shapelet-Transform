package timeseriesweka.measures.lcss;

import timeseriesweka.measures.dtw.Dtw;

public class Lcss extends Dtw implements LcssInterface {

    public static final double DEFAULT_TOLERANCE = 0.01;

    public Lcss(double tolerance, double warpingWindowPercentage) {
        super(warpingWindowPercentage);
        setTolerance(tolerance);
    }

    // delta === warp
    // epsilon === diff between two values before they're considered the same AKA tolerance

    public Lcss() {
        this(DEFAULT_TOLERANCE, DEFAULT_WARPING_WINDOW);
    }

    private double tolerance;

    public double getTolerance() {
        return tolerance;
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    @Override
    protected double measureDistance(double[] first, double[] second, double cutOff) {
        // todo cleanup
        // todo trim memory to window by window
        // todo early abandon
        double[] a  = first;
        double[] b = second;
        int m = first.length;
        int n = second.length;

        int[][] lcss = new int[m+1][n+1];

        int warpingWindow = (int) (this.getWarpingWindow() * first.length);

        for(int i = 0; i < m; i++){ // another version which gives no diff in results!
            for(int j = i-warpingWindow; j <= i+warpingWindow; j++){
//                System.out.println("here");
                if(j < 0 || j >= n){
                    //do nothing
                }else if(b[j]+this.tolerance >= a[i] && b[j]-tolerance <=a[i]){
                    lcss[i+1][j+1] = lcss[i][j]+1;
                }else if(lcss[i][j+1] > lcss[i+1][j]){
                    lcss[i+1][j+1] = lcss[i][j+1];
                }else{
                    lcss[i+1][j+1] = lcss[i+1][j];
                }
            }
        }

        int max = -1;
        for(int i = 1; i < lcss[lcss.length-1].length; i++){
            if(lcss[lcss.length-1][i] > max){
                max = lcss[lcss.length-1][i];
            }
        }
        return 1-((double)max/m);

//        for(int i = 0; i < m; i++){ // another version, calculates the same distance
//            for(int j = i-warpingWindow; j <= i+warpingWindow; j++){
//                if(j < 0){
//                    j = -1;
//                }else if(j >= n){
//                    j = i+warpingWindow;
//                }else if(second[j]+this.tolerance >= first[i] && second[j]-tolerance <=first[i]){
//                    lcss[i+1][j+1] = lcss[i][j]+1;
//                }else if(lcss[i][j+1] > lcss[i+1][j]){
//                    lcss[i+1][j+1] = lcss[i][j+1];
//                }else{
//                    lcss[i+1][j+1] = lcss[i+1][j];
//                }
//
//                // could maybe do an early abandon here? Not sure, investigate further
//            }
//        }
//
//        int max = -1;
//        for(int i = 1; i < lcss[lcss.length-1].length; i++){
//            if(lcss[lcss.length-1][i] > max){
//                max = lcss[lcss.length-1][i];
//            }
//        }
//        return 1-((double)max/m);
    }

    private static final String TOLERANCE_KEY = "-t";

    @Override
    public String[] getOptions() {
        String[] superOptions = super.getOptions();
        String[] options = new String[superOptions.length + 2];
        System.arraycopy(superOptions, 0, options, 0, superOptions.length);
        options[options.length - 2] = TOLERANCE_KEY;
        options[options.length - 1] = String.valueOf(tolerance);
        return options;
    }

    @Override
    public boolean setOption(final String key, final String value) {
        if(key.equals(TOLERANCE_KEY)) {
            setTolerance(Double.parseDouble(value));
            return true;
        } else {
            return super.setOption(key, value);
        }
    }

}
