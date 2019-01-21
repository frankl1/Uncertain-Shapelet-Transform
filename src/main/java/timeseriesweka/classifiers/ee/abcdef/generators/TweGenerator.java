package timeseriesweka.classifiers.ee.abcdef.generators;

import timeseriesweka.classifiers.ee.abcdef.Indexed;
import timeseriesweka.classifiers.ee.abcdef.IndexedMutator;
import timeseriesweka.classifiers.ee.abcdef.TargetedMutator;
import timeseriesweka.classifiers.ee.index.LinearInterpolater;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.twe.Twe;
import timeseriesweka.measures.wdtw.Wdtw;
import utilities.Box;
import utilities.StatisticUtilities;
import weka.core.Instances;

import java.util.List;

public class TweGenerator extends NnGenerator {
    private final Box<Twe> distanceMeasureBox = new Box<>();
    private final IndexedMutator<Twe, Double> lambdaParameter = new IndexedMutator<>(Twe.LAMBDA_MUTABLE);
    private final TargetedMutator<Twe> lambdaMutator = new TargetedMutator<>(lambdaParameter, distanceMeasureBox);
    private final IndexedMutator<Twe, Double> nuParameter = new IndexedMutator<>(Twe.NU_MUTABLE);
    private final TargetedMutator<Twe> nuMutator = new TargetedMutator<>(nuParameter, distanceMeasureBox);

    public TweGenerator() {
        List<Indexed> parameters = getParameters().getIndexeds();
        parameters.add(lambdaMutator);
        parameters.add(nuMutator);
    }

    @Override
    protected DistanceMeasure getDistanceMeasure() {
        distanceMeasureBox.setContents(new Twe());
        return distanceMeasureBox.getContents();
    }

    @Override
    public void setParameterRanges(final Instances instances) {
        nuParameter.getValueRange().setIndexedSupplier(new LinearInterpolater(0, 9, 10));
        lambdaParameter.getValueRange().setIndexedSupplier(new LinearInterpolater(0, 9, 10));
    }
}
