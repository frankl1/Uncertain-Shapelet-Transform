package timeseriesweka.classifiers.nn.NeighbourWeighting;


import java.io.Serializable;

public interface NeighbourWeighter extends Serializable {
    double weight(double distance);
}
