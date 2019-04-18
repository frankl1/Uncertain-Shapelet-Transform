package ee;

import ee.parameter.ParameterPermutation;
import ee.parameter.ParameterPool;
import ee.parameter.ParameterPermutationIterator;
import ee.selection.BestPerTypeSelector;
import ee.selection.Selector;
import evaluation.evaluators.CrossValidationEvaluator;
import evaluation.evaluators.Evaluator;
import evaluation.storage.ClassifierResults;
import timeseriesweka.classifiers.AdvancedAbstractClassifier.AdvancedAbstractClassifier;
import timeseriesweka.classifiers.Nn.AbstractNn;
import timeseriesweka.classifiers.Nn.Nn;
import timeseriesweka.classifiers.Nn.Specialised.Ddtw.DdtwNn;
import timeseriesweka.classifiers.Nn.Specialised.Dtw.DtwNn;
import timeseriesweka.classifiers.Nn.Specialised.Erp.ErpNn;
import timeseriesweka.classifiers.Nn.Specialised.Lcss.LcssNn;
import timeseriesweka.classifiers.Nn.Specialised.Msm.MsmNn;
import timeseriesweka.classifiers.Nn.Specialised.Twe.TweNn;
import timeseriesweka.classifiers.Nn.Specialised.Wddtw.WddtwNn;
import timeseriesweka.classifiers.Nn.Specialised.Wdtw.WdtwNn;
import timeseriesweka.classifiers.ensembles.EnsembleModule;
import timeseriesweka.classifiers.ensembles.voting.MajorityConfidence;
import timeseriesweka.classifiers.ensembles.voting.ModuleVotingScheme;
import timeseriesweka.classifiers.ensembles.weightings.ModuleWeightingScheme;
import timeseriesweka.classifiers.ensembles.weightings.TrainAcc;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;
import timeseriesweka.measures.erp.Erp;
import timeseriesweka.measures.lcss.Lcss;
import timeseriesweka.measures.msm.Msm;
import timeseriesweka.measures.twe.Twe;
import timeseriesweka.measures.wdtw.Wdtw;
import utilities.ClassifierTools;
import utilities.InstanceTools;
import utilities.StatisticUtilities;
import utilities.Utilities;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static utilities.Utilities.incrementalDiffList;
import static utilities.Utilities.incrementalDiffListInt;

public class Ee
    extends AdvancedAbstractClassifier
    {

    private BiFunction<List<Constituent>, Random, Iterator<Classifier>> constituentIteratorSupplier = new BiFunction<List<Constituent>, Random, Iterator<Classifier>>() {
        @Override
        public Iterator<Classifier> apply(List<Constituent> constituents, Random random) {
            constituents.removeIf(constituent -> !constituent.hasNext());
            return new Iterator<Classifier>() {
                private final List<Constituent> constituentList = new ArrayList<>(constituents);


                @Override
                public boolean hasNext() {
                    return !constituentList.isEmpty();
                }

                @Override
                public Classifier next() {
                    int index = random.nextInt(constituentList.size());
                    Constituent constituent = constituentList.get(index);
                    Classifier next = constituent.next();
                    if(!constituent.hasNext()) {
                        constituentList.remove(index);
                    }
                    return next;
                }
            };
        }
    };
    private Iterator<Classifier> constituentIterator;
    private Selector<EnsembleModule> constituentSelector = new BestPerTypeSelector<>(ensembleModule -> {
        Classifier classifier = ensembleModule.getClassifier();
        if (classifier instanceof AbstractNn) {
            DistanceMeasure distanceMeasure = ((AbstractNn) classifier).getDistanceMeasure();
            return distanceMeasure.toString();
        } else {
            throw new IllegalStateException("cannot select based on distance measure");
        }
    }, Comparator.comparingDouble(ensembleModule -> ensembleModule.trainResults.getAcc()));
    private EnsembleModule[] selectedCandidates;

    @Override
    public void buildClassifier(Instances trainInstances) throws Exception {
        getCapabilities().testWithFail(trainInstances);
        if(resetOnTrain) {
            trainTime = 0;
            trainTimeStamp = System.nanoTime();
            ConstituentBuilderParameters parameters = new ConstituentBuilderParameters(trainInstances, random);
            for(ConstituentBuilder builder : constituentBuilders) {
                Constituent constituent = builder.apply(parameters);
                constituents.add(constituent);
            }
            constituentIterator = constituentIteratorSupplier.apply(constituents, random);
        }
        while(withinTrainContract() && constituentIterator.hasNext()) {
            Classifier classifier = constituentIterator.next();
            System.out.print(classifier.toString()); // todo logger
            if(classifier instanceof AbstractNn) {
                AbstractNn nn = (AbstractNn) classifier;
                System.out.println(" " + Utilities.join(nn.getDistanceMeasure().getOptions(), ", "));
            } else {
                System.out.println();
            }
            Evaluator evaluator = new CrossValidationEvaluator(); // todo set contract + undo at end
            ClassifierResults trainResults = evaluator.evaluate(classifier, trainInstances);
            EnsembleModule module = new EnsembleModule();
            module.trainResults = trainResults;
            module.setClassifier(classifier);
            constituentSelector.considerCandidate(module);
            trainCheckpoint();
        }
        selectedCandidates = constituentSelector.getSelected().toArray(new EnsembleModule[0]);
        weightingScheme.defineWeightings(selectedCandidates, trainInstances.numClasses());
        votingScheme.trainVotingScheme(selectedCandidates, trainInstances.numClasses());
        trainCheckpoint();
    }

    private ModuleVotingScheme votingScheme = new MajorityConfidence();
    private ModuleWeightingScheme weightingScheme = new TrainAcc();

    @Override
    public double classifyInstance(Instance testInstance) throws Exception {
        return votingScheme.classifyInstance(selectedCandidates, testInstance);
    }

    @Override
    public double[] distributionForInstance(Instance testInstance) throws Exception {
        return votingScheme.distributionForInstance(selectedCandidates, testInstance);
    }

    @Override
    public Capabilities getCapabilities() {
        Capabilities result = super.getCapabilities();
        result.disableAll();

        // attributes must be numeric
        // Here add in relational when ready
        result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
        // class
        result.enable(Capabilities.Capability.NOMINAL_CLASS);
        // instances
        result.setMinimumNumberInstances(0);

        return result;
    }

    private Random random;

    @Override
    public void setRandom(Random random) {
        this.random = random;
    }

    @Override
    public Random getRandom() {
        return random;
    }

    private static class Constituent implements Iterator<Classifier> {
        private final Supplier<AbstractClassifier> classifierSupplier;
        private Iterator<ParameterPermutation> parameterPermutationIterator;

        public Constituent(Supplier<AbstractClassifier> classifierSupplier, Iterator<ParameterPermutation> parameterPermutationIterator) {
            this.classifierSupplier = classifierSupplier;
            this.parameterPermutationIterator = parameterPermutationIterator;
        }

        @Override
        public boolean hasNext() {
            return parameterPermutationIterator.hasNext();
        }

        @Override
        public Classifier next() {
            AbstractClassifier classifier = classifierSupplier.get();
            try {
                classifier.setOptions(parameterPermutationIterator.next().getOptions());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return classifier;
        }
    }

    private final List<Constituent> constituents = new ArrayList<>();
    private final List<ConstituentBuilder> constituentBuilders = new ArrayList<>();

    public static class IterationParameters {
        public final Random random;
        public final ParameterPool parameterPool;

        public IterationParameters(final Random random, final ParameterPool parameterPool) {
            this.random = random;
            this.parameterPool = parameterPool;
        }
    }

    public static class ConstituentBuilderParameters {
        public final Instances trainInstances;
        public final Random random;

        public ConstituentBuilderParameters(Instances trainInstances, Random random) {
            this.trainInstances = trainInstances;
            this.random = random;
        }
    }

    public interface ConstituentBuilder extends Function<ConstituentBuilderParameters, Constituent> {}

    public void addConstituentBuilder(Supplier<AbstractClassifier> classifierSupplier,
                                      Function<Instances, ParameterPool> parameterPoolObtainer,
                                      Function<IterationParameters, Iterator<ParameterPermutation>> iteratorObtainer) {
        ConstituentBuilder constituentBuilder = builderParameters -> {
            ParameterPool parameterPool = parameterPoolObtainer.apply(builderParameters.trainInstances);
            IterationParameters iterationParameters = new IterationParameters(builderParameters.random, parameterPool);
            Constituent constituent = new Constituent(classifierSupplier, iteratorObtainer.apply(iterationParameters));
            return constituent;
        };
        constituentBuilders.add(constituentBuilder);
    }

    public static ParameterPool getDtwParameterPool(Instances instances) {
        int instanceLength = instances.numAttributes() - 1;
        ParameterPool parameterPool = new ParameterPool();
        parameterPool.add(Dtw.WARPING_WINDOW_KEY, incrementalDiffListInt(0, instanceLength, 100));
        return parameterPool;
    }

    public static ParameterPool getLcssParameterPool(Instances instances) {
        ParameterPool parameterPool = new ParameterPool();
        int instanceLength = instances.numAttributes() - 1;
        instanceLength *= 0.25;
        parameterPool.add(Lcss.WARPING_WINDOW_KEY, incrementalDiffListInt(0, instanceLength, 10));
        double maxTolerance = StatisticUtilities.populationStandardDeviation(instances);
        double minTolerance = maxTolerance * 0.2;
        parameterPool.add(Lcss.TOLERANCE_KEY, incrementalDiffList(minTolerance, maxTolerance, 10));
        return parameterPool;
    }

    public static ParameterPool getErpParameterPool(Instances instances) {
        ParameterPool parameterPool = new ParameterPool();
        int instanceLength = instances.numAttributes() - 1;
        instanceLength *= 0.25;
        parameterPool.add(Erp.WARPING_WINDOW_KEY, incrementalDiffListInt(0, instanceLength, 10));
        double maxPenalty = StatisticUtilities.populationStandardDeviation(instances);
        double minPenalty = maxPenalty * 0.2;
        parameterPool.add(Erp.PENALTY_KEY, incrementalDiffList(minPenalty, maxPenalty, 10));
        return parameterPool;
    }

    public static ParameterPool getTweParameterPool(Instances instances) {
        ParameterPool parameterPool = new ParameterPool();
        parameterPool.add(Twe.PENALTY_KEY, new ArrayList<>(Arrays.asList(0d,
                0.011111111,
                0.022222222,
                0.033333333,
                0.044444444,
                0.055555556,
                0.066666667,
                0.077777778,
                0.088888889,
                0.1)));
        parameterPool.add(Twe.STIFFNESS_KEY, new ArrayList<>(Arrays.asList(0.00001,
                0.0001,
                0.0005,
                0.001,
                0.005,
                0.01,
                0.05,
                0.1,
                0.5,
                1d)));
        return parameterPool;
    }

    public static ParameterPool getMsmParameterPool(Instances instances) {
        ParameterPool parameterPool = new ParameterPool();
        parameterPool.add(Msm.PENALTY_KEY, new ArrayList<>(Arrays.asList(
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
                1d,
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
                10d,
                13.6,
                17.2,
                20.8,
                24.4,
                28d,
                31.6,
                35.2,
                38.8,
                42.4,
                46d,
                49.6,
                53.2,
                56.8,
                60.4,
                64d,
                67.6,
                71.2,
                74.8,
                78.4,
                82d,
                85.6,
                89.2,
                92.8,
                96.4,
                100d
        )));
        return parameterPool;
    }

    public static ParameterPool getWdtwParameterPool(Instances instances) {
        ParameterPool parameterPool = new ParameterPool();
        parameterPool.add(Wdtw.WEIGHT_KEY, Utilities.linearInterpolate(100, 100));
        return parameterPool;
    }

    public Ee() {
        setRandom(new Random());
        applyStandardConfiguration();
    }

    public void applyStandardConfiguration() {
        constituentBuilders.clear();

        addConstituentBuilder(DtwNn::new, Ee::getDtwParameterPool, iterationParameters -> new ParameterPermutationIterator(iterationParameters.parameterPool, iterationParameters.random));
        addConstituentBuilder(DdtwNn::new, Ee::getDtwParameterPool, iterationParameters -> new ParameterPermutationIterator(iterationParameters.parameterPool, iterationParameters.random));
        addConstituentBuilder(WdtwNn::new, Ee::getWdtwParameterPool, iterationParameters -> new ParameterPermutationIterator(iterationParameters.parameterPool, iterationParameters.random));
        addConstituentBuilder(WddtwNn::new, Ee::getWdtwParameterPool, iterationParameters -> new ParameterPermutationIterator(iterationParameters.parameterPool, iterationParameters.random));
        addConstituentBuilder(LcssNn::new, Ee::getLcssParameterPool, iterationParameters -> new ParameterPermutationIterator(iterationParameters.parameterPool, iterationParameters.random));
        addConstituentBuilder(MsmNn::new, Ee::getMsmParameterPool, iterationParameters -> new ParameterPermutationIterator(iterationParameters.parameterPool, iterationParameters.random));
        addConstituentBuilder(ErpNn::new, Ee::getErpParameterPool, iterationParameters -> new ParameterPermutationIterator(iterationParameters.parameterPool, iterationParameters.random));
        addConstituentBuilder(TweNn::new, Ee::getTweParameterPool, iterationParameters -> new ParameterPermutationIterator(iterationParameters.parameterPool, iterationParameters.random));

    }

    public static void main(String[] args) throws Exception {
//        Random random = new Random(0);
//        List<Double> warpingWindow = new ArrayList<>(Arrays.asList(1.0,2.0,3.0,4.0,5.0));
////        Distribution<Double> tolerance = new UniformDistribution();
//        List<Double> tolerance = new ArrayList<>(Arrays.asList(6.0, 7.0, 8.0));
//        ParameterPool pool = new ParameterPool();
//        pool.add(Lcss.TOLERANCE_KEY, tolerance);
//        pool.add(Lcss.WARPING_WINDOW_KEY, warpingWindow);
////        ParameterPermutationIterator randomIterator = new ParameterPermutationIterator(pool, new Random(0), 20);
//

        Ee ee = new Ee();
        String datasetsDir = "/scratch/Datasets/TSCProblems2019";
        String datasetName = "OliveOil";
        int seed = 0;
        Random random = new Random(seed);
        ee.setRandom(random);
        Instances trainInstances = ClassifierTools.loadData(datasetsDir + "/" + datasetName + "/" + datasetName + "_TRAIN.arff");
        Instances testInstances = ClassifierTools.loadData(datasetsDir + "/" + datasetName + "/" + datasetName + "_TEST.arff");
        Instances[] splitInstances = InstanceTools.resampleTrainAndTestInstances(trainInstances, testInstances, seed);
        trainInstances = splitInstances[0];
        testInstances = splitInstances[1];
        ClassifierResults results = Utilities.trainAndTest(ee, trainInstances, testInstances, random);
        results.findAllStatsOnce();
        System.out.println(results.getAcc());
    }
}
