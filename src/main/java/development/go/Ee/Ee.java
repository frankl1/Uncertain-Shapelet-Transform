package development.go.Ee;

import development.go.Ee.ConstituentBuilders.ConstituentBuilder;
import development.go.Ee.ConstituentBuilders.DistanceMeasureBuilders.*;
import development.go.Ee.ParameterIteration.IterationStrategy;
import development.go.Ee.ParameterIteration.RandomRoundRobinIterationStrategy;
import development.go.Ee.Selection.FirstBestPerType;
import development.go.Ee.Selection.Selector;
import evaluation.storage.ClassifierResults;
import net.sourceforge.sizeof.SizeOf;
import timeseriesweka.classifiers.CheckpointClassifier;
import timeseriesweka.classifiers.ContractClassifier;
import timeseriesweka.classifiers.SaveParameterInfo;
import timeseriesweka.classifiers.ensembles.EnsembleModule;
import timeseriesweka.classifiers.ensembles.voting.MajorityVote;
import timeseriesweka.classifiers.ensembles.voting.ModuleVotingScheme;
import timeseriesweka.classifiers.ensembles.weightings.ModuleWeightingScheme;
import timeseriesweka.classifiers.ensembles.weightings.TrainAcc;
import timeseriesweka.classifiers.nn.NeighbourWeighting.UniformWeighting;
import timeseriesweka.classifiers.nn.Nn;

import utilities.*;
import weka.classifiers.AbstractClassifier;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

public class Ee extends AbstractClassifier implements SaveParameterInfo
    , OptionsSetter
    , Serializable
    , Reproducible
//    , ParameterSplittable
    , TrainAccuracyEstimate
    , CheckpointClassifier
    , ContractClassifier
{


    private static final String CHECKPOINT_FILE_NAME = "checkpoint.ser.gzip";
    private final List<ConstituentBuilder<?>> originalConstituentBuilders = new ArrayList<>();
    private Random random = new Random();
    private Selector<EnsembleModule, String> selector = new FirstBestPerType<>(Comparator.comparingDouble(constituent -> constituent.trainResults.getAcc()));
    private double sampleSizePercentage = 1;
    private boolean trainCv = true;
    private ClassifierResults trainResults;
    private EnsembleModule[] modules;
    private ModuleWeightingScheme weightingScheme = new TrainAcc();
    private ModuleVotingScheme votingScheme = new MajorityVote();
    private IterationStrategy iterationStrategy = new RandomRoundRobinIterationStrategy();
    private Long seed = 0L;
    private boolean buildFromFile = true;
    private String resultsFilePath = "/scratch/results";
    private long trainTime;
    private long trainContract = -1;
    private long testContract = -1;
    private long testTime;
    private long predictionTime;
    private long predictionContract = -1;
    private long trainTimeStamp;
    private String checkpointFilePath = null;
    private long minCheckpointInterval = TimeUnit.NANOSECONDS.convert(10, TimeUnit.MINUTES);
    private long lastCheckpointTimeStamp = -1;
    private boolean isCheckpointing = false;
    private boolean useRandomTieBreak = false;

    private Ee() {
        reset();
    }

    public void reset() {
        trainTime = -1;
        predictionTime = -1;
        testTime = -1;
        originalConstituentBuilders.clear();
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

    public void addConstituentBuilder(ConstituentBuilder<?> builder) {
        originalConstituentBuilders.add(builder);
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
//        ee.setSelector(new BestPerType<>(Comparator.comparingDouble(constituent -> constituent.getTrainResults().getAcc())));
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
//        ee.setSelector(new BestPerType<>(Comparator.comparingDouble(constituent -> constituent.getTrainResults().balancedAcc)));
        return ee;
    }

    public double getSampleSizePercentage() {
        return sampleSizePercentage;
    }

    public void setSampleSizePercentage(final double sampleSizePercentage) {
        this.sampleSizePercentage = sampleSizePercentage;
    }

    @Override
    public void setFindTrainAccuracyEstimate(final boolean setCV) {
        setTrainCv(setCV);
    }

    @Override
    public boolean findsTrainAccuracyEstimate() {
        return true;
    }

    @Override
    public void writeCVTrainToFile(final String train) {
        throw new UnsupportedOperationException();
    }

    public ClassifierResults getTrainResults() {
        return trainResults;
    }

    private void trainCheckpoint() throws IOException {
        trainCheckpoint(false);
    }

    public boolean isCheckpointing() {
        return isCheckpointing;
    }

    public void setCheckpointing(boolean on) {
        isCheckpointing = on;
    }

    private void checkpoint(boolean force) throws IOException {
        if(isCheckpointing() && (force || System.nanoTime() - lastCheckpointTimeStamp > minCheckpointInterval)) {
            saveToFile(checkpointFilePath);
        }
    }

    private void trainCheckpoint(boolean force) throws IOException {
        long timeStamp = System.nanoTime();
        trainTime += timeStamp - trainTimeStamp;
        checkpoint(force);
        trainTimeStamp = System.nanoTime();
    }

    @Override
    public void buildClassifier(final Instances trainInstances) throws Exception {
        resumeFromCheckpoint();
        trainTimeStamp = System.nanoTime();
        final List<ConstituentBuilder<?>> constituentBuilders = new ArrayList<>(originalConstituentBuilders);
        for(ConstituentBuilder<?> constituentBuilder : constituentBuilders) {
            constituentBuilder.setUpParameters(trainInstances);
        }
        trainResults = new ClassifierResults();
        trainCheckpoint();
        while (iterationStrategy.hasNext() && (trainContract < 0 || trainTime < trainContract)) {
            Nn nn = iterationStrategy.next();
            nn.setCvTrain(trainCv);
            nn.setCheckpointing(isCheckpointing());
            nn.setTrainContract(trainContract - trainTime);
            // todo below should be offloaded to constituent builders perhaps?
            nn.setSampleSizePercentage(sampleSizePercentage);
//            System.out.println(nn.toString() + " " + nn.getDistanceMeasure().getParameters());
            EnsembleModule ensembleModule = new EnsembleModule();
            if(buildFromFile) {
                long timeStamp = System.nanoTime();
                trainTime += timeStamp - trainTimeStamp;
                trainTimeStamp = timeStamp;
                String path = resultsFilePath // todo this string probs need adjusting for running consistuents individually
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
                trainTime += trainResults.getBuildTimeInNanos();
                trainTimeStamp = System.nanoTime();
            } else {
                nn.buildClassifier(trainInstances);
                ensembleModule.setClassifier(nn);
                ensembleModule.trainResults = nn.getTrainResults();
            }
            selector.consider(ensembleModule, nn.getDistanceMeasure().toString());
            trainCheckpoint();
        }
        List<EnsembleModule> constituents = selector.getSelected();
        modules = new EnsembleModule[constituents.size()];
//        long constituentPredictionContract = predictionContract / modules.length;
//        long constituentTestContract = testContract / modules.length;
        for(int i = 0; i < modules.length; i++) {
            modules[i] = constituents.get(i);
//            modules[i].setPredictionContract(constituentPredictionContract); // todo when new api is enforced
//            modules[i].setTestContract(constituentTestContract); // todo when new api is enforced
        }
        weightingScheme.defineWeightings(modules, trainInstances.numClasses());
        votingScheme.trainVotingScheme(modules, trainInstances.numClasses());
        trainCheckpoint(true);
    }

    private void resumeFromCheckpoint() throws Exception {
        if(isCheckpointing()) {
            try {
                loadFromFile(checkpointFilePath);
            } catch (FileNotFoundException e) {

            }
        }
    }

    public Random getRandom() {
        return random;
    }

    public void setRandom(final Random random) {
        this.random = random;
    }

    public Selector<EnsembleModule, String> getSelector() {
        return selector;
    }

    public void setSelector(final Selector<EnsembleModule, String> selector) {
        this.selector = selector;
    }

    public boolean isTrainCv() {
        return trainCv;
    }

    public void setTrainCv(final boolean trainCv) {
        this.trainCv = trainCv;
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

    public Long getSeed() {
        return seed;
    }

    public void setSeed(final Long seed) {
        this.seed = seed;
        random.setSeed(seed); // todo reset
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public boolean isBuildFromFile() { // todo rename these
        return buildFromFile;
    }

    public void setBuildFromFile(final boolean buildFromFile) {
        this.buildFromFile = buildFromFile;
    }

    public String getResultsFilePath() {
        return resultsFilePath;
    }

    public void setResultsFilePath(final String resultsFilePath) {
        this.resultsFilePath = resultsFilePath;
    }

    public long getTrainContract() {
        return trainContract;
    }

    public void setTrainContract(final long trainContract) {
        this.trainContract = trainContract;
    }

    public long getTestContract() {
        return testContract;
    }

    public void setTestContract(final long testContract) {
        this.testContract = testContract;
    }

    public long getPredictionContract() {
        return predictionContract;
    }

    public void setPredictionContract(final long predictionContract) {
        this.predictionContract = predictionContract;
    }

    public long getMinCheckpointInterval() {
        return minCheckpointInterval;
    }

    public void setMinCheckpointInterval(final long minCheckpointInterval) {
        this.minCheckpointInterval = minCheckpointInterval;
    }

    public boolean isUseRandomTieBreak() {
        return useRandomTieBreak;
    }

    public void setUseRandomTieBreak(final boolean useRandomTieBreak) {
        this.useRandomTieBreak = useRandomTieBreak;
    }

    public ClassifierResults getTestResults(final Instances testInstances) throws Exception {
        ClassifierResults results = new ClassifierResults();
        for(int i = 0; i < testInstances.size(); i++) {
            Instance testInstance = testInstances.get(i);
            long timeStamp = System.nanoTime();
            double[] prediction = distributionForInstance(testInstance);
            predictionTime = System.nanoTime() - timeStamp;
            double predictedClass;
            if(useRandomTieBreak) {
                predictedClass = Utilities.argMax(prediction, random);
            } else {
                predictedClass = Utilities.argMax(prediction)[0];
            }
            results.addPrediction(testInstance.classValue(), prediction, predictedClass, predictionTime, null);
        }
        setResultsMetaData(testInstances, results);
        return results;
    }

    @Override
    public double[] distributionForInstance(final Instance testInstance) throws Exception {
        return votingScheme.distributionForInstance(modules, testInstance);
    }

    private ClassifierResults setResultsMetaData(final Instances instances, ClassifierResults results) throws Exception {
        results.setNumClasses(instances.numClasses());
        try {
            results.setMemory(SizeOf.deepSizeOf(this));
        } catch (Exception e) {

        }
        results.setClassifierName(toString());
        results.setParas(getParameters());
        results.setTimeUnit(TimeUnit.NANOSECONDS);
        results.setBuildTime(getTrainTime());
        results.setTestTime(getTestTime());
        results.findAllStatsOnce();
        return results;
    }

    @Override
    public String getParameters() {
        return null; // todo
    }

    public long getTrainTime() {
        return trainTime;
    }

//    @Override
//    public void setParamSearch(final boolean b) {
//
//    }
//
//    @Override
//    public void setParametersFromIndex(final int x) {
//
//    }
//
//    @Override
//    public String getParas() {
//        return null;
//    }
//
//    @Override
//    public double getAcc() {
//        return 0;
//    }

    public long getTestTime() {
        return testTime;
    }

    public boolean usesRandomTieBreak() {
        return useRandomTieBreak;
    }

    @Override
    public boolean setOption(final String key, final String value) {
        throw new UnsupportedOperationException(); // todo setoptiosn
    }

    public IterationStrategy getIterationStrategy() {
        return iterationStrategy;
    }

    public void setIterationStrategy(final IterationStrategy iterationStrategy) {
        this.iterationStrategy = iterationStrategy;
    }

    @Override
    public void setSavePath(final String path) {
        checkpointFilePath = new File(path, CHECKPOINT_FILE_NAME).getPath();
    }

    @Override
    public void copyFromSerObject(final Object obj) throws Exception {
        reset();
        Ee other = (Ee) obj;
        seed = other.seed;
        random = other.random;
        originalConstituentBuilders.clear();
        originalConstituentBuilders.addAll(other.originalConstituentBuilders);
        selector = other.selector;
        sampleSizePercentage = other.sampleSizePercentage;
        trainResults = other.trainResults;
        modules = other.modules;
        weightingScheme = other.weightingScheme;
        votingScheme = other.votingScheme;
        iterationStrategy = other.iterationStrategy;
        trainTime = other.trainTime;
        testTime = other.testTime;
        predictionTime = other.predictionTime;
        trainContract = other.trainContract;
        testContract = other.testContract;
        predictionContract = other.predictionContract;
        minCheckpointInterval = other.minCheckpointInterval;
        useRandomTieBreak = other.useRandomTieBreak;
    }

    @Override
    public void setTimeLimit(final long nanoseconds) {
        trainContract = nanoseconds;
    }
}
