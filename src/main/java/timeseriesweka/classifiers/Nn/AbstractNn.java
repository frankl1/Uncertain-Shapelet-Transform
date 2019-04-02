package timeseriesweka.classifiers.Nn;

import timeseriesweka.classifiers.AdvancedAbstractClassifier.AdvancedAbstractClassifier;
import timeseriesweka.classifiers.CheckpointClassifier;
import timeseriesweka.classifiers.ContractClassifier;
import timeseriesweka.classifiers.Nn.NeighbourWeighting.NeighbourWeighter;
import timeseriesweka.classifiers.Nn.NeighbourWeighting.UniformWeighting;
import timeseriesweka.classifiers.Nn.NeighbourWeighting.WeightByDistance;
import timeseriesweka.Sampling.RandomRoundRobinSampler;
import timeseriesweka.Sampling.Sampler;
import timeseriesweka.measures.DistanceMeasure;
import utilities.*;
import weka.core.Instance;
import weka.core.Instances;
import evaluation.storage.ClassifierResults;

import java.io.*;
import java.util.*;

public abstract class AbstractNn extends AdvancedAbstractClassifier implements  CheckpointClassifier, ContractClassifier {

    private static final String CHECKPOINT_FILE_NAME = "checkpoint.ser.gzip";
    private static final String SAMPLE_SIZE_PERCENTAGE_KEY = "sampleSizePercentage";
    private static final String K_PERCENTAGE_KEY = "kPercentage";
    private static final String RANDOM_TIE_BREAK_KEY = "randomTieBreak";
    private static final String USE_EARLY_ABANDON_KEY = "earlyAbandon";
    private static final String NEIGHBOUR_WEIGHTER_KEY = "neighbourWeighter";
    private static final String SAMPLER_KEY = "sampler";
    private final List<Instance> sampledTrainInstances = new ArrayList<>();
    private final List<NearestNeighbourFinder> trainNearestNeighbourFinders = new ArrayList<>();
    private final List<Instance> originalSampledTrainInstances = new ArrayList<>();
    private final List<NearestNeighbourFinder> testNearestNeighbourFinders = new ArrayList<>();
    private double sampleSizePercentage = 1;
    private double kPercentage = 0;
    private int k;
    private int sampleSize;
    private boolean useRandomTieBreak = true;
    private boolean useEarlyAbandon = false;
    private NeighbourWeighter neighbourWeighter = new UniformWeighting();
    private Sampler sampler = new RandomRoundRobinSampler();

    public static void main(String[] args) throws Exception {
//        ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//        executorService.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
//        String datasetsDirPath = "/scratch/Datasets/TSCProblems2015/";
//        System.out.println(datasetsDirPath);
//        List<String> datasetNames = new ArrayList<>();
//        datasetNames.add("GunPoint");
//        String datasetName = datasetNames.get(0);
//        Instances trainInstances = ClassifierTools.loadData(datasetsDirPath + datasetName + "/" + datasetName + "_TRAIN.arff");
//        Instances testInstances = ClassifierTools.loadData(datasetsDirPath + datasetName + "/" + datasetName + "_TEST.arff");
//        Instances[] splitInstances = InstanceTools.resampleTrainAndTestInstances(trainInstances, testInstances, 0);
//        trainInstances = splitInstances[0];
//        testInstances = splitInstances[1];
//        Nn Nn = new Nn();
//        Nn.setSavePath("checkpoints");
//        Nn.setCheckpointing(true);
//        Nn.setSeed(0);
//        Nn.setSampleSizePercentage(1);
//        Nn.setCvTrain(true);
//        Nn.buildClassifier(trainInstances);
//        ClassifierResults testResults = Nn.getTestResults(testInstances);
//        ClassifierResults trainResults = Nn.getTrainResults();
//        System.out.print(trainResults.getAcc());
//        System.out.print(", ");
//        System.out.println(testResults.getAcc());
//        for(int i = 0; i <= trainInstances.numInstances(); i++) {
//            if(i == trainInstances.numInstances() - 2) {
//                boolean b = true;
//            }
//            Nn.setSampleSizePercentage((double) i / trainInstances.numInstances());
//            ClassifierResults testResults = trainAndTest(Nn, trainInstances, testInstances);
//            ClassifierResults trainResults = Nn.getTrainResults();
//            System.out.print(trainResults.acc);
//            System.out.print(", ");
//            System.out.println(testResults.acc);
//        }

//        for(File file : new File(datasetsPath).listFiles(new FileFilter() {
//            @Override
//            public boolean accept(final File file) {
//                return file.isDirectory();
//            }
//        })) {
//            datasetNames.add(file.getName());
//        }
//        BufferedReader reader = new BufferedReader(new FileReader("/scratch/datasetList.txt"));
//        String line;
//        while ((line = reader.readLine()) != null) {
//            datasetNames.add(line);
//        }
//        reader.close();
//        int foldIndex = 0;
//        String type = "DTW";
//        for(String datasetName : datasetNames) {
//            String datasetPath = datasetsPath + datasetName;
////            executorService.submit(new Runnable() {
////                @Override
////                public void run() {
////                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
//                    try {
////                        System.out.println("loading " + datasetName);
//                        Instances[] split = Utilities.loadSplitInstances(new File(datasetPath));
//                        Instances trainInstances = split[0];
//                        Instances testInstances = split[1];
//                        DTW1NN orig = new DTW1NN();
////                        Dtw1Nn2 orig2 = new Dtw1Nn2();
//                        Nn Nn = new Nn();
////                        Nn.setSavePath("/scratch/checkpoints/" + datasetName);
//                        Nn.setCvTrain(true);
//                        Nn.setUseEarlyAbandon(false);
//                        Nn.setUseRandomTieBreak(false);
//                        Nn.setNeighbourWeighter(WEIGHT_UNIFORM);
//                        Dtw dtw = new Dtw();
//                        Nn.setDistanceMeasure(dtw);
//                        String previousTestResult = "/run/user/33190/gvfs/smb-share:server=cmptscsvr.cmp.uea.ac.uk,share=ueatsc/Results_7_2_19/JayMovingInProgress/EEConstituentResults/" + type + "_Rn_1NN/Predictions/" + datasetName + "/testFold" + foldIndex + ".csv";
//                        String previousTrainResult = "/run/user/33190/gvfs/smb-share:server=cmptscsvr.cmp.uea.ac.uk,share=ueatsc/Results_7_2_19/JayMovingInProgress/EEConstituentResults/" + type + "_Rn_1NN/Predictions/" + datasetName + "/trainFold" + foldIndex + ".csv";
//                        BufferedReader testReader = new BufferedReader(new FileReader(previousTestResult));
//                        testReader.readLine();
//                        int testParam = Integer.parseInt(testReader.readLine());
//                        double testAcc = Double.parseDouble(testReader.readLine());
//                        testReader.close();
//                        BufferedReader trainReader = new BufferedReader(new FileReader(previousTrainResult));
//                        trainReader.readLine();
//                        int trainParam = Integer.parseInt(trainReader.readLine());
//                        double trainAcc = Double.parseDouble(trainReader.readLine());
//                        trainReader.close();
//                        dtw.setWarpingWindow((double) trainParam / 100);
//                        orig.setParamsFromParamId(trainInstances, testParam);
////                        orig2.setParamsFromParamId(trainInstances, testParam);
//                        ClassifierResults origTestResults = trainAndTest(orig, trainInstances, testInstances);
////                        ClassifierResults orig2TestResults = trainAndTest(orig2, trainInstances, testInstances);
//                        Nn.buildClassifier(trainInstances);
//                        ClassifierResults nnTestResults = Nn.getTestPrediction(trainInstances, testInstances);
//                        nnTestResults.findAllStatsOnce();
//                        dtw.setWarpingWindow((double) trainParam / 100);
//                        orig.setParamsFromParamId(trainInstances, trainParam);
////                        orig2.setParamsFromParamId(trainInstances, trainParam);
//                        ClassifierResults results = Nn.getTrainResults();
//                        results.findAllStatsOnce();
//                        double nnLoocv = results.acc;
//                        double origLoocv = orig.loocvAccAndPreds(trainInstances,  trainParam)[0];
////                        double orig2Loocv = orig2.loocvAccAndPreds(trainInstances,  trainParam)[0];
////                        System.out.println(orig2Loocv);
////                        System.out.println("---");
////                        System.out.println(nnLoocv);
////                        System.out.println("---");
//                        System.out.println(datasetName
//                            + ", " + origLoocv
////                            + ", " + orig2Loocv
//                            + ", " + nnLoocv
//                            + ", " + trainAcc
//                            + ", " + origTestResults.acc
////                            + ", " + orig2TestResults.acc
//                            + ", " + nnTestResults.acc
//                            + ", " + testAcc
//                        );
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
////                }
////            });
//        }
//        executorService.shutdown();
//        int seed = 3;
//        NearestNeighbour nearestNeighbour = new NearestNeighbour();
//        nearestNeighbour.setUseRandomTieBreak(false);
//        nearestNeighbour.setUseEarlyAbandon(false);
//        nearestNeighbour.setSampleSizePercentage(1);
//        nearestNeighbour.setCvTrain(true);
//        Dtw dtw = new Dtw();
//        dtw.setWarpingWindow(0.02);
//        nearestNeighbour.setDistanceMeasure(dtw);
//        nearestNeighbour.setNeighbourWeighter(WEIGHT_UNIFORM);
//        nearestNeighbour.setSeed(seed);
//        String datasetName = "GunPoint";
//        String checkpointDirPath = "/scratch/checkpoints/" + datasetName;
//        new File(checkpointDirPath).mkdirs();
////        nearestNeighbour.setSavePath(checkpointDirPath);
//        String datasetPath = "/scratch/Datasets/TSCProblems2015/" + datasetName;
//        Instances[] split = Utilities.loadSplitInstances(new File(datasetPath));
//        Instances trainInstances = split[0];
//        Instances testInstances = split[1];
//        split = InstanceTools.resampleTrainAndTestInstances(trainInstances, testInstances, seed);
//        trainInstances = split[0];
//        testInstances = split[0];
//        ClassifierResults trainResults = nearestNeighbour.getTrainResults(trainInstances);
//        trainResults.findAllStatsOnce();
//        ClassifierResults testResults = nearestNeighbour.getTestPrediction(testInstances);
//        testResults.findAllStatsOnce();
//        System.out.println(trainResults.acc);
//        System.out.println(testResults.acc);
    }


    @Override
    public void setSavePath(String path) {
        super.setSavePath(path);
        this.checkpointFilePath = new File(path).getPath() + "/" + CHECKPOINT_FILE_NAME;
    }


    // todo make contract use linear regression (order 2)


    @Override
    public void copyFromSerObject(final Object obj) throws Exception {
        super.copyFromSerObject(obj);
        AbstractNn other = (AbstractNn) obj;
        kPercentage = other.kPercentage;
        useRandomTieBreak = other.useRandomTieBreak;
        useEarlyAbandon = other.useEarlyAbandon;
        neighbourWeighter = other.neighbourWeighter;
        sampleSizePercentage = other.sampleSizePercentage;
        sampler = other.sampler;
        sampledTrainInstances.addAll(other.sampledTrainInstances);
        originalSampledTrainInstances.addAll(other.originalSampledTrainInstances);
        trainNearestNeighbourFinders.addAll(other.trainNearestNeighbourFinders);
        testNearestNeighbourFinders.addAll(other.testNearestNeighbourFinders);
        k = other.k;
        sampleSize = other.sampleSize;
    }

    @Override
    public String[] getOptions() {
        List<String> strings = new ArrayList<>();
        strings.add(SAMPLE_SIZE_PERCENTAGE_KEY);
        strings.add(String.valueOf(sampleSizePercentage));
        strings.add(K_PERCENTAGE_KEY);
        strings.add(String.valueOf(kPercentage));
        strings.add(RANDOM_TIE_BREAK_KEY);
        strings.add(String.valueOf(useRandomTieBreak));
        strings.add(USE_EARLY_ABANDON_KEY);
        strings.add(String.valueOf(useEarlyAbandon));
        strings.add(NEIGHBOUR_WEIGHTER_KEY);
        strings.add(neighbourWeighter.getClass().getSimpleName());
        strings.add(SAMPLER_KEY);
        strings.add(sampler.getClass().getSimpleName());
        strings.addAll(Arrays.asList(super.getOptions()));
        strings.addAll(Arrays.asList(getDistanceMeasureInstance().getOptions()));
        return strings.toArray(new String[0]);
    }

    @Override
    public boolean setOption(final String key, final String value) {
        if(!super.setOption(key, value)) {
            if(key.equals(SAMPLE_SIZE_PERCENTAGE_KEY)) {
                setSampleSizePercentage(Double.parseDouble(value));
            } else if(key.equals(K_PERCENTAGE_KEY)) {
                setKPercentage(Double.parseDouble(value));
            } else if(key.equals(USE_EARLY_ABANDON_KEY)) {
                setUseEarlyAbandon(Boolean.parseBoolean(value));
            } else if(key.equals(RANDOM_TIE_BREAK_KEY)) {
                setUseRandomTieBreak(Boolean.parseBoolean(value));
            } else if(key.equals(NEIGHBOUR_WEIGHTER_KEY)) {
                if(value.equals(WeightByDistance.class.getSimpleName())) {
                    setNeighbourWeighter(new WeightByDistance());
                } else if(value.equals(UniformWeighting.class.getSimpleName())) {
                    setNeighbourWeighter(new UniformWeighting());
                } else {
                    return false;
                }
            } else if(key.equals(SAMPLER_KEY)) {
                if(value.equals(RandomRoundRobinSampler.class.getSimpleName())) {
                    setSampler(new RandomRoundRobinSampler());
//                } else if(value.equals(UniformWeighting.class.getSimpleName())) {
//                    sampler = ;
                } else {
                    return false;
                }
            } else {
                return getDistanceMeasureInstance().setOption(key, value);
            }
        }
        return true;
    }

    public void setUseEarlyAbandon(final boolean useEarlyAbandon) {
        this.useEarlyAbandon = useEarlyAbandon;
    }

    public void setUseRandomTieBreak(final boolean useRandomTieBreak) {
        this.useRandomTieBreak = useRandomTieBreak;
    }

    @Override
    public void buildClassifier(Instances trainInstances) throws Exception {
        resumeFromCheckpoint();
        trainTimeStamp = System.nanoTime();
        if(resetOnTrain) {
            trainTime = 0;
            this.originalTrainInstances = trainInstances;
            trainInstances = originalTrainInstances;
            sampler.setInstances(trainInstances);
            originalSampledTrainInstances.clear();
            trainNearestNeighbourFinders.clear();
            if(cvTrain) {
                for(Instance trainInstance : trainInstances) {
                    trainNearestNeighbourFinders.add(new NearestNeighbourFinder(trainInstance));
                }
            }
            k = 1 + (int) kPercentage * originalTrainInstances.numInstances();
            trainCheckpoint();
        }
        sampleSize = (int) (trainInstances.numInstances() * sampleSizePercentage);
        while (originalSampledTrainInstances.size() < sampleSize && withinTrainContract()) {
            if(!sampler.hasNext()) {
                throw new IllegalStateException("Cannot sample another instance, this should never happen!");
            }
            Instance sampledInstance = sampler.next();
            originalSampledTrainInstances.add(sampledInstance);
            if (cvTrain) {
                for(NearestNeighbourFinder nearestNeighbourFinder : trainNearestNeighbourFinders) {
                    if(!nearestNeighbourFinder.getInstance().equals(sampledInstance)) {
                        nearestNeighbourFinder.addNeighbour(sampledInstance);
                    }
                }
            }
            trainResults = null;
            trainCheckpoint();
        }
        if(trainResults == null) {
            updateTrainTime();
            if(cvTrain) {
                trainResults = getResults(trainNearestNeighbourFinders);
            }
            checkpoint(true);
            if(cvTrain && trainFilePath != null) {
                Utilities.mkdirParent(new File(trainFilePath));
                trainResults.writeFullResultsToFile(trainFilePath);
            }
        }
    }

    private ClassifierResults getResults(List<NearestNeighbourFinder> nearestNeighbourFinders) throws Exception {
        ClassifierResults results = new ClassifierResults();
        for (int i = 0; i < nearestNeighbourFinders.size(); i++) {
            NearestNeighbourFinder nearestNeighbourFinder = nearestNeighbourFinders.get(i);
            double classValue = nearestNeighbourFinder.getInstance().classValue();
            long predictionTimeStamp = System.nanoTime();
            double[] predictions = nearestNeighbourFinder.predict();
            long predictionTime = System.nanoTime() - predictionTimeStamp;
            results.addPrediction(classValue, predictions, Utilities.argMax(predictions, random), predictionTime, null);
        }
        setResultsMetaData(nearestNeighbourFinders.get(0).getInstance().numClasses(), results);
        return results;
    }

    public ClassifierResults getTestResults(Instances testInstances) throws Exception {
        resumeFromCheckpoint();
        testTimeStamp = System.nanoTime();
        if(resetOnTest) {
            testTime = 0;
            sampledTrainInstances.clear();
            testNearestNeighbourFinders.clear();
            sampledTrainInstances.addAll(originalSampledTrainInstances);
            for(Instance testInstance : testInstances) {
                testNearestNeighbourFinders.add(new NearestNeighbourFinder(testInstance));
            }
            testCheckpoint();
        }
        while (!sampledTrainInstances.isEmpty() && withinTestContract()) {
            testResults = null;
            Instance sampledTrainInstance = sampledTrainInstances.remove(random.nextInt(sampledTrainInstances.size()));
            for(NearestNeighbourFinder nearestNeighbourFinder : testNearestNeighbourFinders) {
                nearestNeighbourFinder.addNeighbour(sampledTrainInstance);
            }
            testCheckpoint();
        }
        if(testResults == null) {
            testResults = getResults(testNearestNeighbourFinders);
            checkpoint(true);
        }
        return testResults;
    }

    @Override
    public String toString() {
        return getDistanceMeasureInstance().toString() + "-" + getClass().getSimpleName().toUpperCase();
    }

    protected abstract DistanceMeasure getDistanceMeasureInstance();

    public Sampler getSampler() {
        return sampler;
    }

    public void setSampler(final Sampler sampler) {
        this.sampler = sampler;
        sampler.setRandom(random);
    }

    private int getSampleSize() {
        return (int) (sampleSizePercentage * originalTrainInstances.numInstances());
    }

    public double getSampleSizePercentage() {
        return sampleSizePercentage;
    }

    public void setSampleSizePercentage(final double percentage) {
        Utilities.percentageCheck(percentage);
        this.sampleSizePercentage = percentage;

    }

    public double getKPercentage() {
        return kPercentage;
    }

    public void setKPercentage(final double percentage) {
        Utilities.percentageCheck(percentage);
        this.kPercentage = percentage;
    }

    public boolean usesRandomTieBreak() {
        return useRandomTieBreak;
    }

    @Override
    public double classifyInstance(final Instance testInstance) throws Exception {
        return Utilities.argMax(distributionForInstance(testInstance), random);
    }

    @Override
    public double[] distributionForInstance(final Instance testInstance) throws Exception {
        predictionTimeStamp = System.nanoTime();
        predictionTime = 0;
        NearestNeighbourFinder nearestNeighbourFinder = new NearestNeighbourFinder(testInstance);
        List<Instance> sampledTrainInstances = new ArrayList<>(originalSampledTrainInstances);
        while (!sampledTrainInstances.isEmpty() && withinPredictionContract()) {
            Instance sampledTrainInstance = sampledTrainInstances.remove(random.nextInt(sampledTrainInstances.size()));
            nearestNeighbourFinder.addNeighbour(sampledTrainInstance);
            updatePredictionTime();
        }
        double[] prediction = nearestNeighbourFinder.predict();
        Utilities.normalise(prediction);
        return prediction;
    }

    public boolean usesEarlyAbandon() {
        return useEarlyAbandon;
    }

    public NeighbourWeighter getNeighbourWeighter() {
        return neighbourWeighter;
    }

    public void setNeighbourWeighter(final NeighbourWeighter neighbourWeighter) {
        this.neighbourWeighter = neighbourWeighter;
    }

    private class NearestNeighbourFinder implements Serializable {
        private Instance instance;
        private TreeMap<Double, List<Instance>> nearestNeighbours = new TreeMap<>();
        private int neighbourCount = 0;

        public NearestNeighbourFinder(Instance instance) {
            this.instance = instance;
        }

        public Instance getInstance() {
            return instance;
        }

        public double addNeighbour(Instance neighbour) {
            double distance = getDistanceMeasureInstance().distance(instance, neighbour, findCutOff());
            addNeighbour(neighbour, distance);
            return distance;
        }

        private double findCutOff() {
            if (useEarlyAbandon) {
                return nearestNeighbours.lastKey();
            } else {
                return Double.POSITIVE_INFINITY;
            }
        }

        public void addNeighbour(Instance neighbour, double distance) {
            List<Instance> neighbours = nearestNeighbours.computeIfAbsent(distance, key -> new ArrayList<>());
            neighbours.add(neighbour);
            neighbourCount++;
            if(neighbourCount > k) {
                Map.Entry<Double, List<Instance>> furthestNeighboursEntry = nearestNeighbours.lastEntry();
                if (neighbourCount - k >= furthestNeighboursEntry.getValue().size()) {
                    neighbourCount -= nearestNeighbours.pollLastEntry().getValue().size();
                }
            }
        }

        public double[] predict() {
            double[] predictions = new double[instance.numClasses()];
            Iterator<Map.Entry<Double, List<Instance>>> iterator = nearestNeighbours.entrySet().iterator();
            if(!iterator.hasNext()) {
                predictions[random.nextInt(predictions.length)]++;
                return predictions;
            }
            Map.Entry<Double, List<Instance>> entry = iterator.next();
            neighbourCount = 0;
            double distance = entry.getKey();
            List<Instance> neighbours = entry.getValue();
            while(neighbourCount + neighbours.size() <= k) {
                for(Instance neighbour : neighbours) {
                    predictions[(int) neighbour.classValue()] += neighbourWeighter.weight(distance);
                }
                neighbourCount += neighbours.size();
                if(neighbourCount < k) {
                    entry = iterator.next();
                    neighbours = entry.getValue();
                    distance = entry.getKey();
                }
            }
            if(neighbourCount < k) {
                neighbours = new ArrayList<>(neighbours);
                while (neighbourCount < k) {
                    Instance neighbour = neighbours.remove(0);
                    predictions[(int) neighbour.classValue()] += neighbourWeighter.weight(distance);
                    neighbourCount++;
                }
            }
            ArrayUtilities.normalise(predictions);
            return predictions;
        }
    }
}
