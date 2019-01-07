//package timeseriesweka.measures;
//
//import timeseriesweka.filters.DerivativeFilter;
//import timeseriesweka.measures.dtw.Dtw;
//import weka.core.TechnicalInformation;
//
//public class Ddtw extends Dtw {
//
//    private final static DerivativeFilter derivativeFilter = new DerivativeFilter();
//
//    public Ddtw(double warp) {
//        super(warp);
//    }
//
//    @Override
//    protected double measureDistance(double[] timeSeriesA, double[] timeSeriesB, double cutOff) {
//        return super.measureDistance(derivativeFilter.,
//                DerivativeFilter.GLOBAL.filter(timeSeriesB),
//                cutOff);
//    }
//
//    @Override
//    public String getRevision() {
//        return null;
//    }
//
//    @Override
//    public TechnicalInformation getTechnicalInformation() {
//        return null;
//    }
//}
