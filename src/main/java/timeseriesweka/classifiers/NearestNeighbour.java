package timeseriesweka.classifiers;

import net.sourceforge.sizeof.SizeOf;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;
import utilities.ArrayUtilities;
import utilities.ClassifierResults;
import utilities.Reproducible;
import utilities.Utilities;
import weka.classifiers.AbstractClassifier;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

public class NearestNeighbour extends AbstractClassifier implements Reproducible, SaveParameterInfo {

    // todo implement checkpointing and contracting

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

    public ClassifierResults getTrainPrediction(Instances trainInstances) throws Exception {
        buildClassifier(trainInstances);
        ClassifierResults results = new ClassifierResults();
        results.setNumClasses(originalTrainInstances.numClasses());
        results.setNumInstances(trainNearestNeighbourFinders.size());
        for(NearestNeighbourFinder nearestNeighbourFinder : trainNearestNeighbourFinders) {
            results.storeSingleResult(nearestNeighbourFinder.classValue(), nearestNeighbourFinder.predict());
        }
        results.memory = SizeOf.deepSizeOf(this);
        results.setName(toString());
        results.setParas(getParameters());
        results.setTrainTime(getTrainDuration());
        results.setTestTime(-1);
        return results;
    }

    public ClassifierResults getTestPredictions(Instances testInstances) {
        double[][] predictions = distributionForInstances(testInstances);
        ClassifierResults results = new ClassifierResults();
        results.setNumClasses(testInstances.numClasses());
        results.setNumInstances(testInstances.numInstances());
        for(int i = 0; i < predictions.length; i++) {
            results.storeSingleResult(testInstances.get(i).classValue(), predictions[i]);
        }
        results.memory = SizeOf.deepSizeOf(this);
        results.setName(toString());
        results.setParas(getParameters());
        results.setTrainTime(getTrainDuration());
        results.setTestTime(getTestDuration());
        return results;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public boolean usesRandomTieBreak() {
        return useRandomTieBreak;
    }

    public void setUseRandomTieBreak(final boolean useRandomTieBreak) {
        this.useRandomTieBreak = useRandomTieBreak;
    }

    private boolean useRandomTieBreak = false;

    @Override
    public double classifyInstance(final Instance instance) throws Exception {
        double[] prediction = distributionForInstance(instance);
        int[] maxIndices = Utilities.argMax(prediction);
        if(useRandomTieBreak) {
            return prediction[maxIndices[random.nextInt(maxIndices.length)]];
        } else {
            return prediction[maxIndices[0]];
        }
    }

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

    public boolean usesEarlyAbandon() {
        return useEarlyAbandon;
    }

    public void setUseEarlyAbandon(final boolean useEarlyAbandon) {
        this.useEarlyAbandon = useEarlyAbandon;
    }

    private boolean useEarlyAbandon = false;

    @Override
    public String getParameters() {
        return null; // todo delegate to getOptions
    }

    // todo getOptions setOptions

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

        public double classValue() {
            return instance.classValue();
        }

        private double findCutOff() {
            if(useEarlyAbandon) {
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
                Instance neighbour = neighbours.remove(random.nextInt(neighbours.size()));
                predictions[(int) neighbour.classValue()] += neighbourWeighter.weight(distance);
                neighbourCount++;
            }
            ArrayUtilities.normalise(predictions);
            return predictions;
        }
    }
}
