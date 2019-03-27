package timeseriesweka.classifiers.nn.NeighbourWeighting;

public class WeightByDistance implements NeighbourWeighter {
    @Override
    public double weight(final double distance) {
        return 1 / (1 + distance);
    }
}