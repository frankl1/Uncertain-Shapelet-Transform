package timeseriesweka.classifiers.Nn.NeighbourWeighting;


import java.io.Serializable;

public interface NeighbourWeighter extends Serializable {
    double weight(double distance);
}
