package development.go.Ee.Constituents;

import development.go.Ee.Constituents.ParameterSpaces.*;
import development.go.Indexed.IndexConsumer;
import timeseriesweka.classifiers.nn.Nn;
import timeseriesweka.measures.DistanceMeasure;
import utilities.Utilities;
import weka.core.Instances;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConstituentBuilder extends AbstractConstituentBuilder {

    public ParameterSpace<? extends DistanceMeasure> getParameterSpace() {
        return parameterSpace;
    }

    public void setParameterSpace(final ParameterSpace<? extends DistanceMeasure> parameterSpace) {
        this.parameterSpace = parameterSpace;
    }

    private ParameterSpace<? extends DistanceMeasure> parameterSpace;

    public ConstituentBuilder(final ParameterSpace<? extends DistanceMeasure> parameterSpace) {
        setParameterSpace(parameterSpace);
    }

    @Override
    protected List<IndexConsumer<?>> setupParameters(final Instances instances) {
        return null;
    }

    @Override
    public Nn build() {
        Nn nn = new Nn();
        DistanceMeasure distanceMeasure = parameterSpace.build();
        nn.setDistanceMeasure(distanceMeasure);
        return nn;
    }

    public static void main(String[] args) throws IOException {
        Instances instances = Utilities.loadDataset(new File("/scratch/Datasets/TSCProblems2015/GunPoint"));
        List<ParameterSpace<? extends DistanceMeasure>> parameterSpaces = new ArrayList<>(Arrays.asList(
            new DtwParameterSpace(),
            new DdtwParameterSpace(),
            new WdtwParameterSpace(),
            new WddtwParameterSpace(),
            new LcssParameterSpace(),
            new ErpParameterSpace(),
            new MsmParameterSpace(),
            new TweParameterSpace()
        ));
        for(ParameterSpace<? extends DistanceMeasure> parameterSpace : parameterSpaces) {
            parameterSpace.useInstances(instances);
        }
        for(ParameterSpace<? extends DistanceMeasure> parameterSpace : parameterSpaces) {
            int size = parameterSpace.size();
            for(int i = 0; i < size; i++) {
                parameterSpace.setCombination(i);
                DistanceMeasure distanceMeasure = parameterSpace.build();
                System.out.println(distanceMeasure.toString() + ": " + distanceMeasure.getParameters());
            }
        }
    }
}
