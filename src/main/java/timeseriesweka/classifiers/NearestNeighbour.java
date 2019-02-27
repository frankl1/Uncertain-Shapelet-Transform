package timeseriesweka.classifiers;

import scala.reflect.macros.runtime.JavaReflectionRuntimes;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;
import utilities.ArrayUtilities;
import utilities.Reproducible;
import utilities.Utilities;
import weka.classifiers.AbstractClassifier;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

public class NearestNeighbour extends AbstractClassifier implements Reproducible {

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

    public void setSeed(long seed) {
        random.setSeed(seed);
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    private Instances sampledTrainInstances;
    private Random random = new Random();
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

    public long getTrainDuration() {
        return trainDuration;
    }

    private long trainDuration = 0;

    public long getTestDuration() {
        return testDuration + predictDuration;
    }

    private long testDuration = 0;
    private long predictDuration = 0;

    public boolean isCvTrain() {
        return cvTrain;
    }

    public void setCvTrain(final boolean cvTrain) {
        this.cvTrain = cvTrain;
    }

    private boolean cvTrain = false;

    @Override
    public void buildClassifier(final Instances trainInstances) throws Exception {
        long timeStamp = System.nanoTime();
        if(!trainInstances.equals(this.originalTrainInstances)) {
            // reset as train dataset has change
            this.originalTrainInstances = trainInstances;
            this.sampledTrainInstances = new Instances(trainInstances, 0);
            instancesByClassMap = Utilities.instancesByClassMap(trainInstances);
            classSamplingProbabilities = new TreeMap<>();
            for(Double classValue : instancesByClassMap.keySet()) {
                classSamplingProbabilities.put((double) instancesByClassMap.get(classValue).numInstances() / trainInstances.numInstances(), classValue);
            }
            classDistribution = Utilities.classDistribution(trainInstances);
            trainNearestNeighbourFinders = new ArrayList<>();
            trainDuration = 0;
        }
        int sampleSize = getSampleSize();
        while (sampledTrainInstances.numInstances() < sampleSize) {
            Instance sampledInstance = sampleTrainInstance();
            sampledTrainInstances.add(sampledInstance);
            if(cvTrain) {
                trainNearestNeighbourFinders.add(new NearestNeighbourFinder(sampledInstance));
            }
        }
        trainDuration += System.nanoTime() - timeStamp;
    }

    // todo some means of getting train cv stats - ask james

    // todo different distributionForInstances, e.g. class weighting, random tie break, absolute, etc

    @Override
    public double[] distributionForInstance(final Instance testInstance) throws Exception {
        return distributionForInstances(Utilities.instanceToInstances(testInstance))[0];
    }

    private Instances originalTestInstances;
    private List<NearestNeighbourFinder> testNearestNeighbourFinders;

    public double[][] distributionForInstances(final Instances testInstances) {
        long timeStamp = System.nanoTime();
        if(!testInstances.equals(originalTestInstances)) {
            originalTestInstances = testInstances;
            testNearestNeighbourFinders = new ArrayList<>();
            for(Instance testInstance : testInstances) {
                testNearestNeighbourFinders.add(new NearestNeighbourFinder(testInstance));
            }
            testDuration = 0;
        }
        while (!sampledTrainInstances.isEmpty()) {
            Instance sampledTrainInstance = sampledTrainInstances.remove(random.nextInt(sampledTrainInstances.numInstances()));
            for(NearestNeighbourFinder testNearestNeighbourFinder : testNearestNeighbourFinders) {
                testNearestNeighbourFinder.addNeighbour(sampledTrainInstance);
            }
        }
        testDuration += System.nanoTime() - timeStamp;
        timeStamp = System.nanoTime();
        double[][] predictions = new double[testNearestNeighbourFinders.size()][];
        for(int i = 0; i < predictions.length; i++) {
            predictions[i] = testNearestNeighbourFinders.get(i).predict();
        }
        predictDuration = System.nanoTime() - timeStamp;
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
