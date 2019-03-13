package development.go.Ee.ConstituentBuilders;

import timeseriesweka.classifiers.nn.Nn;
import timeseriesweka.measures.DistanceMeasure;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

public class DistanceMeasureNnBuilder<A extends DistanceMeasure> extends NnBuilder {
    private final ConfiguredBuilder<A> distanceMeasureBuilder;

    public DistanceMeasureNnBuilder(final ConfiguredBuilder<A> lcssBuilder) {
        this.distanceMeasureBuilder = lcssBuilder;
    }

    @Override
    protected void setupParameters(final Instances instances) {
        super.setupParameters(instances);
        distanceMeasureBuilder.setupParameters(instances);
    }

    @Override
    protected int getNumParameters() {
        return super.getNumParameters() + distanceMeasureBuilder.getNumParameters();
    }

    @Override
    protected List<Integer> getParameterSizes() {
        List<Integer> parameterSizes = super.getParameterSizes();
        parameterSizes.addAll(distanceMeasureBuilder.getParameterSizes());
        return parameterSizes;
    }

    @Override
    public void configure(final Nn nn, List<Integer> parametersPermutation) {
        List<Integer> distanceMeasureParametersPermutation = new ArrayList<>();
        int numNnParameters = super.getNumParameters();
        for(int i = numNnParameters; i < parametersPermutation.size(); i++) {
            distanceMeasureParametersPermutation.add(parametersPermutation.remove(numNnParameters));
        }
        super.configure(nn, parametersPermutation);
        distanceMeasureBuilder.setParametersPermutation(distanceMeasureParametersPermutation);
        nn.setDistanceMeasure(distanceMeasureBuilder.build());
    }
}
