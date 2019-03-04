package development.go.Constituents;

import development.go.Constituents.ParameterSpaces.ParameterSpace;
import development.go.Indexed.IndexConsumer;
import timeseriesweka.classifiers.nn.NearestNeighbour;
import timeseriesweka.measures.DistanceMeasure;
import weka.core.Instances;

import java.util.List;

public class ConstituentBuilder extends AbstractConstituentBuilder {

    private ParameterSpace<? extends DistanceMeasure> parameterSpace;

    public ConstituentBuilder(final ParameterSpace<? extends DistanceMeasure> parameterSpace) {
        this.parameterSpace = parameterSpace;
    }

    @Override
    protected List<IndexConsumer<?>> setupParameters(final Instances instances) {
        return null;
    }

    @Override
    public NearestNeighbour build() {
        NearestNeighbour nearestNeighbour = new NearestNeighbour();
        DistanceMeasure distanceMeasure = parameterSpace.build();
        nearestNeighbour.setDistanceMeasure(distanceMeasure);
        return nearestNeighbour;
    }
}
