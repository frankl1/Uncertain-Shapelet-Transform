package timeseriesweka.classifiers;

import timeseriesweka.classifiers.ee.iteration.*;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;
import utilities.*;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
        private final AbstractIndexIterator indexIterator = new RandomIndexIterator();// todo set seed
        // todo reset func
        private Box<Long> trainDuration = new Box<>(0L);
        private Box<Long> testDuration = new Box<>(0L);

        public void add(Instance instance) {
            time(() -> {
                indexIterator.add(searchers.size());
                searchers.add(new Searcher(instance));
            }, trainDuration);
        }

        public void testTick() {
            time(() -> {
                Searcher searcher = searchers.get(indexIterator.next());
                searcher.tick();
                if(!searcher.remainingTicks()) {
                    indexIterator.remove();
                }
            }, testDuration);
        }

        public boolean remainingTestTicks() {
            return time(() -> {
                boolean result = indexIterator.hasNext();
                return result;
            }, testDuration);
        }

        public void addInstanceIndex(final int index) {
            time(() -> {
                for(Searcher searcher : searchers) {
                    searcher.addInstanceIndex(index);
                }
            }, trainDuration);
        }

        public ClassifierResults predict() {
            Box<Long> predictionDuration = new Box<>(0L);
            ClassifierResults results = new ClassifierResults();
            time(() -> {
                for (Searcher searcher : searchers) {
                    results.storeSingleResult(searcher.classValue(), searcher.predict());
                }
            }, predictionDuration);
            results.setNumInstances(searchers.size());
            results.setNumClasses(originalTrainInstances.numClasses());
            results.setTrainTime(trainDuration.get());
            results.setTestTime(testDuration.get() + predictionDuration.get());
            return results;
        }

//        public int size() {
//            return time(searchers::size, trainDuration);
//        }
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

        private Instance instance;
        private int numNeighbours = 0;
        private int noNeighbourPrediction;
        private final RandomIndexIterator uncheckedInstanceIndexIterator = new RandomIndexIterator();

        public Searcher(Instance instance) {
            uncheckedInstanceIndexIterator.setRandom(samplingRandom);
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
            Instance trainInstance = originalTrainInstances.get(trainInstanceIndex);
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

    }

    private Instances originalTrainInstances;
    private Instances originalTestInstances;
    private Searchers trainSearchers = new Searchers();
    private Searchers testSearchers = new Searchers();
    private List<List<Integer>> instanceIndicesByClass;
    private List<Double> classDistribution;
    private final TreeMap<Integer, Double> classSamplingProbabilities = new TreeMap<>();
    private Random samplingRandom = new Random();
    private boolean hasSampledTrainInstance = true;

    public boolean hasSampledTrainInstance() {
        return hasSampledTrainInstance;
    }

    private static <A> A time(Supplier<A> function, Box<Long> box) {
        long timeStamp = System.nanoTime();
        A result = function.get();
        long duration = System.nanoTime() - timeStamp;
        box.setContents(duration + box.getContents());
        return result;
    }

    private static void time(Runnable function, Box<Long> box) {
        long timeStamp = System.nanoTime();
        function.run();
        long duration = System.nanoTime() - timeStamp;
        box.setContents(duration + box.getContents());
    }

    public boolean remainingTrainTicks() {
        return time(trainSearchers::remainingTestTicks, trainDuration);
    }

    public boolean remainingTestTicks() {
        return time(testSearchers::remainingTestTicks, testDuration);
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
        time(() -> {
            trainSearchers.testTick();
            if(!trainSearchers.remainingTestTicks() && hasRemainingSampling()) {
                addNextTrainInstance();
            } else {
                hasSampledTrainInstance = false;
            }
        }, trainDuration);

    }

    public void setTrainInstances(Instances trainInstances) {
        trainDuration.setContents(0L);
        time(() -> {
            originalTrainInstances = trainInstances;
            instanceIndicesByClass = Utilities.instanceIndicesByClass(trainInstances);
            classDistribution = new ArrayList<>();
            for(int i = 0; i < instanceIndicesByClass.size(); i++) {
                double proportion = (double) instanceIndicesByClass.get(i).size() / trainInstances.numInstances();
                classDistribution.add(proportion);
                classSamplingProbabilities.put(i, proportion);
            }
        }, trainDuration);
    }

    public void setTestInstances(Instances testInstances) {
        testDuration.setContents(0L);
        time(() -> {
            originalTestInstances = testInstances;
            for(Instance testInstance : testInstances) {
                testSearchers.add(testInstance);
            }
            addNextTrainInstance();
        }, testDuration);
    }

    private boolean hasRemainingSampling() {
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
        Integer sampleClass = sampleClasses.get(samplingRandom.nextInt(sampleClasses.size()));
        classSamplingProbabilities.put(sampleClass, maxClassProbability - 1);
        List<Integer> homogeneousInstancesIndices = instanceIndicesByClass.get(sampleClass);
        int sampledInstanceIndex = homogeneousInstancesIndices.remove(samplingRandom.nextInt(homogeneousInstancesIndices.size()));
        if(homogeneousInstancesIndices.isEmpty()) {
            classSamplingProbabilities.remove(sampleClass);
        }
        return sampledInstanceIndex;
    }

    private void addNextTrainInstance() {
        int instanceIndex = sampleTrainInstanceIndex();
        trainSearchers.add(originalTrainInstances.get(instanceIndex));
        trainSearchers.addInstanceIndex(instanceIndex);
        testSearchers.addInstanceIndex(instanceIndex);
        hasSampledTrainInstance = true;
    }

    public void testTick() {
        time(testSearchers::testTick, testDuration);
    }

    public void reset() {
        setSeed(seed);
        setTrainInstances(originalTrainInstances);
        setTestInstances(originalTestInstances);
    }

    public ClassifierResults predictTrain() {
        return trainSearchers.predict();
        // todo make distance measures not copy the array
    }

    public ClassifierResults predictTest() {
        return testSearchers.predict();
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
    private Box<Long> trainDuration = new Box<>(-1L);
    private Box<Long> testDuration = new Box<>(-1L);

    public long getTrainDuration() {
        return trainDuration + trainSearchers.getDuration();
    }

    public long getTestDuration() {
        return testDuration + testSearchers.getDuration();
    }

    @Override
    public void buildClassifier(Instances trainInstances) throws Exception {
        setTrainInstances(trainInstances);
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
        results.setTrainTime(trainDuration);
        long startTime = System.nanoTime();
        for(Instance testInstance : testInstances) {
            results.storeSingleResult(testInstance.classValue(), distributionForInstance(testInstance));
        }
        long stopTime = System.nanoTime();
        results.setName(toString());
        results.setParas(getParameters());
        results.setTrainTime(trainDuration);
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
        return "k=" + k + ",distanceMeasure=" + distanceMeasure.toString() + ",distanceMeasureParameters={" + distanceMeasure.getParameters() + "}";
    }
}
