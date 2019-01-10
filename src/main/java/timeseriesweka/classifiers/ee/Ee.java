package timeseriesweka.classifiers.ee;

import timeseriesweka.classifiers.CheckpointClassifier;
import timeseriesweka.classifiers.Classifier;
import timeseriesweka.classifiers.ContractClassifier;
import utilities.Reproducible;
import timeseriesweka.classifiers.ee.constituents.*;
import timeseriesweka.classifiers.ee.iteration.AbstractIndexIterator;
import timeseriesweka.classifiers.ee.iteration.ElementIterator;
import timeseriesweka.classifiers.ee.iteration.RoundRobinIndexIterator;
import timeseriesweka.classifiers.ee.index.ElementObtainer;
import timeseriesweka.classifiers.ee.index.LinearInterpolater;
import timeseriesweka.classifiers.ee.range.ValueRange;
import timeseriesweka.classifiers.ee.selection.BestPerTypeSelector;
import timeseriesweka.classifiers.ee.selection.Selector;
import timeseriesweka.classifiers.ee.selection.Weighted;
import utilities.ArrayUtilities;
import utilities.ClassifierResults;
import utilities.Utilities;
import utilities.instances.Experimenter;
import utilities.instances.Folds;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class Ee implements Classifier, Reproducible, CheckpointClassifier, ContractClassifier {

    @Override
    public void copyFromSerObject(final Object obj) throws Exception {
        Ee other = (Ee) obj;
        constituentBuilders.addAll(other.constituentBuilders);
        constituentIndexIterator = other.constituentIndexIterator;
        evaluationMetric = other.evaluationMetric;
        selector = other.selector;
        selectedClassifiers = other.selectedClassifiers;
        seed = other.seed;
        // todo distributed / contract time
    }

    private final List<Function<Instances, Iterator<Classifier>>> constituentBuilders = new LinkedList<>();
    private AbstractIndexIterator constituentIndexIterator = new RoundRobinIndexIterator();
    private Function<ClassifierResults, Double> evaluationMetric = classifierResults -> {
        classifierResults.findAllStatsOnce();
        return classifierResults.acc;
    };
    private Selector<Classifier> selector = new BestPerTypeSelector<>();
    private List<Weighted<Classifier>> selectedClassifiers = new ArrayList<>();
    private Folds folds = null;
    private ElementIterator<Iterator<Classifier>> constituentIterator = null;

    private List<Iterator<Classifier>> buildConstituents(Instances trainInstances) {
        // build constituents
        List<Iterator<Classifier>> constituents = new LinkedList<>();
        for(Function<Instances, Iterator<Classifier>> constituentBuilder : constituentBuilders) {
            Iterator<Classifier> constituent = constituentBuilder.apply(trainInstances);
            constituents.add(constituent);
        }
        return constituents;
    }

    @Override
    public void buildClassifier(Instances trainInstances) throws Exception {
        startContract();
        if(isCheckpointing()) {
            loadFromFile(checkpointPath + CHECKPOINT_FILE_NAME);
        } else {
            selector.reset();
            selector.setSeed(seed);
            // fold train instances
            folds = new Folds.Builder(trainInstances).setSeed(seed).build(); // todo num folds? loocv from Jay's
            // setup constituent iterator
            constituentIterator = new ElementIterator<>();
            // build constituents
            List<Iterator<Classifier>> constituents = new LinkedList<>();
            constituentIterator.setIndexIterator(constituentIndexIterator);
            constituentIterator.setList(constituents);
            constituentIterator.setSeed(seed);
            // iterate through constituent iterators
        }
        while (constituentIterator.hasNext() && (distributed || !contractExceeded())) {
            Iterator<Classifier> classifierIterator = constituentIterator.next();
            // if constituent has no classifiers left
            if(!classifierIterator.hasNext()) {
                // remove constituent
                constituentIterator.remove();
            } else {
                // get next classifier
                Classifier classifier = classifierIterator.next();
                System.out.println(classifier.toString());
                // seed classifier
                classifier.setSeed(seed);
                // set to remaining contract to avoid overrunning
                classifier.setTimeLimit(remainingContract());
                // set checkpointing
                if(isCheckpointing()) {
                    classifier.setSavePath(checkpointPath + classifier.toString() + "/" + classifier.getParameters() + "/");
                }
                // run classifier
                if(isDistributed()) {
                    File dir = new File("abc"); // todo change

                    // todo serialise classifier
                    // serialise fold
                    // record job
                } else {
                    ClassifierResults trainResults = Experimenter.experiment(classifier, folds); // todo distribute
                    // check if classifier makes the cut
                    double stat = evaluationMetric.apply(trainResults);
                    selector.consider(classifier, stat);
                    checkpoint();
                }
            }
        }
        selectedClassifiers.addAll(selector.getSelected());
        folds = null;
        constituentIterator = null;
        checkpoint();
    }

    public static void main(String[] args) {
        Ee ee = new Ee();
        ee.loadClassicConfig();
    }

    private Long seed = null;

    @Override
    public void setSeed(final long seed) {
        this.seed = seed;
    }

    private boolean distributed = false;

    public boolean isDistributed() {
        return distributed;
    }

    public void distribute(boolean distributed) {
        this.distributed = distributed;
    }

    @Override
    public double[] distributionForInstance(final Instance testInstance) throws Exception {
        double[] overallDistribution = new double[testInstance.numClasses()];
        for(Weighted<Classifier> weightedClassifier : selectedClassifiers) {
            double[] distribution = weightedClassifier.getSubject().distributionForInstance(testInstance);
            ArrayUtilities.normalise(distribution);
            ArrayUtilities.multiply(distribution, weightedClassifier.getWeight());
            ArrayUtilities.add(overallDistribution, distribution);
        }
        ArrayUtilities.normalise(overallDistribution);
        return overallDistribution;
    }

    @Override
    public Capabilities getCapabilities() {
        return null;
    }

    @Override
    public double classifyInstance(final Instance testInstance) throws Exception {
        return ArrayUtilities.maxIndex(distributionForInstance(testInstance));
    }

    public List<Function<Instances, Iterator<Classifier>>> getConstituentBuilders() {
        return constituentBuilders;
    }

    public static Function<Instances, Iterator<Classifier>> getDtwConstituentBuilder() {
        return instances -> {
            DtwConstituent dtwConstituent = new DtwConstituent();
            dtwConstituent.getWarpingWindowValueRange().getRange().add(0, 99);
            return dtwConstituent;
        };
    }

    public static Function<Instances, Iterator<Classifier>> getDdtwConstituentBuilder() {
        return instances -> {
            DdtwConstituent ddtwConstituent = new DdtwConstituent();
            ddtwConstituent.getWarpingWindowValueRange().getRange().add(0, 99);
            return ddtwConstituent;
        };
    }

    public static Function<Instances, Iterator<Classifier>> getErpConstituentBuilder() {
        return instances -> {
            double stdDev = ArrayUtilities.populationStandardDeviation(instances);
            ErpConstituent erpConstituent = new ErpConstituent();
            ValueRange<Double> warpingWindowValueRange = erpConstituent.getWarpingWindowValueRange();
            warpingWindowValueRange.setIndexed(new LinearInterpolater(0, 0.25));
            warpingWindowValueRange.getRange().add(0, 9);
            ValueRange<Double> penaltyValueRange = erpConstituent.getPenaltyValueRange();
            penaltyValueRange.setIndexed(new LinearInterpolater(0.2 * stdDev, stdDev));
            penaltyValueRange.getRange().add(0, 9);
            return erpConstituent;
        };
    }

    public static Function<Instances, Iterator<Classifier>> getLcssConstituentBuilder() {
        return instances -> {
            double stdDev = ArrayUtilities.populationStandardDeviation(instances);
            LcssConstituent lcssConstituent = new LcssConstituent();
            ValueRange<Double> warpingWindowValueRange = lcssConstituent.getWarpingWindowValueRange();
            warpingWindowValueRange.setIndexed(new LinearInterpolater(0, (double) (instances.numAttributes() - 1) / 4));
            warpingWindowValueRange.getRange().add(0, 9);
            ValueRange<Double> toleranceValueRange = lcssConstituent.getToleranceValueRange();
            toleranceValueRange.setIndexed(new LinearInterpolater(0.2 * stdDev, stdDev));
            toleranceValueRange.getRange().add(0, 9);
            return lcssConstituent;
        };
    }

    public static Function<Instances, Iterator<Classifier>> getMsmConstituentBuilder() {
        return instances -> {
            MsmConstituent msmConstituent = new MsmConstituent();
            ValueRange<Double> toleranceValueRange = msmConstituent.getCostValueRange();
            Double[] costs = {
                // <editor-fold defaultstate="collapsed" desc="hidden for space">
                0.01,
                0.01375,
                0.0175,
                0.02125,
                0.025,
                0.02875,
                0.0325,
                0.03625,
                0.04,
                0.04375,
                0.0475,
                0.05125,
                0.055,
                0.05875,
                0.0625,
                0.06625,
                0.07,
                0.07375,
                0.0775,
                0.08125,
                0.085,
                0.08875,
                0.0925,
                0.09625,
                0.1,
                0.136,
                0.172,
                0.208,
                0.244,
                0.28,
                0.316,
                0.352,
                0.388,
                0.424,
                0.46,
                0.496,
                0.532,
                0.568,
                0.604,
                0.64,
                0.676,
                0.712,
                0.748,
                0.784,
                0.82,
                0.856,
                0.892,
                0.928,
                0.964,
                1.0,
                1.36,
                1.72,
                2.08,
                2.44,
                2.8,
                3.16,
                3.52,
                3.88,
                4.24,
                4.6,
                4.96,
                5.32,
                5.68,
                6.04,
                6.4,
                6.76,
                7.12,
                7.48,
                7.84,
                8.2,
                8.56,
                8.92,
                9.28,
                9.64,
                10.0,
                13.6,
                17.2,
                20.8,
                24.4,
                28.0,
                31.6,
                35.2,
                38.8,
                42.4,
                46.0,
                49.6,
                53.2,
                56.8,
                60.4,
                64.0,
                67.6,
                71.2,
                74.8,
                78.4,
                82.0,
                85.6,
                89.2,
                92.8,
                96.4,
                100.0// </editor-fold>
            };
            toleranceValueRange.setIndexed(new ElementObtainer<>(Arrays.asList(costs)));
            toleranceValueRange.getRange().add(0, costs.length - 1);
            return msmConstituent;
        };
    }

    public static Function<Instances, Iterator<Classifier>> getTweConstituentBuilder() {
        return instances -> {
            TweConstituent tweConstitent = new TweConstituent();
            ValueRange<Double> warpingWindowValueRange = tweConstitent.getNuValueRange();
            warpingWindowValueRange.setIndexed(new LinearInterpolater(0, 9));
            warpingWindowValueRange.getRange().add(0, 9);
            ValueRange<Double> lambdaValueRange = tweConstitent.getLambdaValueRange();
            lambdaValueRange.setIndexed(new LinearInterpolater(0, 9));
            lambdaValueRange.getRange().add(0, 9);
            return tweConstitent;
        };
    }

    public static Function<Instances, Iterator<Classifier>> getWdtwConstituentBuilder() {
        return instances -> {
            WdtwConstituent wdtwConstitent = new WdtwConstituent();
            ValueRange<Double> warpingWindowValueRange = wdtwConstitent.getWeightValueRange();
            warpingWindowValueRange.setIndexed(new LinearInterpolater(0, 99));
            warpingWindowValueRange.getRange().add(0, 99);
            return wdtwConstitent;
        };
    }

    public static Function<Instances, Iterator<Classifier>> getWddtwConstituentBuilder() {
        return instances -> {
            WddtwConstituent wddtwConstitent = new WddtwConstituent();
            ValueRange<Double> warpingWindowValueRange = wddtwConstitent.getWeightValueRange();
            warpingWindowValueRange.setIndexed(new LinearInterpolater(0, 99));
            warpingWindowValueRange.getRange().add(0, 99);
            return wddtwConstitent;
        };
    }

    public void loadClassicConfig() {
        constituentBuilders.add(getDdtwConstituentBuilder());
        constituentBuilders.add(getDtwConstituentBuilder());
        constituentBuilders.add(getWddtwConstituentBuilder());
        constituentBuilders.add(getWdtwConstituentBuilder());
        constituentBuilders.add(getErpConstituentBuilder());
        constituentBuilders.add(getMsmConstituentBuilder());
        constituentBuilders.add(getLcssConstituentBuilder());
        constituentBuilders.add(getTweConstituentBuilder());
    }

    private String checkpointPath = null;
    private long contractTime = -1;
    private final static String CHECKPOINT_FILE_NAME = "ee.ser";
    private long contractStartTimestamp;
    private long elapsedContractTime;
    private boolean contractPaused = false;
    private long minCheckpointInterval = TimeUnit.NANOSECONDS.convert(1, TimeUnit.HOURS);
    private final static String DISTRIBUTED_DIR_NAME = "parallel";

    private long remainingContract() {
        return contractTime - elapsedContractTime;
    }

    private void resumeContract() {
        contractStartTimestamp = System.nanoTime();
        contractPaused = false;
    }

    private void startContract() {
        elapsedContractTime = 0;
        resumeContract();
    }

    private void lapContract() {
        if(!contractPaused) {
            elapsedContractTime += System.nanoTime() - contractStartTimestamp;
        }
    }

    private void pauseContract() {
        lapContract();
        contractPaused = true;
    }

    private boolean contractExceeded() {
        lapContract();
        return elapsedContractTime < contractTime;
    }

    private boolean isCheckpointing() {
        return checkpointPath != null;
    }

    @Override
    public void setSavePath(final String path) {
        checkpointPath = Utilities.sanitiseFolderPath(path);
    }

    private Long lastCheckpointTimestamp = null;

    private void checkpoint() throws IOException {
        if(lastCheckpointTimestamp == null || System.nanoTime() - lastCheckpointTimestamp > minCheckpointInterval) {
            saveToFile(CHECKPOINT_FILE_NAME);
            lastCheckpointTimestamp = System.nanoTime();
        }
    }

    @Override
    public void setTimeLimit(final long nanoseconds) {
        contractTime = nanoseconds;
    }

    @Override
    public String getParameters() {
        return null;
    }
}
