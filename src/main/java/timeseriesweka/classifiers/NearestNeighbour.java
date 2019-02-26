package timeseriesweka.classifiers;

import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;
import utilities.ArrayUtilities;
import utilities.Utilities;
import weka.classifiers.AbstractClassifier;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

public class NearestNeighbour extends AbstractClassifier {

    private Instances originalTrainInstances = null;

    public double getSampleSizePercentage() {
        return sampleSizePercentage;
    }

    public void setSampleSizePercentage(final double percentage) {
        Utilities.percentageCheck(percentage);
        this.sampleSizePercentage = percentage;
    }

    private double sampleSizePercentage = 1;

    private int getK() {
        return 1 + (int) kPercentage * originalTrainInstances.numInstances();
    }

    private int getSampleSize() {
        return (int) (sampleSizePercentage * originalTrainInstances.numInstances());
    }

    private Instances sampledTrainInstances = null;
    private Random random = new Random(); // todo set seed / set random
    private TreeMap<Double, Double> classSamplingProbabilities; // sampling probability by class value
    private double[] classDistribution;
    private Map<Double, Instances> instancesByClassMap; // class value by instances of that class
    private List<NearestNeighbourFinder> trainNearestNeighbourFinders;

    public double getkPercentage() {
        return kPercentage;
    }

    public void setkPercentage(final double percentage) {
        Utilities.percentageCheck(percentage);
        this.kPercentage = percentage;
    }

    private double kPercentage = 0;

    private Instance sampleTrainInstance() {
        Map.Entry<Double, Double> lastEntry = classSamplingProbabilities.pollLastEntry(); // most likely class to be sampled
        Double classValue = lastEntry.getValue();
        Instances homogeneousInstances = instancesByClassMap.get(classValue); // instances of the class value
        Instance sampledInstance = homogeneousInstances.remove(random.nextInt(homogeneousInstances.numInstances()));
        if(homogeneousInstances.numInstances() > 0) {
            double classSamplingProbability = classDistribution[classValue.intValue()];
            Double nextSamplingProbability = lastEntry.getKey() - classSamplingProbability;
            classSamplingProbabilities.put(nextSamplingProbability, classValue);
        }
        return sampledInstance;
    }

    @Override
    public void buildClassifier(final Instances trainInstances) throws Exception {
        if(trainInstances == null) throw new IllegalArgumentException("train instances cannot be null");
        if(trainInstances == this.originalTrainInstances) { // todo sort out repeat calls as someone could have messed with the train set inbetween
            // same train instances, therefore use current build progress so far
            if(sampledTrainInstances == null) {
                sampledTrainInstances = new Instances(trainInstances, 0);
                trainNearestNeighbourFinders = new ArrayList<>();
            }
        } else {
            // reset as train dataset has change
            this.originalTrainInstances = trainInstances;
            this.sampledTrainInstances = new Instances(trainInstances, 0);
            instancesByClassMap = Utilities.instancesByClassMap(trainInstances);
            for(Double classValue : instancesByClassMap.keySet()) {
                classSamplingProbabilities.put((double) instancesByClassMap.get(classValue).numInstances() / trainInstances.numInstances(), classValue);
            }
            classDistribution = Utilities.classDistribution(trainInstances);
        }
        int sampleSize = getSampleSize();
        while (sampledTrainInstances.numInstances() < sampleSize) {
            Instance sampledInstance = sampleTrainInstance();
            sampledTrainInstances.add(sampledInstance);
            trainNearestNeighbourFinders.add(new NearestNeighbourFinder(sampledInstance));
        }
    }

    // todo some means of getting train cv stats - ask james

    // todo different distributionForInstances, e.g. class weighting, random tie break, absolute, etc

    @Override
    public double[] distributionForInstance(final Instance testInstance) throws Exception {
        if(originalTrainInstances == null) throw new IllegalStateException("not trained");

    }

    private Instances originalTestInstances;
    private List<NearestNeighbourFinder> testNearestNeighbourFinders;

    public double[][] distributionForInstances(final Instances testInstances) {
        // todo check test instances same as current
        if(testInstances == originalTestInstances) { // if same as test instances from previous calls // todo fix this, not a good check, see build

        } else {
            originalTestInstances = testInstances;
            testNearestNeighbourFinders = new ArrayList<>();
            for(Instance testInstance : testInstances) {
                testNearestNeighbourFinders.add(new NearestNeighbourFinder(testInstance));
            }
        }
        while (!sampledTrainInstances.isEmpty()) {
            Instance sampledTrainInstance = sampledTrainInstances.remove(random.nextInt(sampledTrainInstances.numInstances())); // todo use dedicated random
            for(NearestNeighbourFinder testNearestNeighbourFinder : testNearestNeighbourFinders) {
                testNearestNeighbourFinder.addNeighbour(sampledTrainInstance);
            }
        }
        double[][] predictions = new double[testNearestNeighbourFinders.size()][];
        for(int i = 0; i < predictions.length; i++) {
            predictions[i] = testNearestNeighbourFinders.get(i).predict();
        }
        return predictions;
    }

    public DistanceMeasure getDistanceMeasure() {
        return distanceMeasure;
    }

    public void setDistanceMeasure(final DistanceMeasure distanceMeasure) {
        this.distanceMeasure = distanceMeasure;
    }

    private DistanceMeasure distanceMeasure;

    public NearestNeighbour() {
        setDistanceMeasure(new Dtw());
    }

    public boolean isEarlyAbandon() {
        return earlyAbandon;
    }

    public void setEarlyAbandon(final boolean earlyAbandon) {
        this.earlyAbandon = earlyAbandon;
    }

    private boolean earlyAbandon = false;

    public interface NeighbourWeighter {
        double weight(double distance);
    }

    public static final NeighbourWeighter WEIGHT_BY_DISTANCE = distance -> 1 / (1 + distance);

    public static final NeighbourWeighter WEIGHT_UNIFORM = distance -> 1;

    public NeighbourWeighter getNeighbourWeighter() {
        return neighbourWeighter;
    }

    public void setNeighbourWeighter(final NeighbourWeighter neighbourWeighter) {
        this.neighbourWeighter = neighbourWeighter;
    }

    private NeighbourWeighter neighbourWeighter = WEIGHT_BY_DISTANCE;

    private class NearestNeighbourFinder {
        private Instance instance;
        private TreeMap<Double, List<Instance>> nearestNeighbours = new TreeMap<>();
        private int neighbourCount = 0;

        public NearestNeighbourFinder(Instance instance) {
            this.instance = instance;
        }

        private double findCutOff() {
            if(earlyAbandon) {
                return nearestNeighbours.lastKey();
            } else {
                return Double.POSITIVE_INFINITY;
            }
        }

        public void addNeighbour(Instance neighbour) {
            int k = getK();
            double distance = distanceMeasure.distance(instance, neighbour, findCutOff());
            nearestNeighbours.computeIfAbsent(distance, key -> new ArrayList<>());
            neighbourCount++;
            Map.Entry<Double, List<Instance>> furthestNeighboursEntry = nearestNeighbours.lastEntry();
            if(neighbourCount - k > furthestNeighboursEntry.getValue().size()) {
                neighbourCount -= nearestNeighbours.pollLastEntry().getValue().size();
            }
        }

        public double[] predict() {
            double[] predictions = new double[instance.numClasses()];
            int neighbourCount = 0;
            int k = getK();
            NavigableSet<Double> distances = nearestNeighbours.navigableKeySet();
            Double distance;
            while (distances.size() > 1) {
                distance = distances.pollFirst();
                List<Instance> neighbours = this.nearestNeighbours.get(distance);
                for(Instance neighbour : neighbours) {
                    predictions[(int) neighbour.classValue()] += neighbourWeighter.weight(distance);
                    neighbourCount++;
                }
            }
            distance = distances.pollFirst();
            List<Instance> neighbours = new ArrayList<>(this.nearestNeighbours.get(distance));
            while (neighbourCount <= k) {
                Instance neighbour = neighbours.remove(random.nextInt(neighbours.size())); // todo this might cause inconsistencies, i.e. what if classification occurs more than once? Sampling will not be the same after. Need to use a different random
                predictions[(int) neighbour.classValue()] += neighbourWeighter.weight(distance);
                neighbourCount++;
            }
            ArrayUtilities.normalise(predictions);
            return predictions;
        }
    }
}
