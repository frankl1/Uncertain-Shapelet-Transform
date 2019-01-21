package timeseriesweka.classifiers.ee.abcdef.generators;

import timeseriesweka.classifiers.ee.abcdef.Indexed;
import timeseriesweka.classifiers.ee.abcdef.IndexedMutator;
import timeseriesweka.classifiers.ee.abcdef.TargetedMutator;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;
import timeseriesweka.measures.wdtw.Wdtw;
import utilities.Box;

import java.util.List;

public class WdtwGenerator extends NnGenerator {

    protected final Box<Wdtw> distanceMeasureBox = new Box<>();
    private final IndexedMutator<Dtw, Double> warpingWindowParameter = new IndexedMutator<>(Dtw.WARPING_WINDOW_MUTABLE);
    private final TargetedMutator<Dtw> warpingWindowMutator = new TargetedMutator<>(warpingWindowParameter, distanceMeasureBox);
    private final IndexedMutator<Wdtw, Double> weightParameter = new IndexedMutator<>(Wdtw.WEIGHT_MUTABLE);
    private final TargetedMutator<Wdtw> weightMutator = new TargetedMutator<>(weightParameter, distanceMeasureBox);

    public WdtwGenerator() {
        List<Indexed> parameters = getParameters().getIndexeds();
        parameters.add(warpingWindowMutator);
        parameters.add(weightMutator);
    }

    @Override
    protected DistanceMeasure getDistanceMeasure() {
        distanceMeasureBox.setContents(new Wdtw());
        return distanceMeasureBox.getContents();
    }

}
