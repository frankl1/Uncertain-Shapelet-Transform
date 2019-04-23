package ee.ee;

import ee.parameter.ParameterPool;
import ee.selection.BestPerTypeSelector;
import ee.selection.Selector;
import evaluation.storage.ClassifierResults;
import timeseriesweka.classifiers.AdvancedAbstractClassifier.AdvancedAbstractClassifier;
import timeseriesweka.classifiers.Nn.AbstractNn;
import timeseriesweka.classifiers.Nn.Nn;
import timeseriesweka.classifiers.ensembles.EnsembleModule;
import timeseriesweka.classifiers.ensembles.voting.MajorityVote;
import timeseriesweka.classifiers.ensembles.voting.ModuleVotingScheme;
import timeseriesweka.classifiers.ensembles.weightings.ModuleWeightingScheme;
import timeseriesweka.classifiers.ensembles.weightings.TrainAcc;
import timeseriesweka.measures.DistanceMeasureFactory;
import timeseriesweka.measures.erp.Erp;
import timeseriesweka.measures.lcss.Lcss;
import utilities.ClassifierTools;
import utilities.InstanceTools;
import utilities.StatisticUtilities;
import utilities.Utilities;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;
import java.util.function.Function;

import static utilities.Utilities.incrementalDiffList;

public class Ee extends AdvancedAbstractClassifier {

    protected final List<ParameterPool> parameterPools = new ArrayList<>();
    private final List<Function<Instances, ParameterPool>> parameterPoolObtainers = new ArrayList<>();
    private final List<Classifier> candidates = new ArrayList<>();
    private Iterator<String[]> parameterPermutationIterator;
    private Selector<EnsembleModule> selector = new BestPerTypeSelector<>(ensembleModule -> {
        if (!(ensembleModule.getClassifier() instanceof AbstractNn)) {
            throw new IllegalArgumentException();
        } else {
            return ((AbstractNn) ensembleModule.getClassifier()).getDistanceMeasure().toString();
        }
    }, (ensembleModule, t1) -> Double.compare(ensembleModule.trainResults.getAcc(), ensembleModule.trainResults.getAcc()));
    private Function<Ee, Iterator<String[]>> iteratorObtainer = RandomIterator::new;
    private EnsembleModule[] ensembleModules;
    private ModuleWeightingScheme weightingScheme = new TrainAcc();
    private ModuleVotingScheme votingScheme = new MajorityVote();

    @Override
    public void buildClassifier(final Instances trainInstances) throws Exception {
        if(resetOnTrain) {
            trainTime = 0;
            trainTimeStamp = System.nanoTime();
            parameterPools.clear();
            candidates.clear();
            for(Function<Instances, ParameterPool> parameterPoolObtainer : parameterPoolObtainers) {
                parameterPools.add(parameterPoolObtainer.apply(trainInstances));
            }
            parameterPermutationIterator = iteratorObtainer.apply(this);
        }
        while (withinTrainContract() && parameterPermutationIterator.hasNext()) {
            String[] parameterPermutation = parameterPermutationIterator.next();
            System.out.println(Utilities.join(parameterPermutation, ", "));
            Nn nn = new Nn(); // todo generify to abst classifier
            nn.setOptions(parameterPermutation);
//            nn.setTrainContract(); todo get remaining train contract
            nn.buildClassifier(trainInstances);
            // todo pass to parameterPermutationIterator
            EnsembleModule ensembleModule = new EnsembleModule(nn.toString(), nn, nn.getParameters());
            ensembleModule.trainResults = nn.getTrainResults();
            selector.considerCandidate(ensembleModule);
            trainCheckpoint();
        }
        if(resetOnTrain) {
            ensembleModules = selector.getSelected().toArray(new EnsembleModule[0]);
            weightingScheme.defineWeightings(ensembleModules, trainInstances.numClasses());
            votingScheme.trainVotingScheme(ensembleModules, trainInstances.numClasses());
            trainCheckpoint(true);
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


    @Override
    public double classifyInstance(final Instance instance) throws Exception {
        return votingScheme.classifyInstance(ensembleModules, instance);
    }

    @Override
    public double[] distributionForInstance(final Instance instance) throws Exception {
        return votingScheme.distributionForInstance(ensembleModules, instance);
    }
}
