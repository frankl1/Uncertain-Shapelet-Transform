package timeseriesweka.classifiers.Nn.NeighbourWeighting;

public class UniformWeighting implements NeighbourWeighter {
    @Override
    public double weight(final double distance) {
        return 1;
    }
}
