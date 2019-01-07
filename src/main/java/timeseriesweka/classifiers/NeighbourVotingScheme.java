package timeseriesweka.classifiers;

import timeseriesweka.classifiers.Neighbour;

import java.util.List;

public interface NeighbourVotingScheme {
    double[] distribution(List<Neighbour> nearestNeighbours);
}
