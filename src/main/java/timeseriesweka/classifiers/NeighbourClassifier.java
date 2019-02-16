package timeseriesweka.classifiers;

import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.euclidean.Euclidean;
import utilities.Utilities;
import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class NeighbourClassifier implements Classifier {

    private Long seed;
    private Instance[] neighbours;
    private DistanceMeasure distanceMeasure = new Euclidean();

    public boolean isUseDistancesInPrediction() {
        return useDistancesInPrediction;
    }

    public void setUseDistancesInPrediction(boolean useDistancesInPrediction) {
        this.useDistancesInPrediction = useDistancesInPrediction;
    }

    private boolean useDistancesInPrediction = false;

    public void setSeed(long seed) {
        this.seed = seed;
    }

    @Override
    public void buildClassifier(Instances trainInstances) throws Exception {
        Random random = new Random();
        if(seed != null) {
            random.setSeed(seed);
        }
        Instances[] instancesByClass = Utilities.instancesByClass(trainInstances);
        neighbours = new Instance[instancesByClass.length];
        for(int i = 0; i < instancesByClass.length; i++) {
            Instances homogeneousInstances = instancesByClass[i];
            neighbours[i] = homogeneousInstances.get(random.nextInt(homogeneousInstances.size()));
        }
    }

    @Override
    public double classifyInstance(Instance testInstance) throws Exception {
        return Utilities.maxIndex(distributionForInstance(testInstance));
    }

    private double[] neighbourDistances(Instance instance) {
        double[] distances = new double[neighbours.length];
        for(int i = 0; i < distances.length; i++) {
            distances[i] = distanceMeasure.distance(instance, neighbours[i]);
        }
        return distances;
    }

    @Override
    public double[] distributionForInstance(Instance testInstance) throws Exception {
        double[] distribution = neighbourDistances(testInstance);
        if(!isUseDistancesInPrediction()) {
            double[] temp = distribution;
            distribution = new double[temp.length];
            distribution[Utilities.maxIndex(temp)]++;
        } else {
            Utilities.normalise(distribution);
        }
        return distribution;
    }

    @Override
    public Capabilities getCapabilities() {
        throw new UnsupportedOperationException();
    }
}
