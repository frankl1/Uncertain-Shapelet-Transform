package development.go.Ee;

import development.go.Ee.Constituents.ConstituentBuilder;
import development.go.Ee.Constituents.ParameterSpaces.*;
import development.go.Ee.Constituents.ParameterSpaces.old.*;
import timeseriesweka.classifiers.ensembles.EnsembleModule;
import timeseriesweka.classifiers.ensembles.voting.MajorityVote;
import timeseriesweka.classifiers.ensembles.voting.ModuleVotingScheme;
import timeseriesweka.classifiers.ensembles.weightings.ModuleWeightingScheme;
import timeseriesweka.classifiers.ensembles.weightings.TrainAcc;
import timeseriesweka.classifiers.nn.Nn;
import utilities.*;
import weka.classifiers.AbstractClassifier;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

public class Ee extends AbstractClassifier implements OptionsSetter {


    private final List<ConstituentBuilder> originalConstituentBuilders = new ArrayList<>();

    public void addConstituentBuilder(ConstituentBuilder builder) {
        originalConstituentBuilders.add(builder);
    }

    private Random random = new Random();
    private Selector<Nn, ConstituentBuilder> selector = new FirstBestPerType<>(Comparator.comparingDouble(constituent -> constituent.getTrainPrediction().acc));

    public double getSampleSizePercentage() {
        return sampleSizePercentage;
    }

    public void setSampleSizePercentage(final double sampleSizePercentage) {
        this.sampleSizePercentage = sampleSizePercentage;
    }

    private double sampleSizePercentage = 1;
    private boolean trainCv = true;
    private ClassifierResults trainResults;
    private List<Nn> constituents;
    private EnsembleModule[] modules;
    private ModuleWeightingScheme weightingScheme = new TrainAcc();
    private ModuleVotingScheme votingScheme = new MajorityVote();

    private boolean withinContract() {
        return true; // todo contract
    }

    public ClassifierResults getTrainResults() {
        return trainResults;
    }

    @Override
    public void buildClassifier(final Instances trainInstances) throws Exception {
        final Map<ConstituentBuilder, List<Integer>> combinationMap = new HashMap<>();
        final List<ConstituentBuilder> constituentBuilders = new ArrayList<>(originalConstituentBuilders);
        for(ConstituentBuilder constituentBuilder : constituentBuilders) {
            constituentBuilder.useInstances(trainInstances);
            List<Integer> combinations = new ArrayList<>();
            if(constituentBuilder.getParameterSpace() instanceof OldDdtwParameterSpace) {
                combinations.add(7);
            } else {
                for(int combination = 0; combination < constituentBuilder.size(); combination++) {
                    combinations.add(combination);
                }
            }
            combinationMap.put(constituentBuilder, combinations);
        }
        trainResults = new ClassifierResults();
        while (!constituentBuilders.isEmpty() && withinContract()) {
            int constituentBuilderIndex = random.nextInt(constituentBuilders.size());
            ConstituentBuilder constituentBuilder = constituentBuilders.get(constituentBuilderIndex);
            List<Integer> combinations = combinationMap.get(constituentBuilder);
            int combination = combinations.remove(0);//random.nextInt(combinations.size()));
            if(combinations.isEmpty()) {
                constituentBuilders.remove(constituentBuilderIndex);
            }
//            if(combination == 7 || combination == 6) {
//                if(constituentBuilder.getParameterSpace() instanceof OldDdtwParameterSpace) {
//                    boolean b = true;
//                }
//            }
            Nn nn = constituentBuilder.setCombination(combination).build(); // todo set checkpoint path
            nn.setSampleSizePercentage(sampleSizePercentage);
            nn.setUseRandomTieBreak(false);
            nn.setCvTrain(trainCv);
            System.out.println(nn.toString() + " " + nn.getDistanceMeasure().getParameters());
            nn.buildClassifier(trainInstances);
            selector.consider(nn, constituentBuilder);
        }
        constituents = selector.getSelected();
        modules = new EnsembleModule[constituents.size()];
        for(int i = 0; i < modules.length; i++) { // todo if traincv?
            Nn constituent = constituents.get(i);
            modules[i] = new EnsembleModule(constituent.toString(), constituent, constituent.getParameters());
            modules[i].trainResults = constituent.getTrainPrediction();
        }
        weightingScheme.defineWeightings(modules, trainInstances.numClasses());
        votingScheme.trainVotingScheme(modules, trainInstances.numClasses());
    }

    @Override
    public double[] distributionForInstance(final Instance testInstance) throws Exception {
        return votingScheme.distributionForInstance(modules, testInstance);
    }

    @Override
    public boolean setOption(final String key, final String value) {
        throw new UnsupportedOperationException(); // todo setoptiosn
    }

    public static void main(String[] args) throws Exception {
        Ee ee = new Ee();
        // todo set seed / random
        ee.random.setSeed(0);
//        ee.addConstituentBuilder(new ConstituentBuilder(new LcssParameterSpace()));
        ee.addConstituentBuilder(new ConstituentBuilder(new OldDtwParameterSpace()));
        ee.addConstituentBuilder(new ConstituentBuilder(new OldDdtwParameterSpace()));
        ee.addConstituentBuilder(new ConstituentBuilder(new OldWdtwParameterSpace()));
        ee.addConstituentBuilder(new ConstituentBuilder(new OldWddtwParameterSpace()));
        ee.addConstituentBuilder(new ConstituentBuilder(new ErpParameterSpace()));
        ee.addConstituentBuilder(new ConstituentBuilder(new MsmParameterSpace()));
        ee.addConstituentBuilder(new ConstituentBuilder(new TweParameterSpace()));
        ee.addConstituentBuilder(new ConstituentBuilder(new FullDtwParameterSpace()));
        ee.addConstituentBuilder(new ConstituentBuilder(new FullDdtwParameterSpace()));
        ee.addConstituentBuilder(new ConstituentBuilder(new EdParameterSpace()));
        ee.setSampleSizePercentage(1);
        String datasetName = "GunPoint";
        String datasetsDir = "/scratch/Datasets/TSCProblems2019/";
        Instances trainInstances = ClassifierTools.loadData(datasetsDir + datasetName + "/" + datasetName + "_TRAIN.arff");
        Instances testInstances = ClassifierTools.loadData(datasetsDir + datasetName + "/" + datasetName + "_TEST.arff");
        Instances[] splitInstances = InstanceTools.resampleTrainAndTestInstances(trainInstances, testInstances, 0);
        trainInstances = splitInstances[0];
        testInstances = splitInstances[1];
        ee.buildClassifier(trainInstances);
        ClassifierResults results = new ClassifierResults();
        for(Instance testInstance : testInstances) {
            results.storeSingleResult(testInstance.classValue(), ee.distributionForInstance(testInstance));
        }
        results.setNumInstances(testInstances.numInstances());
        results.setNumClasses(testInstances.numClasses());
        results.findAllStatsOnce();
        System.out.println(results.acc);
    }
}
