package timeseriesweka.classifiers.ee;

import timeseriesweka.classifiers.AdvancedClassifier;
import timeseriesweka.classifiers.Tickable;
import timeseriesweka.classifiers.ee.constituents.Constituent;
import timeseriesweka.classifiers.ee.iteration.AbstractIndexIterator;
import timeseriesweka.classifiers.ee.iteration.RoundRobinIndexIterator;
import utilities.ArrayUtilities;
import utilities.Box;
import utilities.ClassifierResults;
import utilities.Utilities;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

import static timeseriesweka.classifiers.nearest_neighbour.NearestNeighbour.produceResults;

public class Ee implements AdvancedClassifier, Tickable {

    @Override
    public void buildClassifier(final Instances trainInstances) throws Exception {

    }

    @Override
    public double classifyInstance(final Instance testInstance) throws Exception {
        return Utilities.maxIndex(distributionForInstance(testInstance));
    }

    @Override
    public double[] distributionForInstance(final Instance testInstance) throws Exception {
        return null;
    }

    @Override
    public Capabilities getCapabilities() {
        return null;
    }

    @Override
    public void setSeed(final long seed) {
        random.setSeed(seed);
        this.seed = seed;
    }

    @Override
    public void setSavePath(String path) {

    }

    @Override
    public void copyFromSerObject(Object obj) throws Exception {

    }

    @Override
    public void setTimeLimit(long time) {

    }

    @Override
    public String getParameters() {
        return null;
    }

    private Instances trainInstances;
    private Instances testInstances;
    private Box<Long> trainDuration = new Box<>();
    private Box<Long> testDuration = new Box<>();
    private Box<Long> trainPredictionDuration = new Box<>();
    private Box<Long> testPredictionDuration = new Box<>();
    private final List<Constituent> constituents = new ArrayList<>();
    private AbstractIndexIterator constituentIterator = new RoundRobinIndexIterator();
    private Long seed = null;
    private Random random = new Random();
    private FeedbackIterator<TrainedTickable> candidateIterator = null;
    private Selector<TrainedTickable> selector = new BestPerType<>(null, null);
    private boolean refreshCandidates = true;
    private TrainedTickable[] selectedCandidates;
    private AbstractIndexIterator selectedCandidatesIndexIterator = new RoundRobinIndexIterator();

    @Override
    public boolean hasNextTrainTick() {
        return Utilities.time(() -> candidateIterator.hasNext() || constituentIterator.hasNext(), trainDuration);
    }

    @Override
    public void trainTick() {
        Utilities.time(() -> {
            boolean explore = random.nextBoolean(); // todo explore or exploit strategy
            TrainedTickable trainedTickable;
            if(explore) {
                Constituent constituent = constituents.get(constituentIterator.next());
                trainedTickable = new TrainedTickable(constituent.next());
                candidateIterator.add(trainedTickable);
                if(!constituent.hasNext()) {
                    constituentIterator.remove();
                }
            } else {
                trainedTickable = candidateIterator.next();
                Tickable tickable = trainedTickable.getTickable();
                tickable.trainTick();
                trainedTickable.findTrainResults();
                if(tickable.hasNextTrainTick()) {
                    candidateIterator.add(trainedTickable);
                }
            }
            refreshCandidates = selector.add(trainedTickable);
        }, trainDuration);
    }

    @Override
    public void setTrain(final Instances trainInstances) {
        trainDuration.setContents(0L);
        Utilities.time(() -> {
            this.trainInstances = trainInstances;
        }, trainDuration);
    }

    @Override
    public boolean hasNextTestTick() {
        return Utilities.time(() -> refreshCandidates || selectedCandidatesIndexIterator.hasNext(), testDuration);
    }

    @Override
    public void testTick() {
        Utilities.time(() -> {
            if(refreshCandidates) {
                selectedCandidates = selector.getSelected();
                refreshCandidates = false;
                selectedCandidatesIndexIterator.reset();
                for(int i = 0; i < selectedCandidates.length; i++) {
                    selectedCandidatesIndexIterator.add(i);
                }
            } else {
                int index = selectedCandidatesIndexIterator.next();
                TrainedTickable trainedTickable = selectedCandidates[index];
                Tickable tickable = trainedTickable.getTickable();
                tickable.testTick();
                if(!tickable.hasNextTestTick()) {
                    selectedCandidatesIndexIterator.remove();
                }
            }
        }, testDuration);
    }

    @Override
    public void setTest(final Instances testInstances) {
        testDuration.setContents(0L);
        Utilities.time(() -> {
            this.testInstances = testInstances;
        }, testDuration);
    }

    private static double[][] predict(Instances trainInstances, TrainedTickable[] selectedCandidates) {
        double[][] overallPredictions = new double[trainInstances.numInstances()][trainInstances.numClasses()];
        for(int i = 0; i < selectedCandidates.length; i++) {
            double[][] trainPredictions = selectedCandidates[i].getTickable().predictTrain();
            double weighting = selectedCandidates[i].getTrainResults().acc;
            for(int j = 0; j < trainPredictions.length; j++) {
                for(int k = 0; k < trainPredictions.length; k++) {
                    overallPredictions[j][k] += weighting * trainPredictions[j][k];
                }
            }
        }
        for(int i = 0; i < overallPredictions.length; i++) {
            ArrayUtilities.normalise(overallPredictions[i]);
        }
        return overallPredictions;
    }

    @Override
    public double[][] predictTrain() {
        trainPredictionDuration.setContents(0L);
        return Utilities.time(() -> predict(trainInstances, selectedCandidates), trainPredictionDuration);
    }

    @Override
    public double[][] predictTest() {
        testPredictionDuration.setContents(0L);
        return Utilities.time(() -> predict(testInstances, selectedCandidates), testPredictionDuration);
    }

    @Override
    public ClassifierResults findTrainResults() {
        ClassifierResults results = produceResults(trainInstances, predictTrain());
        results.setTrainTime(getTrainDuration());
        return results;
    }

    @Override
    public ClassifierResults findTestResults() {
        ClassifierResults results = produceResults(testInstances, predictTest());
        results.setTrainTime(getTestDuration());
        return results;
    }

    public long getTrainDuration() {
        return trainDuration.get() + trainPredictionDuration.get();
    }

    public long getTestDuration() {
        return testDuration.get() + testPredictionDuration.get();
    }
}
