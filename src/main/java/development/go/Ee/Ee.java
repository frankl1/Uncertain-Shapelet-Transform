package development.go.Ee;

import development.go.Ee.ConstituentBuilders.ConstituentBuilder;
import development.go.Ee.ConstituentBuilders.DistanceMeasureBuilders.*;
import development.go.Ee.ParameterIteration.IterationStrategy;
import development.go.Ee.ParameterIteration.RandomRoundRobinIterationStrategy;
import development.go.Ee.Selection.BestPerType;
import development.go.Ee.Selection.FirstBestPerType;
import development.go.Ee.Selection.Selector;
import evaluation.storage.ClassifierResults;
import timeseriesweka.classifiers.ParameterSplittable;
import timeseriesweka.classifiers.ensembles.EnsembleModule;
import timeseriesweka.classifiers.ensembles.voting.MajorityVote;
import timeseriesweka.classifiers.ensembles.voting.ModuleVotingScheme;
import timeseriesweka.classifiers.ensembles.weightings.ModuleWeightingScheme;
import timeseriesweka.classifiers.ensembles.weightings.TrainAcc;
import timeseriesweka.classifiers.nn.NeighbourWeighting.UniformWeighting;
import timeseriesweka.classifiers.nn.Nn;

import utilities.ClassifierTools;
import utilities.InstanceTools;
import utilities.OptionsSetter;
import weka.classifiers.AbstractClassifier;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.zip.GZIPInputStream;

public class Ee extends AbstractClassifier implements OptionsSetter, ParameterSplittable {


    private final List<ConstituentBuilder<?>> originalConstituentBuilders = new ArrayList<>();

    public void addConstituentBuilder(ConstituentBuilder<?> builder) {
        originalConstituentBuilders.add(builder);
    }

    private Random random = new Random();

    private Selector<EnsembleModule, String> selector = new FirstBestPerType<>(Comparator.comparingDouble(constituent -> constituent.trainResults.getAcc()));

    public double getSampleSizePercentage() {
        return sampleSizePercentage;
    }

    public void setSampleSizePercentage(final double sampleSizePercentage) {
        this.sampleSizePercentage = sampleSizePercentage;
    }

    private double sampleSizePercentage = 1;
    private boolean trainCv = true;
    private ClassifierResults trainResults;
    private List<EnsembleModule> constituents;
    private EnsembleModule[] modules;
    private ModuleWeightingScheme weightingScheme = new TrainAcc();
    private ModuleVotingScheme votingScheme = new MajorityVote();
    private IterationStrategy iterationStrategy = new RandomRoundRobinIterationStrategy();

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
//        constituents = new ArrayList<>(); // alt ver nn pairs
//        for(int i = 0; i < trainInstances.size(); i++) {
//            Instance a = trainInstances.get(i);
//            for(int j = 0; j < i; j++) {
//                Instance b = trainInstances.get(j);
//                Instances instances = new Instances(trainInstances, 0);
//                instances.add(a);
//                instances.add(b);
//                for(ConstituentBuilder<?> constituentBuilder : constituentBuilders) {
//                    for(int k = 0; k < constituentBuilder.size(); k++) {
////                        System.out.println(i + " " + j + " " + k);
//                        constituentBuilder.setParameterPermutation(k);
//                        Nn nn = constituentBuilder.build();
//                        nn.setUseRandomTieBreak(false);
//                        nn.setCvTrain(true);
//                        nn.setNeighbourWeighter(Nn.WEIGHT_BY_DISTANCE);
//                        nn.buildClassifier(instances);
//                        constituents.add(nn);
//                    }
//                }
//            }
//        }
        while (iterationStrategy.hasNext() && withinContract()) {
            Nn nn = iterationStrategy.next();
            nn.setSampleSizePercentage(sampleSizePercentage);
            nn.setNeighbourWeighter(new UniformWeighting());
            nn.setUseRandomTieBreak(false);
            nn.setCvTrain(trainCv);
//            System.out.println(nn.toString() + " " + nn.getDistanceMeasure().getParameters());
            EnsembleModule ensembleModule = new EnsembleModule();
            if(buildFromFile) {
                String path = resultsFilePath
                    + "/Predictions/"
                    + trainInstances.relationName()
                    + "/" + nn.getDistanceMeasure().toString()
                    + "/" + nn.getDistanceMeasure().getParameters()
                    + "/fold" + seed + ".csv.gzip";
                ObjectInputStream objectInputStream = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(path))));
                double percentage;
                String trainResultsString;
                String testResultsString;
                do {
                    percentage = objectInputStream.readDouble();
                    trainResultsString = (String) objectInputStream.readObject();
                    testResultsString = (String) objectInputStream.readObject();
                } while (percentage != sampleSizePercentage);
                ClassifierResults trainResults = ClassifierResults.parse(trainResultsString);
                ClassifierResults testResults = ClassifierResults.parse(testResultsString);
                ensembleModule.trainResults = trainResults;
                ensembleModule.testResults = testResults;
                ensembleModule.setClassifier(nn);
            } else {
                nn.buildClassifier(trainInstances);
                ensembleModule.setClassifier(nn);
                ensembleModule.trainResults = nn.getTrainResults();
            }
            selector.consider(ensembleModule, nn.getDistanceMeasure().toString());
        }
        constituents = selector.getSelected();
        modules = new EnsembleModule[constituents.size()];
        for(int i = 0; i < modules.length; i++) { // todo if traincv?
            modules[i] = constituents.get(i);
        }
        weightingScheme.defineWeightings(modules, trainInstances.numClasses());
        votingScheme.trainVotingScheme(modules, trainInstances.numClasses());
    }

    private Long seed = 0L;
    private boolean buildFromFile = true;
    private String resultsFilePath = "/scratch/results";

    @Override
    public double[] distributionForInstance(final Instance testInstance) throws Exception {
        return votingScheme.distributionForInstance(modules, testInstance);
    }

    @Override
    public boolean setOption(final String key, final String value) {
        throw new UnsupportedOperationException(); // todo setoptiosn
    }

    public void setSeed(long seed) {
        this.seed = seed;
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
        File datasetFile = new File("/scratch/Datasets/TSCProblems2015/GunPoint");
        int seed = 0;
        String datasetName = datasetFile.getName();
        Instances trainInstances = ClassifierTools.loadData(datasetFile + "/" + datasetName + "_TRAIN.arff");
        Instances testInstances = ClassifierTools.loadData(datasetFile + "/" + datasetName + "_TEST.arff");
        Instances[] splitInstances = InstanceTools.resampleTrainAndTestInstances(trainInstances, testInstances, seed);
        trainInstances = splitInstances[0];
        testInstances = splitInstances[1];
        Ee ee = Ee.newClassicConfiguration();


//        // todo set seed / random
//        System.out.println("rf sampled");
//        String datasetsDirPath = "/scratch/Datasets/TSCProblems2015/";
//        File datasetsDir = new File(datasetsDirPath);
////        List<String> datasetNames = Arrays.asList("OliveOil");
//        List<String> datasetNames = datasetNamesFromFile(new File("/scratch/datasetList.txt"));
//        datasetNames.sort((dA, dB) -> {
//            Instances instancesA = ClassifierTools.loadData(datasetsDirPath + dA + "/" + dA + "_TRAIN.arff");
//            Instances instancesB = ClassifierTools.loadData(datasetsDirPath + dB + "/" + dB + "_TRAIN.arff");
//            return instancesA.numInstances() * instancesA.numAttributes() - instancesB.numInstances() * instancesB.numAttributes();
//        });
//        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//        threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
//        for(String datasetName : datasetNames) {
////            threadPoolExecutor.submit(new Runnable() {
////                @Override
////                public void run() {
//                    try {
//                        StringBuilder stringBuilder = new StringBuilder();
//                        stringBuilder.append(datasetName);
//                        stringBuilder.append(", ");
////                        BufferedReader reader = new BufferedReader(new FileReader("/run/user/33190/gvfs/smb-share:server=cmptscsvr.cmp.uea.ac.uk,share=ueatsc/Results_7_2_19/JayMovingInProgress/EE_proto/Predictions/" + datasetName + "/testFold0.csv"));
////                        reader.readLine();
////                        reader.readLine();
////                        Double bakeoffAcc = Double.valueOf(reader.readLine());
////                        reader.close();
////                        stringBuilder.append(bakeoffAcc);
////                        stringBuilder.append(", ");
//                        List<Ee> eeList = new ArrayList<>();//Arrays.asList(Ee.newFairRandomConfiguration()));
//                        for(int i = 0; i < 100; i += 10) {
//                            Ee ee = Ee.newFairRandomConfiguration();
//                            ee.setSampleSizePercentage((double) i / 100);
//                            eeList.add(ee);
//                        }
//                        for (Ee ee : eeList) {
//                            ee.random.setSeed(0);
//                            Instances trainInstances = ClassifierTools.loadData(datasetsDirPath + datasetName + "/" + datasetName + "_TRAIN.arff");
//                            Instances testInstances = ClassifierTools.loadData(datasetsDirPath + datasetName + "/" + datasetName + "_TEST.arff");
//                            Instances[] splitInstances = InstanceTools.resampleTrainAndTestInstances(trainInstances, testInstances, 0);
//                            trainInstances = splitInstances[0];
//                            testInstances = splitInstances[1];
//                            ee.buildClassifier(trainInstances);
//                            ClassifierResults results = new ClassifierResults();
////                            int i = 0;
//                            for (Instance testInstance : testInstances) {
////                                System.out.println(i++ + "/" + testInstances.numInstances());
//                                results.storeSingleResult(testInstance.classValue(), ee.distributionForInstance(testInstance));
//                            }
//                            results.setNumInstances(testInstances.numInstances());
//                            results.setNumClasses(testInstances.numClasses());
//                            results.findAllStatsOnce();
//                            stringBuilder.append(results.acc);
//                            stringBuilder.append(", ");
//                        }
//                        System.out.println(stringBuilder.toString());
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
////                }
////            });
//        }
//        threadPoolExecutor.shutdown();
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
        ee.setSelector(new BestPerType<>(Comparator.comparingDouble(constituent -> constituent.getTrainResults().getAcc())));
        return ee;
    }

    public static Ee newFairRandomBalAccConfiguration() {
        Ee ee = new Ee();
        ee.addConstituentBuilder(new DtwBuilder());
        ee.addConstituentBuilder(new DdtwBuilder());
        ee.addConstituentBuilder(new WdtwBuilder());
        ee.addConstituentBuilder(new WddtwBuilder());
        ee.addConstituentBuilder(new LcssBuilder());
        ee.addConstituentBuilder(new ErpBuilder());
        ee.addConstituentBuilder(new TweBuilder());
        ee.addConstituentBuilder(new MsmBuilder());
        ee.setSelector(new BestPerType<>(Comparator.comparingDouble(constituent -> constituent.getTrainResults().balancedAcc)));
        return ee;
    }

    @Override
    public void setParamSearch(final boolean b) {

    }

    @Override
    public void setParametersFromIndex(final int x) {

    }

    @Override
    public String getParas() {
        return null;
    }

    @Override
    public double getAcc() {
        return 0;
    }

    public IterationStrategy getIterationStrategy() {
        return iterationStrategy;
    }

    public void setIterationStrategy(final IterationStrategy iterationStrategy) {
        this.iterationStrategy = iterationStrategy;
    }
}
