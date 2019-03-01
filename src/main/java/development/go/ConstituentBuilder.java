package development.go;

import timeseriesweka.classifiers.nn.NearestNeighbour;
import timeseriesweka.measures.lcss.Lcss;
import weka.classifiers.Classifier;
import weka.core.Instances;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ConstituentBuilder {

    public void setInstances(Instances instances) {
        // derive parameter ranges
    }

    public NearestNeighbour build(final Integer combination) {
        Lcss lcss = new Lcss();
        NearestNeighbour nn = new NearestNeighbour();
        nn.setDistanceMeasure(lcss);

        // set tolerance from parameter ranges
        // set warp from parameter ranges
        // set k (say) from parameter ranges

        return nn;
    }
}
