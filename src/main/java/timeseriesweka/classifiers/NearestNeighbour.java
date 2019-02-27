package timeseriesweka.classifiers;

import net.sourceforge.sizeof.SizeOf;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;
import utilities.*;
import weka.classifiers.AbstractClassifier;
import weka.core.Instance;
import weka.core.Instances;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class NearestNeighbour extends AbstractClassifier implements Serializable, Reproducible, SaveParameterInfo, CompressedCheckpointClassifier, ContractClassifier {

    // todo implement checkpointing and contracting

    public static final NeighbourWeighter WEIGHT_BY_DISTANCE = distance -> 1 / (1 + distance);
    public static final NeighbourWeighter WEIGHT_UNIFORM = distance -> 1;
    private static final String CHECKPOINT_FILE_NAME = "checkpoint.ser.gzip";
    private String checkpointFilePath;
    private Instances originalTrainInstances = null;
    private double sampleSizePercentage = 1;
    private Instances sampledTrainInstances;
    private Random random = new Random();
    private double[] classSamplingProbabilities;
    private double[] classDistribution;
    private Instances[] instancesByClass; // class value by instances of that class
    private List<NearestNeighbourFinder> trainNearestNeighbourFinders;
    private double kPercentage = 0;
    private long trainDuration = 0;
    private long testDuration = 0;
    private long predictDuration = 0;
    private boolean cvTrain = false;
    private boolean useRandomTieBreak = false;
    private Instances originalTestInstances;
    private List<NearestNeighbourFinder> testNearestNeighbourFinders;
    private DistanceMeasure distanceMeasure;
    private boolean useEarlyAbandon = false;
    private NeighbourWeighter neighbourWeighter = WEIGHT_BY_DISTANCE;
    private long minCheckpointInterval = TimeUnit.NANOSECONDS.convert(10, TimeUnit.MINUTES); // todo put this in the checkpoint interface
    private long lastCheckpointTimeStamp = System.nanoTime() - minCheckpointInterval;
    private long trainDurationLimit = -1;
    private boolean hasLoadedFromCheckpoint = false;

    public NearestNeighbour() {
        setDistanceMeasure(new Dtw());
    }

    public static void main(String[] args) throws Exception {
        int seed = 3;
        NearestNeighbour nearestNeighbour = new NearestNeighbour();
        nearestNeighbour.setUseRandomTieBreak(false);
        nearestNeighbour.setUseEarlyAbandon(false);
        nearestNeighbour.setSampleSizePercentage(1);
        nearestNeighbour.setCvTrain(true);
        Dtw dtw = new Dtw();
        dtw.setWarpingWindow(0);
        nearestNeighbour.setDistanceMeasure(dtw);
        nearestNeighbour.setNeighbourWeighter(WEIGHT_UNIFORM);
        nearestNeighbour.setSeed(seed);
        String datasetName = "GunPoint";
        String checkpointDirPath = "/scratch/checkpoints/" + datasetName;
        new File(checkpointDirPath).mkdirs();
//        nearestNeighbour.setSavePath(checkpointDirPath);
        String datasetPath = "/scratch/Datasets/TSCProblems2015/" + datasetName;
        Instances[] split = Utilities.loadSplitInstances(new File(datasetPath));
        Instances trainInstances = split[0];
        Instances testInstances = split[1];
        split = InstanceTools.resampleTrainAndTestInstances(trainInstances, testInstances, seed);
        trainInstances = split[0];
        testInstances = split[0];
        ClassifierResults trainResults = nearestNeighbour.getTrainPrediction(trainInstances);
        trainResults.findAllStatsOnce();
        ClassifierResults testResults = nearestNeighbour.getTestPrediction(testInstances);
        testResults.findAllStatsOnce();
        System.out.println(trainResults.acc);
        System.out.println(testResults.acc);
    }

    public void setUseRandomTieBreak(final boolean useRandomTieBreak) {
        this.useRandomTieBreak = useRandomTieBreak;
    }

    public void setUseEarlyAbandon(final boolean useEarlyAbandon) {
        this.useEarlyAbandon = useEarlyAbandon;
    }

    public void setSeed(long seed) {
        random.setSeed(seed);
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    public ClassifierResults getTrainPrediction(Instances trainInstances) throws Exception {
        buildClassifier(trainInstances);
        ClassifierResults results = new ClassifierResults();
        results.setNumClasses(originalTrainInstances.numClasses());
        results.setNumInstances(trainNearestNeighbourFinders.size());
        for (NearestNeighbourFinder nearestNeighbourFinder : trainNearestNeighbourFinders) {
            results.storeSingleResult(nearestNeighbourFinder.getInstance().classValue(), nearestNeighbourFinder.predict());
        }
        try {
            results.memory = SizeOf.deepSizeOf(this);
        } catch (Exception e) {

        }
        results.setName(toString());
        results.setParas(getParameters());
        results.setTrainTime(getTrainDuration());
        results.setTestTime(-1);
        return results;
    }

    public ClassifierResults getTestPrediction(Instances testInstances) throws Exception {
        double[][] predictions = distributionForInstances(testInstances);
        ClassifierResults results = new ClassifierResults();
        results.setNumClasses(testInstances.numClasses());
        results.setNumInstances(testInstances.numInstances());
        for (int i = 0; i < predictions.length; i++) {
            results.storeSingleResult(testInstances.get(i).classValue(), predictions[i]);
        }
        try {
            results.memory = SizeOf.deepSizeOf(this);
        } catch (Exception e) {

        }
        results.setName(toString());
        results.setParas(getParameters());
        results.setTrainTime(getTrainDuration());
        results.setTestTime(getTestDuration());
        return results;
    }

    @Override
    public void buildClassifier(final Instances trainInstances) throws Exception {
        resumeFromCheckpoint(); // todo handle checkpoint for specific datasets better
        long timeStamp = System.nanoTime();
        if (!trainInstances.equals(this.originalTrainInstances)) {
            // reset as train dataset has change
            this.originalTrainInstances = trainInstances;
            this.sampledTrainInstances = new Instances(trainInstances, 0);
            instancesByClass = Utilities.instancesByClass(trainInstances);
            classSamplingProbabilities = new double[trainInstances.numClasses()];
            for (int i = 0; i < classSamplingProbabilities.length; i++) {
                classSamplingProbabilities[i] = (double) instancesByClass[i].numInstances() / trainInstances.numInstances();
            }
            classDistribution = Utilities.classDistribution(trainInstances);
            trainNearestNeighbourFinders = new ArrayList<>();
            trainDuration = 0;
            testDuration = 0;
            trainDuration += System.nanoTime() - timeStamp;
            checkpoint();
            timeStamp = System.nanoTime();
        }
        int sampleSize = getSampleSize();
        while (sampledTrainInstances.numInstances() < sampleSize && withinContractTrainTime()) {
            Instance sampledInstance = sampleTrainInstance();
            sampledTrainInstances.add(sampledInstance);
            if (cvTrain) {
                NearestNeighbourFinder newNearestNeighbourFinder = new NearestNeighbourFinder(sampledInstance);
                for(NearestNeighbourFinder nearestNeighbourFinder : trainNearestNeighbourFinders) {
                    double distance = nearestNeighbourFinder.addNeighbour(sampledInstance);
                    newNearestNeighbourFinder.addNeighbour(nearestNeighbourFinder.getInstance(), distance);
                }
                trainNearestNeighbourFinders.add(newNearestNeighbourFinder);
            }
            trainDuration += System.nanoTime() - timeStamp;
            checkpoint();
            timeStamp = System.nanoTime();
        }
        trainDuration += System.nanoTime() - timeStamp;
        checkpoint(); // todo round robin sampling vs stratified vs random
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public String getParameters() {
        return null; // todo delegate to getOptions
    }

    // todo classifierResults train time and productive train time to distinguish between timings with / without non-classifier stuff, e.g. file writing

    public long getTrainDuration() {
        return trainDuration;
    }

    public long getTestDuration() {
        return testDuration + predictDuration;
    }

    private int getSampleSize() {
        return (int) (sampleSizePercentage * originalTrainInstances.numInstances());
    }

    /**
     * can we iterate through another neighbour sample within the train time contract?
     * @return true if can do another iteration of looking for neighbours
     */
    private boolean withinContractTrainTime() {
        return true; // todo test if within contract
    }

    private Instance sampleTrainInstance() {
        int sampleClass = (int) findSampleClass();
        Instances homogeneousInstances = instancesByClass[sampleClass]; // instances of the class value
        Instance sampledInstance = homogeneousInstances.remove(random.nextInt(homogeneousInstances.numInstances()));
        classSamplingProbabilities[sampleClass]--;
        ArrayUtilities.add(classSamplingProbabilities, classDistribution);
        return sampledInstance;
    }

    private double findSampleClass() {
        int[] highestProbabilityClasses = Utilities.argMax(classSamplingProbabilities);
        if(highestProbabilityClasses.length > 1) {
            return highestProbabilityClasses[random.nextInt(highestProbabilityClasses.length)];
        } else {
            return highestProbabilityClasses[0];
        }
    }

    public double getSampleSizePercentage() {
        return sampleSizePercentage;
    }

    public void setSampleSizePercentage(final double percentage) {
        Utilities.percentageCheck(percentage);
        this.sampleSizePercentage = percentage;
    }

    private int getK() {
        return 1 + (int) kPercentage * originalTrainInstances.numInstances();
    }

    public double getKPercentage() {
        return kPercentage;
    }

    // todo load from checkpoint

    public void setKPercentage(final double percentage) {
        Utilities.percentageCheck(percentage);
        this.kPercentage = percentage;
    }

    public boolean isCvTrain() {
        return cvTrain;
    }

    public void setCvTrain(final boolean cvTrain) {
        this.cvTrain = cvTrain;
    }

    public boolean usesRandomTieBreak() {
        return useRandomTieBreak;
    }

    @Override
    public double classifyInstance(final Instance instance) throws Exception {
        double[] prediction = distributionForInstance(instance);
        int[] maxIndices = Utilities.argMax(prediction);
        if (useRandomTieBreak) {
            return prediction[maxIndices[random.nextInt(maxIndices.length)]];
        } else {
            return prediction[maxIndices[0]];
        }
    }

    public double[][] distributionForInstances(final Instances testInstances) throws Exception {
        resumeFromCheckpoint();
        long timeStamp = System.nanoTime();
        if (!testInstances.equals(originalTestInstances)) {
            originalTestInstances = testInstances;
            testNearestNeighbourFinders = new ArrayList<>();
            for (Instance testInstance : testInstances) {
                testNearestNeighbourFinders.add(new NearestNeighbourFinder(testInstance));
            }
            testDuration = 0;
            testDuration += System.nanoTime() - timeStamp;
            checkpoint();
            timeStamp = System.nanoTime();
        }
        while (!sampledTrainInstances.isEmpty()) {
            Instance sampledTrainInstance = sampledTrainInstances.remove(random.nextInt(sampledTrainInstances.numInstances()));
            for (NearestNeighbourFinder testNearestNeighbourFinder : testNearestNeighbourFinders) {
                testNearestNeighbourFinder.addNeighbour(sampledTrainInstance);
            }
            testDuration += System.nanoTime() - timeStamp;
            checkpoint();
            timeStamp = System.nanoTime();
        }
        testDuration += System.nanoTime() - timeStamp;
        timeStamp = System.nanoTime();
        double[][] predictions = new double[testNearestNeighbourFinders.size()][];
        for (int i = 0; i < predictions.length; i++) {
            predictions[i] = testNearestNeighbourFinders.get(i).predict();
        }
        predictDuration = System.nanoTime() - timeStamp;
        return predictions;
    }

    private void resumeFromCheckpoint() throws Exception {
        if(!hasLoadedFromCheckpoint && checkpointFilePath != null && new File(checkpointFilePath).exists()) {
            loadFromFile(checkpointFilePath);
        }
    }

    private void checkpoint() throws IOException {
        if(checkpointFilePath != null && System.nanoTime() - lastCheckpointTimeStamp > minCheckpointInterval) {
            saveToFile(checkpointFilePath);
            lastCheckpointTimeStamp = System.nanoTime();
        }
    }

    @Override
    public double[] distributionForInstance(final Instance testInstance) throws Exception {
        return distributionForInstances(Utilities.instanceToInstances(testInstance))[0];
    }

    public DistanceMeasure getDistanceMeasure() {
        return distanceMeasure;
    }

    // todo getOptions setOptions

    public void setDistanceMeasure(final DistanceMeasure distanceMeasure) {
        this.distanceMeasure = distanceMeasure;
    }

    public boolean usesEarlyAbandon() {
        return useEarlyAbandon;
    }

    @Override
    public void setSavePath(String path) {
        path = Utilities.asDirectoryPath(path);
        this.checkpointFilePath = path + CHECKPOINT_FILE_NAME;
    }

    @Override
    public void copyFromSerObject(final Object obj) throws Exception {
        NearestNeighbour other = (NearestNeighbour) obj;
        originalTestInstances = other.originalTestInstances;
        originalTrainInstances = other.originalTrainInstances;
        sampledTrainInstances = other.sampledTrainInstances;
        random = other.random;
        classSamplingProbabilities = other.classSamplingProbabilities;
        classDistribution = other.classDistribution;
        instancesByClass = other.instancesByClass;
        trainNearestNeighbourFinders = other.trainNearestNeighbourFinders;
        kPercentage = other.kPercentage;
        trainDuration = other.trainDuration;
        testDuration = other.testDuration;
        predictDuration = other.predictDuration;
        cvTrain = other.cvTrain;
        useRandomTieBreak = other.useRandomTieBreak;
        testNearestNeighbourFinders = other.testNearestNeighbourFinders;
        distanceMeasure = other.distanceMeasure;
        useEarlyAbandon = other.useEarlyAbandon;
        neighbourWeighter = other.neighbourWeighter;
        hasLoadedFromCheckpoint = true;
    }

    public NeighbourWeighter getNeighbourWeighter() {
        return neighbourWeighter;
    }

    public void setNeighbourWeighter(final NeighbourWeighter neighbourWeighter) {
        this.neighbourWeighter = neighbourWeighter;
    }

    @Override
    public void setTimeLimit(final long time) { // todo split to train time limit and test time limit
        trainDurationLimit = time;
    }

    public interface NeighbourWeighter extends Serializable {
        double weight(double distance);
    }

    private class NearestNeighbourFinder implements Serializable {
        private Instance instance;
        private TreeMap<Double, List<Instance>> nearestNeighbours = new TreeMap<>();
        private int neighbourCount = 0;

        public NearestNeighbourFinder(Instance instance) {
            this.instance = instance;
        }

        public Instance getInstance() {
            return instance;
        }

        public double addNeighbour(Instance neighbour) {
            double distance = distanceMeasure.distance(instance, neighbour, findCutOff());
            addNeighbour(neighbour, distance);
            return distance;
        }

        private double findCutOff() {
            if (useEarlyAbandon) {
                return nearestNeighbours.lastKey();
            } else {
                return Double.POSITIVE_INFINITY;
            }
        }

        public void addNeighbour(Instance neighbour, double distance) {
            int k = getK();
            List<Instance> neighbours = nearestNeighbours.computeIfAbsent(distance, key -> new ArrayList<>());
            neighbours.add(neighbour);
            neighbourCount++;
            if(neighbourCount > k) {
                Map.Entry<Double, List<Instance>> furthestNeighboursEntry = nearestNeighbours.lastEntry();
                if (neighbourCount - k >= furthestNeighboursEntry.getValue().size()) {
                    neighbourCount -= nearestNeighbours.pollLastEntry().getValue().size();
                }
            }
        }

        public double[] predict() {
            double[] predictions = new double[instance.numClasses()];
            Set<Double> distancesKeySet = nearestNeighbours.keySet();
            Iterator<Double> distancesIterator = distancesKeySet.iterator();
            int i = 0;
            if(!distancesIterator.hasNext()) {
                predictions[random.nextInt(predictions.length)]++;
                return predictions;
            }
            int neighbourCount = 0;
            int k = getK();
            Double distance;
            while (i < distancesKeySet.size() - 1) {
                i++;
                distance = distancesIterator.next();
                List<Instance> neighbours = this.nearestNeighbours.get(distance);
                for (Instance neighbour : neighbours) {
                    predictions[(int) neighbour.classValue()] += neighbourWeighter.weight(distance);
                    neighbourCount++;
                }
            }
            distance = distancesIterator.next(); // todo only need to trim if numNeighbours > k
            List<Instance> neighbours = new ArrayList<>(this.nearestNeighbours.get(distance));
            while (neighbourCount < k) {
                Instance neighbour = neighbours.remove(random.nextInt(neighbours.size()));
                predictions[(int) neighbour.classValue()] += neighbourWeighter.weight(distance);
                neighbourCount++;
            }
            ArrayUtilities.normalise(predictions);
            return predictions;
        }
    }
}
