package timeseriesweka.classifiers.ee.constituents.generators;

import timeseriesweka.classifiers.ee.constituents.Indexed;
import timeseriesweka.classifiers.ee.constituents.IndexedMutator;
import timeseriesweka.classifiers.ee.constituents.TargetedMutator;
import timeseriesweka.classifiers.ee.index.ElementObtainer;
import timeseriesweka.classifiers.ee.index.LinearInterpolater;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.twe.Twe;
import utilities.Box;
import weka.core.Instances;

import java.util.Arrays;
import java.util.List;

public class TweParameterisedSupplier extends ParameterisedSupplier<Twe> {
    private final IndexedMutator<Twe, Double> lambdaParameter = new IndexedMutator<>(Twe.LAMBDA_MUTABLE);
    private final TargetedMutator<Twe> lambdaMutator = new TargetedMutator<>(lambdaParameter, getBox());
    private final IndexedMutator<Twe, Double> nuParameter = new IndexedMutator<>(Twe.NU_MUTABLE);
    private final TargetedMutator<Twe> nuMutator = new TargetedMutator<>(nuParameter, getBox());

    public TweParameterisedSupplier() {
        List<Indexed> parameters = getParameters().getIndexeds();
        parameters.add(lambdaMutator);
        parameters.add(nuMutator);
    }

    @Override
    protected Twe get() {
        return new Twe();
    }

    @Override
    public void setParameterRanges(final Instances instances) {
        Double[] nuParams = {
            // <editor-fold defaultstate="collapsed" desc="hidden for space">
            0.00001,
            0.0001,
            0.0005,
            0.001,
            0.005,
            0.01,
            0.05,
            0.1,
            0.5,
            1d,// </editor-fold>
        };
        ElementObtainer<Double> elementObtainer = new ElementObtainer<>(Arrays.asList(nuParams));
        nuParameter.getValueRange().setIndexedSupplier(elementObtainer);
        lambdaParameter.getValueRange().setIndexedSupplier(new LinearInterpolater(0, 0.1, 10));
    }
}
