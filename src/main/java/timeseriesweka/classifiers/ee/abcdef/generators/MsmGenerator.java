package timeseriesweka.classifiers.ee.abcdef.generators;

import timeseriesweka.classifiers.ee.abcdef.Indexed;
import timeseriesweka.classifiers.ee.abcdef.IndexedMutator;
import timeseriesweka.classifiers.ee.abcdef.TargetedMutator;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.msm.Msm;
import utilities.Box;

import java.util.List;

public class MsmGenerator extends NnGenerator {

    private final Box<Msm> distanceMeasureBox = new Box<>();
    private final IndexedMutator<Msm, Double> warpingWindowParameter = new IndexedMutator<>(Msm.WARPING_WINDOW_MUTABLE);
    private final TargetedMutator<Msm> warpingWindowMutator = new TargetedMutator<>(warpingWindowParameter, distanceMeasureBox);
    private final IndexedMutator<Msm, Double> costParameter = new IndexedMutator<>(Msm.COST_MUTABLE);
    private final TargetedMutator<Msm> costMutator = new TargetedMutator<>(costParameter, distanceMeasureBox);

    public MsmGenerator() {
        List<Indexed> parameters = getParameters().getIndexeds();
        parameters.add(warpingWindowMutator);
        parameters.add(costMutator);
    }

    @Override
    protected DistanceMeasure getDistanceMeasure() {
        distanceMeasureBox.setContents(new Msm());
        return distanceMeasureBox.getContents();
    }
}
