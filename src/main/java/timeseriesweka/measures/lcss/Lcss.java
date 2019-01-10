package timeseriesweka.measures.lcss;

import timeseriesweka.measures.DistanceMeasure;

public class Lcss extends DistanceMeasure {

    public Lcss(double epsilon, int delta) {
        this.tolerance = epsilon;
        this.warpingWindow = delta;
    }

    // delta === warp
    // epsilon === diff between two values before they're considered the same

    public Lcss() {
        this(0.01, 1);
    }

    private double tolerance;

    public double getTolerance() {
        return tolerance;
    }

    public void setTolerance(double epsilon) {
        this.tolerance = epsilon;
    }

    public double getWarpingWindow() {
        return warpingWindow;
    } // todo change to double percetnage

    public void setWarpingWindow(double warpingWindow) {
        this.warpingWindow = warpingWindow;
    }

    private double warpingWindow;

    @Override
    protected double measureDistance(double[] first, double[] second, double cutOff) {
        double[] a  = first;
        double[] b = second;
        int m = first.length;
        int n = second.length;

        int[][] lcss = new int[m+1][n+1];
        int[][] lastX = new int[m+1][n+1];
        int[][] lastY = new int[m+1][n+1];

        int warpingWindow = (int) Math.round(this.warpingWindow * first.length);

        for(int i = 0; i < m; i++){
            for(int j = i-warpingWindow; j <= i+warpingWindow; j++){
//                System.out.println("here");
                if(j < 0 || j >= n){
                    //do nothing
                }else if(b[j]+this.tolerance >= a[i] && b[j]-tolerance <=a[i]){
                    lcss[i+1][j+1] = lcss[i][j]+1;
                    lastX[i+1][j+1] = i;
                    lastY[i+1][j+1] = j;
                }else if(lcss[i][j+1] > lcss[i+1][j]){
                    lcss[i+1][j+1] = lcss[i][j+1];
                    lastX[i+1][j+1] = i;
                    lastY[i+1][j+1] = j+1;
                }else{
                    lcss[i+1][j+1] = lcss[i+1][j];
                    lastX[i+1][j+1] = i+1;
                    lastY[i+1][j+1] = j;
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
    }

    @Override
    public String getRevision() {
        return null;
    }

    @Override
    public String getParameters() {
        return "tolerance=" + tolerance + ",warpingWindow=" + warpingWindow;
    }

    @Override
    public String toString() {
        return "lcss";
    }


}
