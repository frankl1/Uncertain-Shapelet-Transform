package development.go.Ee;

import development.go.Ee.ConstituentBuilders.ConstituentBuilder;
import development.go.Ee.ConstituentBuilders.DistanceMeasureBuilders.*;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Ee extends AbstractClassifier implements OptionsSetter {


    private final List<ConstituentBuilder<?>> originalConstituentBuilders = new ArrayList<>();

    public void addConstituentBuilder(ConstituentBuilder<?> builder) {
        originalConstituentBuilders.add(builder);
    }

    private Random random = new Random();

    public Selector<Nn, ConstituentBuilder<?>> getSelector() {
        return selector;
    }

    public void setSelector(final Selector<Nn, ConstituentBuilder<?>> selector) {
        this.selector = selector;
    }

    private Selector<Nn, ConstituentBuilder<?>> selector = new FirstBestPerType<>(Comparator.comparingDouble(constituent -> constituent.getTrainPrediction().acc));

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
        final Map<ConstituentBuilder<?>, List<Integer>> combinationMap = new HashMap<>();
        final List<ConstituentBuilder<?>> constituentBuilders = new ArrayList<>(originalConstituentBuilders);
        for(ConstituentBuilder<?> constituentBuilder : constituentBuilders) {
            constituentBuilder.setUpParameters(trainInstances);
            List<Integer> combinations = new ArrayList<>();
            for(int combination = 0; combination < constituentBuilder.size(); combination++) {
                combinations.add(combination);
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
            constituentBuilder.setParameterPermutation(combination);
            Nn nn = constituentBuilder.build(); // todo set checkpoint path
            nn.setSampleSizePercentage(sampleSizePercentage);
            nn.setUseRandomTieBreak(false);
            nn.setCvTrain(trainCv);
//            System.out.println(nn.toString() + " " + nn.getDistanceMeasure().getParameters());
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

    private static List<String> datasetNamesFromFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        List<String> datasetNames = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            datasetNames.add(line.trim());
        }
        return datasetNames;
    }

    public static void main(String[] args) throws Exception {
        // todo set seed / random
        String datasetsDirPath = "/scratch/Datasets/TSCProblems2015/";
        File datasetsDir = new File(datasetsDirPath);
        List<String> datasetNames = datasetNamesFromFile(new File("/scratch/datasetList.txt"));
        datasetNames.sort((dA, dB) -> {
            Instances instancesA = ClassifierTools.loadData(datasetsDirPath + dA + "/" + dA + "_TRAIN.arff");
            Instances instancesB = ClassifierTools.loadData(datasetsDirPath + dB + "/" + dB + "_TRAIN.arff");
            return instancesA.numInstances() * instancesA.numAttributes() - instancesB.numInstances() * instancesB.numAttributes();
        });
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        for(String datasetName : datasetNames) {
            threadPoolExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(datasetName);
                        stringBuilder.append(", ");
                        List<Ee> eeList = Arrays.asList(Ee.newClassicConfiguration(), Ee.newFairConfiguration(), Ee.newFairRandomConfiguration());
                        for (Ee ee : eeList) {
                            ee.random.setSeed(0);
                            ee.setSampleSizePercentage(1);
                            Instances trainInstances = ClassifierTools.loadData(datasetsDirPath + datasetName + "/" + datasetName + "_TRAIN.arff");
                            Instances testInstances = ClassifierTools.loadData(datasetsDirPath + datasetName + "/" + datasetName + "_TEST.arff");
                            Instances[] splitInstances = InstanceTools.resampleTrainAndTestInstances(trainInstances, testInstances, 0);
                            trainInstances = splitInstances[0];
                            testInstances = splitInstances[1];
                            ee.buildClassifier(trainInstances);
                            ClassifierResults results = new ClassifierResults();
                            for (Instance testInstance : testInstances) {
                                results.storeSingleResult(testInstance.classValue(), ee.distributionForInstance(testInstance));
                            }
                            results.setNumInstances(testInstances.numInstances());
                            results.setNumClasses(testInstances.numClasses());
                            results.findAllStatsOnce();
                            stringBuilder.append(results.acc);
                            stringBuilder.append(", ");
                        }
                        stringBuilder.append(System.lineSeparator());
                        System.out.println(stringBuilder.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public static Ee newClassicConfiguration() {
        Ee ee = new Ee();
        ee.addConstituentBuilder(new LcssBuilder());
        ee.addConstituentBuilder(new OldDtwBuilder());
        ee.addConstituentBuilder(new OldDdtwBuilder());
        ee.addConstituentBuilder(new OldWdtwBuilder());
        ee.addConstituentBuilder(new OldWddtwBuilder());
        ee.addConstituentBuilder(new ErpBuilder());
        ee.addConstituentBuilder(new MsmBuilder());
        ee.addConstituentBuilder(new TweBuilder());
        ee.addConstituentBuilder(new FullDtwBuilder());
        ee.addConstituentBuilder(new FullDdtwBuilder());
        ee.addConstituentBuilder(new EdBuilder());
        return ee;
    }

    public static Ee newFairConfiguration() {
        Ee ee = new Ee();
        ee.addConstituentBuilder(new DtwBuilder());
        ee.addConstituentBuilder(new DdtwBuilder());
        ee.addConstituentBuilder(new WdtwBuilder());
        ee.addConstituentBuilder(new WddtwBuilder());
        ee.addConstituentBuilder(new LcssBuilder());
        ee.addConstituentBuilder(new ErpBuilder());
        ee.addConstituentBuilder(new TweBuilder());
        ee.addConstituentBuilder(new MsmBuilder());
        return ee;
    }

    public static Ee newFairRandomConfiguration() {
        Ee ee = new Ee();
        ee.addConstituentBuilder(new DtwBuilder());
        ee.addConstituentBuilder(new DdtwBuilder());
        ee.addConstituentBuilder(new WdtwBuilder());
        ee.addConstituentBuilder(new WddtwBuilder());
        ee.addConstituentBuilder(new LcssBuilder());
        ee.addConstituentBuilder(new ErpBuilder());
        ee.addConstituentBuilder(new TweBuilder());
        ee.addConstituentBuilder(new MsmBuilder());
        ee.setSelector(new BestPerType<>(Comparator.comparingDouble(constituent -> constituent.getTrainPrediction().acc)));
        return ee;
    }
}
