package timeseriesweka.classifiers.ee.abcdef.generators;

import timeseriesweka.classifiers.ee.abcdef.Indexed;
import timeseriesweka.classifiers.ee.abcdef.IndexedMutator;
import timeseriesweka.classifiers.ee.abcdef.TargetedMutator;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;
import timeseriesweka.measures.erp.Erp;
import timeseriesweka.measures.wdtw.Wdtw;
import utilities.Box;

import java.util.List;

public class ErpGenerator extends NnGenerator {
    private final Box<Erp> distanceMeasureBox = new Box<>();
    private final IndexedMutator<Erp, Double> warpingWindowParameter = new IndexedMutator<>(Erp.WARPING_WINDOW_MUTABLE);
    private final TargetedMutator<Erp> warpingWindowMutator = new TargetedMutator<>(warpingWindowParameter, distanceMeasureBox);
    private final IndexedMutator<Erp, Double> penaltyParameter = new IndexedMutator<>(Erp.PENALTY_MUTABLE);
    private final TargetedMutator<Erp> penaltyMutator = new TargetedMutator<>(penaltyParameter, distanceMeasureBox);

    public ErpGenerator() {
        List<Indexed> parameters = getParameters().getIndexeds();
        parameters.add(warpingWindowMutator);
        parameters.add(penaltyMutator);
    }

    @Override
    protected DistanceMeasure getDistanceMeasure() {
        distanceMeasureBox.setContents(new Erp());
        return distanceMeasureBox.getContents();
    }
}
