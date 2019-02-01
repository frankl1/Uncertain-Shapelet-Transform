package timeseriesweka.classifiers;

import timeseriesweka.classifiers.ee.iteration.*;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;
import utilities.*;
import utilities.range.Range;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

public class NearestNeighbour implements AdvancedClassifier {

    @Override
    public void setSavePath(String path) {

    }

    @Override
    public void copyFromSerObject(Object obj) throws Exception {

    }

    @Override
    public void setTimeLimit(long time) {

    }

    private class NeighbourSearcher {
        private final TreeMap<Double, TreeMap<Double, Integer>> neighbourClusters = new TreeMap<>();
        private final IndexIterator trainInstanceIterator;
        private final Instance testInstance;
        private int numNeighbours = 0;
        private final Random random = new Random();

        public NeighbourSearcher(Instance testInstance) {
            this.testInstance = testInstance;
            RandomIndexIterator randomIndexIterator = new RandomIndexIterator();
            randomIndexIterator.setRange(new Range(0, trainInstances.numInstances()));
            trainInstanceIterator = randomIndexIterator;
        }

        public boolean hasRemainingTicks() {
            return trainInstanceIterator.hasNext();
        }

        public void tick() {
            Instance trainInstance = trainInstances.get(trainInstanceIterator.next());
            double cutOff = getCutOff();
            double distance = distanceMeasure.distance(trainInstance, testInstance, cutOff);
            if(distance <= cutOff) {
                 addNeighbour(distance, trainInstance.classValue());
            }
        }

        public double[] predict() {
            TreeMap<Double, TreeMap<Double, Integer>> neighbours = findNeighbours();
            double[] probabilities = new double[testInstance.numClasses()];
            for(Double distance : neighbours.keySet()) {
                TreeMap<Double, Integer> neighbourCluster = neighbours.get(distance);
                for(Double classValue : neighbourCluster.keySet()) {
                    probabilities[classValue.intValue()] += neighbourCluster.get(classValue);
                }
            }
            ArrayUtilities.normalise(probabilities);
            return probabilities;
        }

        private TreeMap<Double, TreeMap<Double, Integer>> findNeighbours() {
            TreeMap<Double, TreeMap<Double, Integer>> neighbours = new TreeMap<>();
            for(Double key : neighbourClusters.keySet()) {
                neighbours.put(key, new TreeMap<>(neighbourClusters.get(key)));
            }
            int numNeighboursToRemove = numNeighbours - k;
            TreeMap<Double, Integer> lastCluster = neighbours.lastEntry().getValue();
            List<Double> lastClusterClassValues = new ArrayList<>(lastCluster.keySet());
            while (numNeighboursToRemove > 0) {
                numNeighboursToRemove--;
                Double randomClassValue = lastClusterClassValues.get(random.nextInt(lastClusterClassValues.size()));
                Integer count = lastCluster.get(randomClassValue);
                count--;
                if(count <= 0) {
                    lastCluster.remove(randomClassValue);
                } else {
                    lastCluster.put(randomClassValue, count);
                }
            }
            return neighbours;
        }

        private void addNeighbour(double distance, double classValue) {
            Map<Double, Integer> neighbourCluster = neighbourClusters.computeIfAbsent(distance, key -> new TreeMap<>());
            Integer count = neighbourCluster.get(classValue);
            if(count == null) {
                count = 0;
            } else {
                count++;
            }
            neighbourCluster.put(classValue, count);
            numNeighbours++;
            int kOverflow = numNeighbours - k;
            Map.Entry<Double, TreeMap<Double, Integer>> furtherNeighbourCluster = neighbourClusters.lastEntry();
            if(kOverflow > furtherNeighbourCluster.getValue().size()) {
                neighbourClusters.pollLastEntry();
            }
        }

        private double getCutOff() {
            if(useCutOff) {
                if(neighbourClusters.isEmpty()) {
                    return distanceMeasure.MAX;
                } else {
                    return neighbourClusters.lastKey();
                }
            } else {
                return DistanceMeasure.MAX;
            }
        }

        public void reset() {
            neighbourClusters.clear();
            trainInstanceIterator.reset();
            numNeighbours = 0;
        }

    }

    private Instances trainInstances;
    private Instances testInstances;
    private NeighbourSearcher[] neighbourSearchers;
    private AbstractIndexIterator testInstanceIndexIterator = new RoundRobinIndexIterator();
    private Instances[] instancesByClass;
    private int[] sampleSizes;
    private AbstractIndexIterator sampleSizesIndexIterator = new RoundRobinIndexIterator();
    private RandomIndexIterator sampleOverflowClassValueIterator = new RandomIndexIterator();
    private Random samplingRandom = new Random();

    public boolean remainingTrainTicks() {
        return sampleSizesIndexIterator.hasNext() || sampleOverflowClassValueIterator.hasNext();
    }

    public boolean remainingTestTicks() {
        return testInstanceIndexIterator.hasNext();
    }

    public void train() {
        while (remainingTrainTicks()) {
            trainTick();
        }
    }

    public void test() {
        while (remainingTestTicks()) {
            testTick();
        }
    }

    public void trainTick() {
        if(stratifiedSample) {
            int classValue;
            if(sampleSizesIndexIterator.hasNext()) {
                int sampleSizeIndex = sampleSizesIndexIterator.next();
                int sampleSize = sampleSizes[sampleSizeIndex];
                sampleSize--;
                if(sampleSize <= 0) {
                    sampleSizesIndexIterator.remove();
                }
                classValue = sampleSizeIndex;
            } else {
                // overflow
                classValue = sampleOverflowClassValueIterator.next();
                sampleOverflowClassValueIterator.remove();
            }
            Instances instances = instancesByClass[classValue];
            trainInstances.add(instances.remove(samplingRandom.nextInt(instances.numInstances())));
        }
    }

    public void setTrainInstances(Instances trainInstances) {
        if(stratifiedSample) {
            this.trainInstances = new Instances(trainInstances, 0);
            instancesByClass = Utilities.instancesByClass(trainInstances);
            sampleSizes = new int[instancesByClass.length];
            int sum = 0;
            for(int i = 0; i < sampleSizes.length; i++) {
                sampleSizes[i] = (int) (instancesByClass[i].numInstances() * samplePercentage);
                sum += sampleSizes[i];
            }
            sampleSizesIndexIterator.setRange(new Range(0, sampleSizes.length));
            int overallSampleSize = (int) (trainInstances.numInstances() * samplePercentage);
            int overflow = overallSampleSize - sum;
            sampleOverflowClassValueIterator.setRange(new Range(0, instancesByClass.length));
            for(int i = 0; i < instancesByClass.length - overflow; i++) {
                sampleOverflowClassValueIterator.next();
                sampleOverflowClassValueIterator.remove();
            }
        }
    }

    public void setTestInstances(Instances testInstances) {
        this.testInstances = testInstances;
        neighbourSearchers = new NeighbourSearcher[testInstances.numInstances()];
        for(int i = 0; i < neighbourSearchers.length; i++) {
            neighbourSearchers[i] = new NeighbourSearcher(testInstances.get(i));
        }
        testInstanceIndexIterator.setRange(new Range(0, neighbourSearchers.length)); // todo edge case when test instances is empty, same with train
    }

    public void testTick() {
        int testInstanceIndex = testInstanceIndexIterator.next();
        NeighbourSearcher neighbourSearcher = neighbourSearchers[testInstanceIndex];
        neighbourSearcher.tick();
        if(!neighbourSearcher.hasRemainingTicks()) {
            testInstanceIndexIterator.remove();
        }
    }

    public void reset() {
        testInstanceIndexIterator.reset();
        while (testInstanceIndexIterator.hasNext()) {
            int testInstanceIndex = testInstanceIndexIterator.next();
            neighbourSearchers[testInstanceIndex].reset();
        }
        testInstanceIndexIterator.reset();
        sampleSizesIndexIterator.reset();
        sampleOverflowClassValueIterator.reset();
        setSeed(seed);
    }

    public double[][] predict() {
        double[][] predictions = new double[testInstances.numInstances()][testInstances.numClasses()];
        for(int i = 0; i < predictions.length; i++) {
            predictions[i] = neighbourSearchers[i].predict();
        }
        return predictions;
    }

    public NearestNeighbour() {
        sampleOverflowClassValueIterator.setRandom(samplingRandom);
    }

    private int k = 1;

    public DistanceMeasure getDistanceMeasure() {
        return distanceMeasure;
    }

    public void setDistanceMeasure(DistanceMeasure distanceMeasure) {
        this.distanceMeasure = distanceMeasure;
    }

    private DistanceMeasure distanceMeasure = new Dtw();
    private final Random random = new Random(); // generic random for tied breaks, etc
    private Long seed = null;
    private double samplePercentage = 1;
    private long trainTime = -1;

    public boolean isStratifiedSample() {
        return stratifiedSample;
    }

    public void setStratifiedSample(final boolean stratifiedSample) {
        this.stratifiedSample = stratifiedSample;
    }

    private boolean stratifiedSample = true;

    public double getSamplePercentage() {
        return samplePercentage;
    }

    public void setSamplePercentage(double percentage) {
        if(percentage < 0) {
            throw new IllegalArgumentException(); // todo
        } else if(percentage > 1) {
            throw new IllegalArgumentException(); // todo
        } else {
            this.samplePercentage = percentage;
        }
    }

    @Override
    public void buildClassifier(Instances trainInstances) throws Exception {
        trainTime = System.nanoTime();
        setTrainInstances(trainInstances);
        while(remainingTrainTicks()) {
            trainTick();
        }
        trainTime = System.nanoTime() - trainTime;
    }

    @Override
    public double classifyInstance(Instance testInstance) throws Exception {
        return Utilities.max(distributionForInstance(testInstance));
    }

    @Override
    public String toString() {
        return distanceMeasure.toString() + "-nn";
    }

    public boolean isUsingCutOff() {
        return useCutOff;
    }

    public void useCutOff(boolean useCutOff) {
        this.useCutOff = useCutOff;
    }

    private boolean useCutOff = false;
    private NeighbourVotingScheme neighbourVotingScheme;

    private TreeMap<Double, Map<Integer, Integer>> findNeighbours(Instance testInstance) {
        // list of distances to neighbours (multiple neighbours *could* have same distance, hence list)
        TreeMap<Double, Map<Integer, Integer>> neighbours = new TreeMap<>();
        // for each instance in the train set
        double cutOff = DistanceMeasure.MAX;
        int numNeighbours = 0;
        for(int instanceIndex = 0; instanceIndex < trainInstances.size(); instanceIndex++) { // todo train set iteration technique
            // find distance between train instance and test instance
            Instance trainInstance = trainInstances.get(instanceIndex);
            Double distance = distanceMeasure.distance(trainInstance, testInstance, cutOff);
            // map of classValue to number of neighbours with said class
            Map<Integer, Integer> neighbourMap = neighbours.computeIfAbsent(distance, key -> new HashMap<>());
            Integer classValue = (int) trainInstance.classValue();
            Integer classValueCount = neighbourMap.get(classValue); // todo make class vals doubles
            if(classValueCount == null) {
                classValueCount = 1;
            } else {
                classValueCount++;
            }
            neighbourMap.put(classValue, classValueCount);
            numNeighbours++;
            if(numNeighbours > k && neighbours.size() > 1) {
                // too many neighbours and neighbours with different distances exist
                // if distance is less than the furthest neighbour's distance then current train instance must displace neighbour
                // check if number of neighbours can be trimmed
                // can only trim if the list of furthest neighbours is no longer in the k nearest neighbours
                Map.Entry<Double, Map<Integer, Integer>> furthestNeighboursEntry = neighbours.lastEntry();
                Map<Integer, Integer> furthestNeighboursMap = furthestNeighboursEntry.getValue();
                int size = 0;
                for(Integer count : furthestNeighboursMap.values()) {
                    size += count;
                }
                if(numNeighbours - k >= size) {
                    // can remove furthest neighbours entirely as sufficient number of closer neighbours
                    neighbours.pollLastEntry();
                    numNeighbours -= size;
                }
                if(useCutOff) {
                    cutOff = neighbours.lastKey();
                }
            }
        }
        // no more neighbours to be found, therefore select k closest from the current neighbour list
        // the last entry contains the redundant neighbours, if any
        // neighbours need to be randomly selected from the last entry as all have equal distance
        // first work out whether any removal needs to occur
        int numNeighboursToRemove = numNeighbours - k;
        // for each neighbour to trim
        Map<Integer, Integer> furthestNeighbours = neighbours.lastEntry().getValue();
        for(int index = 0; index < numNeighboursToRemove; index++) {
            // randomly remove 1 of the last neighbours
            List<Integer> keys = new ArrayList<>(furthestNeighbours.keySet());
            int randomIndex = random.nextInt(keys.size());
            Integer randomKey = keys.get(randomIndex);
            Integer classCount = furthestNeighbours.get(randomKey);
            classCount--;
            if(classCount <= 0) {
                furthestNeighbours.remove(randomKey);
            } else {
                furthestNeighbours.put(randomKey, classCount);
            }
        }
        return neighbours;
    }

    @Override
    public double[] distributionForInstance(Instance testInstance) throws Exception {
        // majority vote // todo k voting scheme
        double[] distribution = new double[testInstance.numClasses()];
        if(trainInstances.size() == 0) {
            distribution[random.nextInt(distribution.length)]++;
        } else {
            TreeMap<Double, Map<Integer, Integer>> neighbours = findNeighbours(testInstance);
            for(Map<Integer, Integer> neighbourMap : neighbours.values()) {
                for(Integer classValue : neighbourMap.keySet()) {
                    distribution[classValue] += neighbourMap.get(classValue);
                }
            }
        }
        ArrayUtilities.normalise(distribution);
        return distribution;
    }

    public ClassifierResults predict(Instances testInstances) throws Exception {
        ClassifierResults results = new ClassifierResults();
        results.setNumClasses(testInstances.numClasses());
        results.setNumInstances(testInstances.numInstances());
        results.setTrainTime(trainTime);
        long startTime = System.nanoTime();
        for(Instance testInstance : testInstances) {
            results.storeSingleResult(testInstance.classValue(), distributionForInstance(testInstance));
        }
        long stopTime = System.nanoTime();
        results.setName(toString());
        results.setParas(getParameters());
        results.setTrainTime(trainTime);
        results.setTestTime(stopTime - startTime);
        return results;
    }

    @Override
    public Capabilities getCapabilities() {
        return null;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    @Override
    public void setSeed(long seed) {
        random.setSeed(seed);
        samplingRandom.setSeed(seed);
        this.seed = seed;
    }

    public double[][] distributionForInstances(final Instances testInstances) throws Exception {
        double[][] distributions = new double[testInstances.numInstances()][];
        for(int i = 0; i < distributions.length; i++) {
            distributions[i] = new double[testInstances.numClasses()];
        }
        for(int i = 0; i < distributions.length; i++) {
            Instance testInstance = testInstances.get(i);
            distributions[i] = distributionForInstance(testInstance);
        }
        return distributions;
    }

    @Override
    public String getParameters() {
        return "samplePercentage=" + samplePercentage + ",k=" + k + ",stratifiedSample=" + stratifiedSample + ",distanceMeasure=" + distanceMeasure.toString() + ",distanceMeasureParameters={" + distanceMeasure.getParameters() + "}";
    }
}
