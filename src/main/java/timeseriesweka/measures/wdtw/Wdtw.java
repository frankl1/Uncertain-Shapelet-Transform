package timeseriesweka.measures.wdtw;

import timeseriesweka.measures.dtw.Dtw;

public class Wdtw extends Dtw implements WdtwInterface {

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Wdtw(double warp, double weight) {
        super(warp);
        setWeight(weight);
    }

    public Wdtw() {
        this(1, 0.05);
    }

    private double weight; // AKA g // 0.05 to 3 perhaps

    @Override
    protected double findCost(double[] timeSeriesA, int indexA, double[] timeSeriesB, int indexB) {
        return super.findCost(timeSeriesA, indexA, timeSeriesB, indexB) +
                1 / (1 + Math.exp(-weight * ((double) indexA - (double) timeSeriesA.length / 2)));
//                1 / (1 + Math.exp(-weight * (Math.abs(indexA - indexB) - (double)timeSeriesA.length / 2))); // this makes a difference, todo check agaisnt paper
    }

    @Override
    protected double measureDistance(final double[] timeSeriesA, final double[] timeSeriesB, final double cutOff) {

        // todo cleanup
        // todo trim memory to window by window
        // todo early abandon
        // todo might be able to inherit from dtw to use warping window perhaps?

        int seriesLength = timeSeriesA.length;
        double[] weightVector = new double[seriesLength];
        double halfLength = (double)seriesLength/2;

        for(int i = 0; i < seriesLength; i++){ // todo precompute and use in findCost func
            weightVector[i] = 1/(1+Math.exp(-weight*(i-halfLength)));
        }
        
        //create empty array
        int m = timeSeriesA.length;
        int n = timeSeriesB.length;
        double[][] distances = new double[m][n];

        //first value
        distances[0][0] = weightVector[0]*(timeSeriesA[0]-timeSeriesB[0])*(timeSeriesA[0]-timeSeriesB[0]);

        //early abandon if first values is larger than cut off
        if(distances[0][0] > cutOff){
            return Double.MAX_VALUE;
        }

        //top row
        for(int i=1;i<n;i++){
            distances[0][i] = distances[0][i-1]+weightVector[i]*(timeSeriesA[0]-timeSeriesB[i])*(timeSeriesA[0]-timeSeriesB[i]); //edited by Jay
        }

        //first column
        for(int i=1;i<m;i++){
            distances[i][0] = distances[i-1][0]+weightVector[i]*(timeSeriesA[i]-timeSeriesB[0])*(timeSeriesA[i]-timeSeriesB[0]); //edited by Jay
        }

        //warp rest
        double minDistance;
        for(int i = 1; i<m; i++){
            boolean overflow = true;

            for(int j = 1; j<n; j++){
                //calculate distances
                minDistance = Math.min(distances[i][j-1], Math.min(distances[i-1][j], distances[i-1][j-1]));
                distances[i][j] = minDistance+weightVector[Math.abs(i-j)] *(timeSeriesA[i]-timeSeriesB[j])*(timeSeriesA[i]-timeSeriesB[j]);

                if(overflow && distances[i][j] < cutOff){
                    overflow = false; // because there's evidence that the path can continue
                }
            }

            //early abandon
            if(overflow){
                return Double.MAX_VALUE;
            }
        }
        return distances[m-1][n-1];
    }

    @Override
    public String getRevision() {
        return "1";
    }

    public static final String WEIGHT_KEY = "weight";

    @Override
    public String[] getOptions() {
        String[] superOptions = super.getOptions();
        String[] options = new String[superOptions.length + 2];
        System.arraycopy(superOptions, 0, options, 0, superOptions.length);
        options[options.length - 2] = WEIGHT_KEY;
        options[options.length - 1] = String.valueOf(weight);
        return options;
    }

    @Override
    public boolean setOption(final String key, final String value) {
        if(key.equals(WEIGHT_KEY)) {
            setWeight(Double.parseDouble(value));
            return true;
        } else {
            return super.setOption(key, value);
        }
    }

}
