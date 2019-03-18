package timeseriesweka.classifiers.nn.NeighbourWeighting;

public class UniformWeighting implements NeighbourWeighter {
    @Override
    public double weight(final double distance) {
        return 1;
    }
}
