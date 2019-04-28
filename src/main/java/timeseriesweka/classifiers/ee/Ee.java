package timeseriesweka.classifiers.ee;

import utilities.parameters.ParameterPermutation;
import utilities.parameters.ParameterPool;
import timeseriesweka.classifiers.ee.selection.BestPerTypeSelector;
import timeseriesweka.classifiers.ee.selection.Selector;
import evaluation.evaluators.CrossValidationEvaluator;
import evaluation.evaluators.Evaluator;
import evaluation.storage.ClassifierResults;
import timeseriesweka.classifiers.AdvancedAbstractClassifier.AdvancedAbstractClassifier;
import timeseriesweka.classifiers.ContractClassifier;
import timeseriesweka.classifiers.Nn.AbstractNn;
import timeseriesweka.classifiers.Nn.Nn;
import timeseriesweka.classifiers.ensembles.EnsembleModule;
import timeseriesweka.classifiers.ensembles.voting.MajorityVote;
import timeseriesweka.classifiers.ensembles.voting.ModuleVotingScheme;
import timeseriesweka.classifiers.ensembles.weightings.ModuleWeightingScheme;
import timeseriesweka.classifiers.ensembles.weightings.TrainAcc;
import utilities.*;
import weka.classifiers.AbstractClassifier;
import weka.core.Instance;
import weka.core.Instances;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class Ee extends AdvancedAbstractClassifier {

    public static final Selector<EnsembleModule> DEFAULT_SELECTOR = new BestPerTypeSelector<>(ensembleModule -> {
        if (!(ensembleModule.getClassifier() instanceof AbstractNn)) {
            throw new IllegalArgumentException();
        } else {
            return ((AbstractNn) ensembleModule.getClassifier()).getDistanceMeasure().toString();
        }
    }, Comparator.comparingDouble(ensembleModule -> ensembleModule.trainResults.getAcc()));
    protected final List<ParameterPool> parameterPools = new ArrayList<>();
    private final List<Function<Instances, ParameterPool>> parameterPoolObtainers = new ArrayList<>();
    private Iterator<String[]> parameterPermutationIterator;
    private Selector<EnsembleModule> selector = DEFAULT_SELECTOR;
    private IterationMethod iterationMethod = IterationMethod.RANDOM;
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
            parameterPermutationIterator = generateIterator(iterationMethod);
            selector.setRandom(getTrainRandom());
            selector.clear();
        }
        while (withinTrainContract() && parameterPermutationIterator.hasNext()) {
            String[] parameterPermutation = parameterPermutationIterator.next();
            getLogger().info("Running parameters permutation: " + Utilities.join(parameterPermutation, ", "));
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
//                    throw new IllegalStateException("parameters indices don't match");
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


    public Ee() {
        setClassicConfig();
    }

    public void setClassicConfig() {
        parameterPoolObtainers.clear();
        parameterPoolObtainers.add(ParameterPoolFactory::classicDtwParameterPool);
        parameterPoolObtainers.add(ParameterPoolFactory::euclideanParameterPool);
        parameterPoolObtainers.add(ParameterPoolFactory::fullWindowDtwParameterPool);
        parameterPoolObtainers.add(ParameterPoolFactory::classicDdtwParameterPool);
        parameterPoolObtainers.add(ParameterPoolFactory::classicWdtwParameterPool);
        parameterPoolObtainers.add(ParameterPoolFactory::classicWddtwParameterPool);
        parameterPoolObtainers.add(ParameterPoolFactory::lcssParameterPool);
        parameterPoolObtainers.add(ParameterPoolFactory::erpParameterPool);
        parameterPoolObtainers.add(ParameterPoolFactory::tweParameterPool);
        parameterPoolObtainers.add(ParameterPoolFactory::msmParameterPool);
        setIterationMethod(IterationMethod.LINEAR);
        setSelector(DEFAULT_SELECTOR);
    }

    public void setDefaultConfig() {
        parameterPoolObtainers.clear();
        parameterPoolObtainers.add(ParameterPoolFactory::dtwParameterPool);
        parameterPoolObtainers.add(ParameterPoolFactory::ddtwParameterPool);
        parameterPoolObtainers.add(ParameterPoolFactory::wddtwParameterPool);
        parameterPoolObtainers.add(ParameterPoolFactory::wddtwParameterPool);
        parameterPoolObtainers.add(ParameterPoolFactory::lcssParameterPool);
        parameterPoolObtainers.add(ParameterPoolFactory::erpParameterPool);
        parameterPoolObtainers.add(ParameterPoolFactory::tweParameterPool);
        parameterPoolObtainers.add(ParameterPoolFactory::msmParameterPool);
        setIterationMethod(IterationMethod.RANDOM);
        setSelector(DEFAULT_SELECTOR);

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
        ClassifierResults results = Utilities.trainAndTest(ee, trainInstances, testInstances, random);
        results.findAllStatsOnce();
        System.out.println(results.getAcc());
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

    public IterationMethod getIterationMethod() {
        return iterationMethod;
    }

    public void setIterationMethod(IterationMethod iterationMethod) {
        this.iterationMethod = iterationMethod;
    }

    public enum IterationMethod {
        LINEAR,
        RANDOM;

        @Override
        public String toString() {
            return name();
        }

        public static IterationMethod fromString(String str) {
            for(int i = 0; i < IterationMethod.values().length; i++) {
                IterationMethod method = IterationMethod.values()[i];
                if(str.equals(method)) {
                    return method;
                }
            }
            throw new IllegalArgumentException("unrecognised iteration method: " + str);
        }
    }

    private Iterator<String[]> generateIterator(IterationMethod iterationMethod) {
        if (iterationMethod.equals(IterationMethod.LINEAR)) {
            return new LinearIterator();
        } else if (iterationMethod.equals(IterationMethod.RANDOM)) {
            return new RandomIterator();
        }
        throw new IllegalArgumentException("Unknown iteration method: " + iterationMethod);
    }

    private class LinearIterator implements Iterator<String[]> {
        private int index = 0;

        @Override
        public boolean hasNext() {
            return !parameterPools.isEmpty();
        }

        @Override
        public String[] next() { // todo deal with pool already empty
            ParameterPool parameterPool = parameterPools.get(0);
            ParameterPermutation parameterPermutation = parameterPool.getParameterPermutationFromIndexAndRandom(index, getTrainRandom());
            index++;
            if(index >= Utilities.numPermutations(parameterPool.getDiscreteParameterPoolSizes())) {
                index = 0;
                parameterPools.remove(0);
            }
            return parameterPermutation.getOptions();
        }
    }

    private class RandomIterator implements Iterator<String[]> {

        private final Map<ParameterPool, List<Integer>> indicesMap = new HashMap<>();

        public RandomIterator() {
            for(ParameterPool parameterPool : parameterPools) {
                indicesMap.put(parameterPool, Utilities.naturalNumbersFromZero(parameterPool.getNumDiscreteParameterPermutations() - 1));
            }
        }

        @Override
        public boolean hasNext() {
            return !parameterPools.isEmpty();
        }

        @Override
        public String[] next() { // todo deal with pool already empty
            Random random = getTrainRandom();
            int parameterPoolIndex = random.nextInt(parameterPools.size());
            ParameterPool parameterPool = parameterPools.get(parameterPoolIndex);
            List<Integer> indices = indicesMap.get(parameterPool);
            int index = indices.remove(random.nextInt(indices.size()));
            ParameterPermutation parameterPermutation = parameterPool.getParameterPermutationFromIndexAndRandom(index, random);
            if(indices.isEmpty()) {
                parameterPools.remove(parameterPoolIndex);
                indicesMap.remove(parameterPool);
            }
            return parameterPermutation.getOptions();
        }
    }

}
