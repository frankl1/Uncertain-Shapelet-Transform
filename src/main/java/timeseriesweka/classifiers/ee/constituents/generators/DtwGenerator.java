package timeseriesweka.classifiers.ee.constituents.generators;

import timeseriesweka.classifiers.ee.constituents.Indexed;
import timeseriesweka.classifiers.ee.constituents.IndexedMutator;
import timeseriesweka.classifiers.ee.constituents.TargetedMutator;
import timeseriesweka.classifiers.ee.index.LinearInterpolater;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;
import utilities.Box;
import weka.core.Instances;

import java.util.List;

public class DtwGenerator extends NnGenerator {

    protected final Box<Dtw> distanceMeasureBox = new Box<>();
    private final IndexedMutator<Dtw, Double> warpingWindowParameter = new IndexedMutator<>(Dtw.WARPING_WINDOW_MUTABLE);
    private final TargetedMutator<Dtw> warpingWindowMutator = new TargetedMutator<>(warpingWindowParameter, distanceMeasureBox);

    public DtwGenerator() {
        List<Indexed> parameters = getParameters().getIndexeds();
        parameters.add(warpingWindowMutator);
    }


    @Override
    protected DistanceMeasure getDistanceMeasure() {
        distanceMeasureBox.setContents(new Dtw());
        return distanceMeasureBox.getContents();
    }

    @Override
    public void setParameterRanges(final Instances instances) {
        warpingWindowParameter.getValueRange().setIndexedSupplier(new LinearInterpolater(0, 1, 101));
    }

}