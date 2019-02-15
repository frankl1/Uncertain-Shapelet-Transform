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

public class NearestNeighbour implements AdvancedClassifier, Tickable {

    @Override
    public void setSavePath(String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void copyFromSerObject(Object obj) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTimeLimit(long time) {
    throw new UnsupportedOperationException();

    }

    private class NeighbourSearcher {
        private final TreeMap<Double, TreeMap<Double, Integer>> neighbourClusters = new TreeMap<>();

        public void setTestInstance(final Instance testInstance) {
            this.testInstance = testInstance;
            noNeighbourPrediction = random.nextInt(testInstance.numClasses());
            reset();
        }

        private Instance testInstance;
        private int numNeighbours = 0;
        private int noNeighbourPrediction;
        private final RandomIndexIterator trainInstanceIndexIterator = new RandomIndexIterator();

        public NeighbourSearcher(Instance testInstance) {
            trainInstanceIndexIterator.setRandom(samplingRandom);
            setTestInstance(testInstance);
        }

        public void addInstanceIndex(int index) {
            trainInstanceIndexIterator.add(index);
        }

        public boolean remainingTicks() {
            return trainInstanceIndexIterator.hasNext();
        }

        public void tick() {
            int trainInstanceIndex = trainInstanceIndexIterator.next();
            trainInstanceIndexIterator.remove();
            Instance trainInstance = trainInstances.get(trainInstanceIndex);
            double cutOff = getCutOff();
            double distance = distanceMeasure.distance(trainInstance, testInstance, cutOff);
            if(distance <= cutOff) {
                 addNeighbour(distance, trainInstance.classValue());
            }
        }

        public double[] predict() {
            TreeMap<Double, TreeMap<Double, Integer>> neighbours = findNeighbours();
            double[] probabilities = new double[testInstance.numClasses()];
            if(neighbours.isEmpty()) {
                probabilities[noNeighbourPrediction]++;
            } else {
                for(Double distance : neighbours.keySet()) {
                    TreeMap<Double, Integer> neighbourCluster = neighbours.get(distance);
                    for(Double classValue : neighbourCluster.keySet()) {
                        probabilities[classValue.intValue()] += neighbourCluster.get(classValue);
                    }
                }
                ArrayUtilities.normalise(probabilities);
            }
            return probabilities;
        }

        private TreeMap<Double, TreeMap<Double, Integer>> findNeighbours() {
            TreeMap<Double, TreeMap<Double, Integer>> neighbours = new TreeMap<>();
            if(neighbourClusters.isEmpty()) {
                return neighbours;
            }
            for(Double key : neighbourClusters.keySet()) {
                neighbours.put(key, new TreeMap<>(neighbourClusters.get(key)));
            }
            int numNeighboursToRemove = numNeighbours - k;
            TreeMap<Double, Integer> lastCluster = neighbours.lastEntry().getValue();
            List<Double> lastClusterClassValues = new ArrayList<>(lastCluster.keySet());
            while (numNeighboursToRemove > 0) {
                numNeighboursToRemove--;
                Double randomClassValue = lastClusterClassValues.remove(random.nextInt(lastClusterClassValues.size()));
                Integer count = lastCluster.get(randomClassValue);
                count--;
                if(count <= 0) {
                    lastCluster.remove(randomClassValue);
                    if(lastCluster.isEmpty()) {
                        neighbours.pollLastEntry();
                    }
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
                count = 1;
            } else {
                count++;
            }
            neighbourCluster.put(classValue, count);
            numNeighbours++;
            int kOverflow = numNeighbours - k;
            if(kOverflow > 0) {
                Map.Entry<Double, TreeMap<Double, Integer>> furthestNeighbourCluster = neighbourClusters.lastEntry();
                int furthestNeighbourClusterSize = furthestNeighbourCluster.getValue().size();
                if(kOverflow >= furthestNeighbourClusterSize) {
                    neighbourClusters.pollLastEntry();
                    numNeighbours -= furthestNeighbourClusterSize;
                }
            }
        }

        private double getCutOff() {
            if(useCutOff && !neighbourClusters.isEmpty()) {
                return neighbourClusters.lastKey();
            }
            return DistanceMeasure.MAX;
        }

        public void reset() {
            neighbourClusters.clear();
            numNeighbours = 0;
            trainInstanceIndexIterator.reset();
        }

    }

    private Instances originalTrainInstances;
    private Instances originalTestInstances;
    private Instances trainInstances;
    private final List<NeighbourSearcher> testNeighbourSearchers = new ArrayList<>();
    private final List<NeighbourSearcher> trainNeighbourSearchers = new ArrayList<>();
    private AbstractIndexIterator testNeighbourSearcherIndexIterator = new RoundRobinIndexIterator();
    private AbstractIndexIterator trainNeighbourSearcherIndexIterator = new RoundRobinIndexIterator();
    private Instances[] instancesByClass;
    private double[] classDistribution;
    private final TreeMap<Integer, Double> classSamplingProbabilities = new TreeMap<>();
    private Random samplingRandom = new Random();
    private boolean selectedNextTrainInstance = true;
    private int numTrainTicks = 0;

    public int getNumTrainTicks() {
        return numTrainTicks;
    }

    public int getNumTestTicks() {
        return numTestTicks;
    }

    private int numTestTicks = 0;

    public boolean hasSelectedNewTrainInstance() {
        return selectedNextTrainInstance;
    }

    public boolean remainingTrainTicks() {
        return false;
    }

    public boolean remainingTestTicks() {
        return hasNextTrainInstance() || testNeighbourSearcherIndexIterator.hasNext();
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

    private void tickSearcher(List<NeighbourSearcher> neighbourSearchers, AbstractIndexIterator iterator) {
        NeighbourSearcher searcher = neighbourSearchers.get(iterator.next());
        searcher.tick();
        if(!searcher.remainingTicks()) {
            iterator.remove();
            if(!iterator.hasNext() && hasNextTrainInstance()) {
                addNextTrainInstance();
                iterator.reset();
            } else {
                selectedNextTrainInstance = false;
            }
        }
    }

    public void trainTick() {
        long time = System.nanoTime();
        numTrainTicks++;
        tickSearcher(trainNeighbourSearchers, trainNeighbourSearcherIndexIterator);
        trainTime += System.nanoTime() - time;
    }

    public void setTrainInstances(Instances trainInstances) {
        trainTime = 0;
        long time = System.nanoTime();
        numTrainTicks = 0;
        originalTrainInstances = trainInstances;
        this.trainInstances = new Instances(trainInstances, 0);
        instancesByClass = Utilities.instancesByClass(trainInstances);
        classDistribution = new double[instancesByClass.length];
        classSamplingProbabilities.clear();
        for(int i = 0; i < classDistribution.length; i++) {
            classDistribution[i] = (double) instancesByClass[i].numInstances() / trainInstances.numInstances();
            classSamplingProbabilities.put(i, classDistribution[i]);
        }
        numTrainTicks++;
        trainTime += System.nanoTime() - time;
    }

    public void setTestInstances(Instances testInstances) {
        testTime = 0;
        long time = System.nanoTime();
        numTestTicks = 0;
        originalTestInstances = testInstances;
        for(Instance testInstance : testInstances) {
            testNeighbourSearchers.add(new NeighbourSearcher(testInstance));
        }
        testNeighbourSearcherIndexIterator.setRange(new Range(0, testInstances.numInstances() - 1));
        addNextTrainInstance();
        numTestTicks++;
        testTime += System.nanoTime() - time;
    }

    private boolean hasNextTrainInstance() {
        return trainInstances.size() < originalTrainInstances.size();
    }

    private Instance sampleTrainInstanceA() {
        double maxClassProbability = Double.NEGATIVE_INFINITY;
        List<Integer> sampleClasses = new ArrayList<>();
        for(Integer classValue : classSamplingProbabilities.keySet()) {
            double classProbability = classSamplingProbabilities.get(classValue);
            if(maxClassProbability <= classProbability) {
                if(maxClassProbability < classProbability) {
                    maxClassProbability = classProbability;
                    sampleClasses.clear();
                }
                sampleClasses.add(classValue);
            }
            classSamplingProbabilities.put(classValue, classProbability + classDistribution[classValue]);
        }
        Integer sampleClass = sampleClasses.get(samplingRandom.nextInt(sampleClasses.size()));
        classSamplingProbabilities.put(sampleClass, maxClassProbability - 1);
        Instances homogeneousInstances = instancesByClass[sampleClass];
        Instance sampledInstance = homogeneousInstances.remove(samplingRandom.nextInt(homogeneousInstances.size()));
        if(homogeneousInstances.isEmpty()) {
            classSamplingProbabilities.remove(sampleClass);
        }
        return sampledInstance;
    }

    private void addNextTrainInstance() {
        Instance sampledTrainInstance = sampleTrainInstanceA();
        trainInstances.add(sampledTrainInstance);
        trainNeighbourSearchers.add(new NeighbourSearcher(sampledTrainInstance));
        int index = trainInstances.size() - 1;
        trainNeighbourSearcherIndexIterator.add(index);
        for(NeighbourSearcher searcher : trainNeighbourSearchers) {
            searcher.addInstanceIndex(index);
        }
        for(NeighbourSearcher searcher : testNeighbourSearchers) {
            searcher.addInstanceIndex(index);
        }
        selectedNextTrainInstance = true;
    }

    public void testTick() {
        long time = System.nanoTime();
        numTestTicks++;
        tickSearcher(testNeighbourSearchers, testNeighbourSearcherIndexIterator);
        testTime += System.nanoTime() - time;
    }

    public void reset() {
        setSeed(seed);
        setTrainInstances(originalTrainInstances);
        setTestInstances(originalTestInstances);
    }

    public double[][] predict() {
        double[][] predictions = new double[testNeighbourSearchers.size()][];
        for(int i = 0; i < predictions.length; i++) {
            predictions[i] = testNeighbourSearchers.get(i).predict();
        }
        return predictions;
    }

    public NearestNeighbour() {

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
    private long trainTime = -1;
    private long testTime = -1;

    public long getTrainTime() {
        return trainTime;
    }

    public long getTestTime() {
        return testTime;
    }

    @Override
    public void buildClassifier(Instances trainInstances) throws Exception {
        trainTime = System.nanoTime();
        setTrainInstances(trainInstances);
        train();
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

    private TreeMap<Double, Map<Integer, Integer>> findNeighbours(Instance testInstance) { // todo this is redundant now - in neighbour searcher
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
    public double[] distributionForInstance(Instance testInstance) throws Exception {// todo use ticks
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

    @Override
    public String getParameters() {
        return "numTrainTicks=" + numTrainTicks + ",numTestTicks=" + numTestTicks + ",k=" + k + ",distanceMeasure=" + distanceMeasure.toString() + ",distanceMeasureParameters={" + distanceMeasure.getParameters() + "}";
    }
}
