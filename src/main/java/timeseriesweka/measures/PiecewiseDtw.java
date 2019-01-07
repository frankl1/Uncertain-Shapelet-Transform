//package timeseriesweka.measures;
//
//import timeseriesweka.filters.Piecewise;
//import timeseriesweka.measures.dtw.Dtw;
//import weka.core.TechnicalInformation;
//
//public class PiecewiseDtw extends Dtw {
//
//    private final Piecewise piecewise;
//
//    public PiecewiseDtw(double warp, int frameSize) {
//        super(warp);
//        piecewise = new Piecewise(frameSize);
//    }
//
//    public int getFrameSize() {
//        return piecewise.getFrameSize();
//    }
//
//    public void setFrameSize(int frameSize) {
//        piecewise.setFrameSize(frameSize);
//    }
//
//    @Override
//    protected double measureDistance(double[] timeSeriesA, double[] timeSeriesB, double cutOff) {
//        return super.measureDistance(piecewise.filter(timeSeriesA),
//                piecewise.filter(timeSeriesB),
//                cutOff);
//    }
//
//    @Override
//    public String getRevision() {
//        return null; // todo
//    }
//
//    @Override
//    public TechnicalInformation getTechnicalInformation() {
//        return null; // todo
//    }
//}
