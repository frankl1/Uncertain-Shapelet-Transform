package ee.ee;

import ee.parameter.ParameterPool;
import ee.selection.BestPerTypeSelector;
import ee.selection.Selector;
import evaluation.evaluators.CrossValidationEvaluator;
import evaluation.evaluators.Evaluator;
import evaluation.storage.ClassifierResults;
import timeseriesweka.classifiers.AdvancedAbstractClassifier.AdvancedAbstractClassifier;
import timeseriesweka.classifiers.CheckpointClassifier;
import timeseriesweka.classifiers.ContractClassifier;
import timeseriesweka.classifiers.Nn.AbstractNn;
import timeseriesweka.classifiers.Nn.Nn;
import timeseriesweka.classifiers.ensembles.EnsembleModule;
import timeseriesweka.classifiers.ensembles.elastic_ensemble.LCSS1NN;
import timeseriesweka.classifiers.ensembles.voting.MajorityVote;
import timeseriesweka.classifiers.ensembles.voting.ModuleVotingScheme;
import timeseriesweka.classifiers.ensembles.weightings.ModuleWeightingScheme;
import timeseriesweka.classifiers.ensembles.weightings.TrainAcc;
import timeseriesweka.measures.DistanceMeasureFactory;
import timeseriesweka.measures.erp.Erp;
import timeseriesweka.measures.lcss.Lcss;
import utilities.*;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static utilities.Utilities.incrementalDiffList;

public class Ee extends AdvancedAbstractClassifier {

    protected final List<ParameterPool> parameterPools = new ArrayList<>();
    private final List<Function<Instances, ParameterPool>> parameterPoolObtainers = new ArrayList<>();
    private Iterator<String[]> parameterPermutationIterator;
    private Selector<EnsembleModule> selector = new BestPerTypeSelector<>(ensembleModule -> {
        if (!(ensembleModule.getClassifier() instanceof AbstractNn)) {
            throw new IllegalArgumentException();
        } else {
            return ((AbstractNn) ensembleModule.getClassifier()).getDistanceMeasure().toString();
        }
    }, Comparator.comparingDouble(ensembleModule -> ensembleModule.trainResults.getAcc()));
    private Function<Ee, Iterator<String[]>> iteratorObtainer = RandomIterator::new;
    private EnsembleModule[] ensembleModules;

    public List<Function<Instances, ParameterPool>> getParameterPoolObtainers() {
        return parameterPoolObtainers;
    }

    public Selector<EnsembleModule> getSelector() {
        return selector;
    }

    public void setSelector(final Selector<EnsembleModule> selector) {
        this.selector = selector;
    }

    public Function<Ee, Iterator<String[]>> getIteratorObtainer() {
        return iteratorObtainer;
    }

    public void setIteratorObtainer(final Function<Ee, Iterator<String[]>> iteratorObtainer) {
        this.iteratorObtainer = iteratorObtainer;
    }

    public ModuleWeightingScheme getWeightingScheme() {
        return weightingScheme;
    }

    public void setWeightingScheme(final ModuleWeightingScheme weightingScheme) {
        this.weightingScheme = weightingScheme;
    }

    public ModuleVotingScheme getVotingScheme() {
        return votingScheme;
    }

    public void setVotingScheme(final ModuleVotingScheme votingScheme) {
        this.votingScheme = votingScheme;
    }

    public Supplier<AbstractClassifier> getClassifierSupplier() {
        return classifierSupplier;
    }

    public void setClassifierSupplier(final Supplier<AbstractClassifier> classifierSupplier) {
        this.classifierSupplier = classifierSupplier;
    }

    public Evaluator getTrainEstimateEvaluator() {
        return trainEstimateEvaluator;
    }

    public void setTrainEstimateEvaluator(final Evaluator trainEstimateEvaluator) {
        this.trainEstimateEvaluator = trainEstimateEvaluator;
    }

    private ModuleWeightingScheme weightingScheme = new TrainAcc();
    private ModuleVotingScheme votingScheme = new MajorityVote();
    private Supplier<AbstractClassifier> classifierSupplier = Nn::new;
    private Evaluator trainEstimateEvaluator = new CrossValidationEvaluator();
    private int count;

    @Override
    protected void resetTrain() throws Exception {
        super.resetTrain();
        parameterPools.clear();
        count = 0;
    }

    @Override
    public void buildClassifier(final Instances trainInstances) throws Exception {
        super.buildClassifier(trainInstances);
        if(resetsOnTrain()) {
            for(Function<Instances, ParameterPool> parameterPoolObtainer : parameterPoolObtainers) {
                parameterPools.add(parameterPoolObtainer.apply(trainInstances));
            }
            parameterPermutationIterator = iteratorObtainer.apply(this);
            selector.setRandom(getTrainRandom());
            selector.clear();
        }
        while (withinTrainContract() && parameterPermutationIterator.hasNext()) {
            String[] parameterPermutation = parameterPermutationIterator.next();
            getLogger().info("Running parameter permutation: " + Utilities.join(parameterPermutation, ", "));
            AbstractClassifier classifier = classifierSupplier.get();
            classifier.setOptions(parameterPermutation);
            if(classifier instanceof ContractClassifier) {
                long remainingTrainTime = getRemainingTrainTime();
                if(remainingTrainTime >= 0) {
                    ((ContractClassifier) classifier).setTimeLimit(remainingTrainTime);
                }
            }
//            if(classifier instanceof CheckpointClassifier) {
//                ((CheckpointClassifier) classifier).setSavePath(new File(getTrainCheckpointDirPath(), String.valueOf(count)).getPath());
//            }
            ClassifierResults trainResults;
            File iterationTrainFile = new File(getTrainCheckpointDirPath(), count + ".csv");
            if(isTrainCheckpointing() && iterationTrainFile.exists()) {
                trainResults = new ClassifierResults();
                trainResults.loadResultsFromFile(iterationTrainFile.getPath());
                String parametersFromFile = trainResults.getParas();
                String parameterPermutationString = Utilities.join(parameterPermutation, ",");
//                if(!parametersFromFile.contains(parameterPermutationString)) {
//                    throw new IllegalStateException("parameter indices don't match");
//                } todo string of vars (kv pairs) contain another string of vars out of order
            } else {
                classifier.buildClassifier(trainInstances);
                if(classifier instanceof TrainAccuracyEstimate) {
                    trainResults = ((TrainAccuracyEstimate) classifier).getTrainResults();
                } else {
                    trainEstimateEvaluator.setSeed(getSeed());
                    trainResults = trainEstimateEvaluator.evaluate(classifier, trainInstances);
                }
                if(isTrainCheckpointing()) {
                    updateTrainTime();
                    trainResults.writeFullResultsToFile(iterationTrainFile.getPath());
                    setTrainTimeStamp(System.nanoTime());
                }
            }
            EnsembleModule ensembleModule = new EnsembleModule(classifier.toString(), classifier, Utilities.join(classifier.getOptions(), ","));
            ensembleModule.trainResults = trainResults;
            selector.considerCandidate(ensembleModule);
            count++;
            updateTrainTime();
        }
        if(resetsOnTrain()) {
            ensembleModules = selector.getSelected().toArray(new EnsembleModule[0]);
            weightingScheme.defineWeightings(ensembleModules, trainInstances.numClasses());
            votingScheme.trainVotingScheme(ensembleModules, trainInstances.numClasses());
            if(isEstimateTrain()) {
                ClassifierResults trainResults = new ClassifierResults();
                setTrainResults(trainResults);
                for(int i = 0; i < trainInstances.numInstances(); i++) {
                    long timeStamp = System.nanoTime();
                    double[] prediction = votingScheme.distributionForTrainInstance(ensembleModules, i);
                    long predictionTime = System.nanoTime() - timeStamp;
                    trainResults.addPrediction(trainInstances.get(i).classValue(), prediction, Utilities.argMax(prediction, getTrainRandom()), predictionTime, null);
                }
            }
            updateTrainTime();
        }
    }

    public static ParameterPool findLcssParameterPool(Instances instances) {
        ParameterPool parameterPool = new ParameterPool();
        parameterPool.add(Nn.DISTANCE_MEASURE_KEY, new ArrayList<>(Arrays.asList(DistanceMeasureFactory.getInstance().getKey(Lcss.class))));
        parameterPool.add(Lcss.WARPING_WINDOW_KEY, incrementalDiffList(0, 1, 10));
        double maxTolerance = StatisticUtilities.populationStandardDeviation(instances);
        double minTolerance = maxTolerance * 0.2;
        parameterPool.add(Lcss.TOLERANCE_KEY, incrementalDiffList(minTolerance, maxTolerance, 10));
        return parameterPool;
    }

    public static ParameterPool findErpParameterPool(Instances instances) {
        ParameterPool parameterPool = new ParameterPool();
        parameterPool.add(Nn.DISTANCE_MEASURE_KEY, new ArrayList<>(Arrays.asList(DistanceMeasureFactory.getInstance().getKey(Erp.class))));
        parameterPool.add(Erp.WARPING_WINDOW_KEY, incrementalDiffList(0, 1, 10));
        double maxPenalty = StatisticUtilities.populationStandardDeviation(instances);
        double minPenalty = maxPenalty * 0.2;
        parameterPool.add(Erp.PENALTY_KEY, incrementalDiffList(minPenalty, maxPenalty, 10));
        return parameterPool;
    }

    public Ee() {
        setClassicConfig();
    }

    public void setClassicConfig() {
        parameterPoolObtainers.clear();
        parameterPoolObtainers.add(Ee::findErpParameterPool);
        parameterPoolObtainers.add(Ee::findLcssParameterPool);
        // todo set iteration
        // todo set selection
    }

    public static void main(String[] args) throws Exception {
        Ee ee = new Ee();
        String datasetsDir = "/scratch/Datasets/TSCProblems2019";
        String datasetName = "OliveOil";
        int seed = 0;
        Random random = new Random(seed);
        ee.setTrainRandom(random);
        Instances trainInstances = ClassifierTools.loadData(datasetsDir + "/" + datasetName + "/" + datasetName + "_TRAIN.arff");
        Instances testInstances = ClassifierTools.loadData(datasetsDir + "/" + datasetName + "/" + datasetName + "_TEST.arff");
        Instances[] splitInstances = InstanceTools.resampleTrainAndTestInstances(trainInstances, testInstances, seed);
        trainInstances = splitInstances[0];
        testInstances = splitInstances[1];
        LCSS1NN o = new LCSS1NN();
        Lcss n = new Lcss();
        n.setWarpingWindow(1.0);
        n.setTolerance(1);
        o.setParamsFromParamId(trainInstances, 99);
        System.out.println(o.distance(trainInstances.get(0),  trainInstances.get(1)));
        System.out.println(n.distance(trainInstances.get(0),  trainInstances.get(1)));
//        ClassifierResults results = Utilities.trainAndTest(ee, trainInstances, testInstances, random);
//        results.findAllStatsOnce();
//        System.out.println(results.getAcc());
    }

    @Override
    public double classifyInstance(final Instance instance) throws Exception {
        return votingScheme.classifyInstance(ensembleModules, instance);
    }

    @Override
    public double[] distributionForInstance(final Instance instance) throws Exception {
        return votingScheme.distributionForInstance(ensembleModules, instance);
    }

    @Override
    public ClassifierResults getTestResults(final Instances testInstances) throws Exception {
        throw new UnsupportedOperationException();
    }
}
