package timeseriesweka.classifiers.nearest_neighbour;

import timeseriesweka.classifiers.AdvancedClassifier;
import timeseriesweka.classifiers.Tickable;
import timeseriesweka.classifiers.ee.iteration.*;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;
import utilities.*;
import utilities.range.Range;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

import static utilities.Utilities.time;

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

    private class Searchers {
        private final List<Searcher> searchers = new ArrayList<>();
        private final AbstractIndexIterator indexIterator = new RoundRobinIndexIterator();
        // todo reset func

        public void add(Instance instance) {
            Searcher searcher = new Searcher(instance);
            int numSampledTrainInstances = sampledTrainInstanceIndices.size();
            if(numSampledTrainInstances > 0) {
                indexIterator.add(searchers.size());
            }
            for(int i = 0; i < numSampledTrainInstances; i++) {
                searcher.addInstanceIndex(sampledTrainInstanceIndices.get(i));
            }
            searchers.add(searcher);
        }

        public void testTick() {
            Searcher searcher = searchers.get(indexIterator.next());
            searcher.tick();
            if(!searcher.remainingTicks()) {
                indexIterator.remove();
            }
        }

        public boolean remainingTestTicks() {
            return indexIterator.hasNext();
        }

        public void addInstanceIndex(final int index) {
            for(int i = 0; i < searchers.size(); i++) {
                Searcher searcher = searchers.get(i);
                searcher.addInstanceIndex(index);
                indexIterator.add(i);
            }
        }

        public double[][] predict() {
            double[][] predictions = new double[searchers.size()][];
            for(int i = 0; i < predictions.length; i++) {
                predictions[i] = searchers.get(i).predict();
            }
            return predictions;
        }
    }

    private class Searcher {
        private final TreeMap<Double, TreeMap<Double, Integer>> neighbourClusters = new TreeMap<>();

        public void setInstance(final Instance instance) {
            this.instance = instance;
            noNeighbourPrediction = random.nextInt(instance.numClasses());
            reset();
        }

        public double classValue() {
            return instance.classValue();
        }

        // todo make sure randomness is reproducible

        private Instance instance;
        private int numNeighbours = 0;
        private int noNeighbourPrediction;
        private final RandomIndexIterator uncheckedInstanceIndexIterator = new RandomIndexIterator();

        public Searcher(Instance instance) {
            uncheckedInstanceIndexIterator.setRandom(random);
            setInstance(instance);
        }

        public void addInstanceIndex(int index) {
            uncheckedInstanceIndexIterator.add(index);
        }

        public boolean remainingTicks() {
            return uncheckedInstanceIndexIterator.hasNext();
        }

        public void tick() {
            int trainInstanceIndex = uncheckedInstanceIndexIterator.next();
            uncheckedInstanceIndexIterator.remove();
            Instance trainInstance = trainInstances.get(trainInstanceIndex);
            double cutOff = getCutOff();
            double distance = distanceMeasure.distance(trainInstance, instance, cutOff);
            if(distance <= cutOff) {
                 addNeighbour(distance, trainInstance.classValue());
            }
        }

        public double[] predict() {
            TreeMap<Double, TreeMap<Double, Integer>> neighbours = findNeighbours();
            double[] probabilities = new double[instance.numClasses()];
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
            uncheckedInstanceIndexIterator.reset();
        }

        public void setSeed(long seed) {
            // todo
        }

    }

    private Instances trainInstances;
    private Instances testInstances;
    private final Range sampledTrainInstanceIndices = new Range();
    private Searchers trainSearchers = new Searchers();
    private Searchers testSearchers = new Searchers();
    private List<List<Integer>> instanceIndicesByClass;
    private List<Double> classDistribution;
    private final TreeMap<Integer, Double> classSamplingProbabilities = new TreeMap<>();
    private boolean willSampleTrain = true;
    private final Box<Long> trainDuration = new Box<>();
    private final Box<Long> testDuration = new Box<>();
    private final Box<Long> trainPredictionDuration = new Box<>(0L);
    private final Box<Long> testPredictionDuration = new Box<>(0L);

    public boolean willSampleTrain() {
        return willSampleTrain;
    }

    public boolean hasNextTrainTick() {
        return trainSearchers.remainingTestTicks() || hasNextSampling();
    }

    public boolean hasNextTestTick() {
        return testSearchers.remainingTestTicks();
    }

    public void train() {
        while (hasNextTrainTick()) {
            trainTick();
        }
    }

    public void test() {
        while (hasNextTestTick()) {
            testTick();
        }
    }

    public void trainTick() {
        time(() -> {
            if(willSampleTrain) {
                addNextTrainInstance();
            } else {
                trainSearchers.testTick();
            }
            if(!trainSearchers.remainingTestTicks()) {
                willSampleTrain = true;
            } else {
                willSampleTrain = false;
            }
        }, trainDuration);

    }

    public void setTrain(Instances trainInstances) {
        trainDuration.setContents(0L);
        time(() -> {
            this.trainInstances = trainInstances;
            instanceIndicesByClass = Utilities.instanceIndicesByClass(trainInstances);
            classDistribution = new ArrayList<>();
            for(int i = 0; i < instanceIndicesByClass.size(); i++) {
                double proportion = (double) instanceIndicesByClass.get(i).size() / trainInstances.numInstances();
                classDistribution.add(proportion);
                classSamplingProbabilities.put(i, proportion);
            }
            willSampleTrain = true;
        }, trainDuration);
    }

    public void setTest(Instances testInstances) {
        testDuration.setContents(0L);
        time(() -> {
            this.testInstances = testInstances;
            for(Instance testInstance : testInstances) {
                testSearchers.add(testInstance);
            }
        }, testDuration);
    }

    private boolean hasNextSampling() {
        return !classSamplingProbabilities.isEmpty();
    }

    private int sampleTrainInstanceIndex() {
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
            classSamplingProbabilities.put(classValue, classProbability + classDistribution.get(classValue));
        }
        Integer sampleClass = sampleClasses.get(random.nextInt(sampleClasses.size()));
        classSamplingProbabilities.put(sampleClass, classSamplingProbabilities.get(sampleClass) - 1);
        List<Integer> homogeneousInstancesIndices = instanceIndicesByClass.get(sampleClass);
        int sampledInstanceIndex = homogeneousInstancesIndices.remove(random.nextInt(homogeneousInstancesIndices.size()));
        if(homogeneousInstancesIndices.isEmpty()) {
            classSamplingProbabilities.remove(sampleClass);
        }
        return sampledInstanceIndex;
    }

    private void addNextTrainInstance() {
        int instanceIndex = sampleTrainInstanceIndex();
        trainSearchers.addInstanceIndex(instanceIndex);
        trainSearchers.add(trainInstances.get(instanceIndex));
        sampledTrainInstanceIndices.add(instanceIndex);
        testSearchers.addInstanceIndex(instanceIndex);
    }

    public void testTick() {
        time(testSearchers::testTick, testDuration);
    }

    public void reset() {
        setSeed(seed);
        setTrain(trainInstances);
        setTest(testInstances);
    }

    public double[][] predictTrain() {
        trainPredictionDuration.setContents(0L);
        return Utilities.time(trainSearchers::predict, trainPredictionDuration);
    }

    public double[][] predictTest() {
        testPredictionDuration.setContents(0L);
        return time(testSearchers::predict, testPredictionDuration);
    }

    public static ClassifierResults produceResults(Instances instances, double[][] predictions) {
        ClassifierResults results = new ClassifierResults();
        for(int i = 0; i < predictions.length; i++) {
            results.storeSingleResult(instances.get(i).classValue(), predictions[i]);
        }
        results.setNumInstances(instances.numInstances());
        results.setNumClasses(instances.numClasses());
        return results;
    }

    public ClassifierResults findTrainResults() {
        ClassifierResults results = produceResults(trainInstances, predictTrain());
        results.setTrainTime(getTrainDuration());
        return results;
    }

    public ClassifierResults findTestResults() {
        ClassifierResults results = produceResults(testInstances, predictTest());
        results.setTrainTime(getTestDuration());
        return results;
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

    public long getTrainDuration() {
        return trainDuration.get() + trainPredictionDuration.get();
    }

    public long getTestDuration() {
        return testDuration.get() + testPredictionDuration.get();
    }

    @Override
    public void buildClassifier(Instances trainInstances) throws Exception {
        setTrain(trainInstances);
        train();
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

    @Override
    public double[] distributionForInstance(Instance testInstance) throws Exception {// todo use ticks
        // majority vote // todo k voting scheme
        Instances testInstances = new Instances(testInstance.dataset(), 0);
        testInstances.add(testInstance);
        setTest(testInstances);
        double[][] predictions = predictTest();
        return predictions[0];
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
        this.seed = seed;
    }

    @Override
    public String getParameters() {
        return "k=" + k + ",distanceMeasure=" + distanceMeasure.toString() + ",distanceMeasureParameters={" + distanceMeasure.getParameters() + "}";
    }
}
