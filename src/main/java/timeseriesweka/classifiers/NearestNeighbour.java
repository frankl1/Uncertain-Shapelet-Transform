package timeseriesweka.classifiers;

import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;
import utilities.*;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

public class NearestNeighbour implements Classifier {

    public NearestNeighbour() {}

    private Instances trainInstances;
    private int k = 1;

    public DistanceMeasure getDistanceMeasure() {
        return distanceMeasure;
    }

    public void setDistanceMeasure(DistanceMeasure distanceMeasure) {
        this.distanceMeasure = distanceMeasure;
    }

    private DistanceMeasure distanceMeasure = new Dtw();
    private final Random random = new Random(); // generic random for tied breaks, etc
    private final Random samplingRandom = new Random(); // random stream specifically for sampling - ensures sampling in same order
    private Map<Instance, Map<Instance, Double>> distanceCache = null;
    private String cacheName = "";
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

    public boolean isCachingDistances() {
        return distanceCache != null;
    }

    public void cacheDistances(boolean cache) {
        if(cache) {
            distanceCache = new HashMap<>();
        } else {
            distanceCache = null;
        }
    }

    /**
     * only used by getCachedDistance function, do not use elsewhere
     * @param a
     * @param b
     * @return
     */
    private Double getCachedDistanceOrdered(Instance a, Instance b) {
        if(distanceCache == null) {
            return null;
        }
        Map<Instance, Double> map = distanceCache.get(a);
        if(map != null) {
            return map.get(b);
        }
        return null;
    }

    private Double getCachedDistance(Instance a, Instance b) {
        if(distanceCache == null) {
            return null;
        }
        Double distance = getCachedDistanceOrdered(a, b);
        if(distance == null) {
            distance = getCachedDistanceOrdered(b, a);
        }
        return distance;
    }

    private void cacheDistance(Instance a, Instance b, Double distance) {
        if(distanceCache != null) {
            Map<Instance, Double> map = distanceCache.computeIfAbsent(a, key -> new HashMap<>());
            map.put(b, distance);
        }
    }

    @Override
    public void buildClassifier(Instances trainInstances) throws Exception {
        long startTime = System.nanoTime();
        trainInstances = new Instances(trainInstances);
        this.trainInstances = new Instances(trainInstances, 0);
        int overallSampleSize = (int) (trainInstances.numInstances() * samplePercentage);
        if(stratifiedSample) {
            Instances[] instancesByClass = Utilities.instancesByClass(trainInstances);
            for(int i = 0; i < instancesByClass.length; i++) {
                Instances homogeneousInstances = instancesByClass[i];
                int sampleSize = (int) (homogeneousInstances.numInstances() * samplePercentage);
                for(int j = 0; j < sampleSize; j++) {
                    this.trainInstances.add(homogeneousInstances.remove(samplingRandom.nextInt(homogeneousInstances.numInstances())));
                }
            }
            List<Integer> classIndices = new ArrayList<>();
            for(int i = 0; i < instancesByClass.length; i++) {
                classIndices.add(i);
            }
            Collections.shuffle(classIndices, samplingRandom);
            int overflow = overallSampleSize - this.trainInstances.numInstances();
            for(int i = 0; i < overflow; i++) {
                Instances homogeneousInstances = instancesByClass[classIndices.remove(0)];
                this.trainInstances.add(homogeneousInstances.remove(samplingRandom.nextInt(homogeneousInstances.numInstances())));
            }
        } else {
            for(int i = 0; i < overallSampleSize; i++) {
                this.trainInstances.add(trainInstances.remove(samplingRandom.nextInt(trainInstances.numInstances())));
            }
        }
        long stopTime = System.nanoTime();
        trainTime = stopTime - startTime;
    }

    @Override
    public double classifyInstance(Instance testInstance) throws Exception {
        return Utilities.max(distributionForInstance(testInstance));
    }

    private Double findDistance(Instance a, Instance b, Double cutOff) {
//        // find the distance between the train case and test case
//        // check if cached
//        Double cachedDistance = getCachedDistance(a, b); // todo
//        // if distance not in cache then compute and cache
//        if(cachedDistance == null) {
//            cachedDistance = Double.POSITIVE_INFINITY;
//        }
//        if(cachedDistance > cutOff) {
//            double distance = distanceMeasure.distance(a, b, cutOff);
//            if(distance < cachedDistance) {
//                cacheDistance(a, b, cachedDistance);
//            }
//        }
//        return cachedDistance; // todo THIS DOENS'T WORK!
        return distanceMeasure.distance(a, b, cutOff);
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
            Double distance = findDistance(trainInstance, testInstance, cutOff);
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
            int randomIndex = random.nextInt(furthestNeighbours.size());
            Integer classCount = furthestNeighbours.get(randomIndex);
            classCount--;
            if(classCount <= 0) {
                furthestNeighbours.remove(randomIndex);
            } else {
                furthestNeighbours.put(randomIndex, classCount);
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

    private void setCacheName() {
        cacheName = trainInstances.relationName() + "_" + seed;
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
        return "samplePercentage=" + samplePercentage + ",k=" + k + ",stratifiedSample=" + stratifiedSample + ",distanceMeasure=" + distanceMeasure.toString() + ",distanceMeasureParameters=\"" + distanceMeasure.getParameters() + "\"";
    }

    @Override
    public void setSavePath(final String path) {

    }

    @Override
    public void copyFromSerObject(final Object obj) throws Exception {

    }

    @Override
    public void setTimeLimit(final long time) {

    }
}
