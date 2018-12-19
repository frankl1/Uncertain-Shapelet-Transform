package timeseriesweka.classifiers.ensembles.ee.Interpolation;

import timeseriesweka.classifiers.ensembles.ee.Indexing.IndexedObtainer;

public class LinearInterpolator extends IndexedObtainer<Double> {

    private final double start;
    private final double end;

    public LinearInterpolator(double start, double end, int num) {
        super(num);
        this.start = start;
        this.end = end;
    }

    protected Double obtainByIndex(Integer index) {
        double diff = end - start;
        return start + diff * index / getSize();
    }
}
