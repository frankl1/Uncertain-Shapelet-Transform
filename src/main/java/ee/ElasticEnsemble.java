package timeseriesweka.classifiers.ensembles.ee;

import timeseriesweka.classifiers.ContractCheckpointClassifier;
import timeseriesweka.classifiers.Nn;
import timeseriesweka.classifiers.ensembles.ee.CandidateSelector.BestCandidatePerClassifier;
import timeseriesweka.classifiers.ensembles.ee.CandidateSelector.CandidateSelector;
import timeseriesweka.classifiers.ensembles.ee.Indexing.CombinedIndexer;
import timeseriesweka.classifiers.ensembles.ee.Indexing.IndexerObtainer;
import timeseriesweka.classifiers.ensembles.ee.Indexing.ListElementObtainer;
import timeseriesweka.classifiers.ensembles.ee.Interpolation.LinearInterpolator;
import timeseriesweka.classifiers.ensembles.ee.Iteration.AbstractIterator;
import timeseriesweka.classifiers.ensembles.ee.Iteration.Iterator;
import timeseriesweka.classifiers.ensembles.ee.Iteration.LinearIterator;
import timeseriesweka.classifiers.ensembles.ee.Iteration.RoundRobinIterator;
import timeseriesweka.classifiers.ensembles.ee.Parameter.Parameter;
import timeseriesweka.classifiers.ensembles.ee.Parameter.ParameterInterface;
import timeseriesweka.classifiers.ensembles.ee.Parameter.PopulatedParameter;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.lcss.Lcss;
import utilities.*;
import utilities.generic_storage.Box;
import utilities.instances.Folds;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.SimpleBatchFilter;

import java.io.*;
import java.util.*;

import static utilities.Utilities.populationStandardDeviation;

public class ElasticEnsemble implements ContractCheckpointClassifier, Classifier, Reproducible, Serializable {

    private static final String SAVE_FILENAME = "elasticEnsemble.ser";
    private final List<ConstituentObtainer> constituentObtainers = new LinkedList<>();
    private final List<Constituent> constituents = new ArrayList<>();
    private final Random random = new Random();
    private final Map<SimpleBatchFilter, Folds> filterFoldsMap = new HashMap<>();
    private CandidateSelector<ConstituentCandidate> candidateSelector = new BestCandidatePerClassifier<>(new SerializedComparator<ConstituentCandidate>() {
        @Override
        public int compare(ConstituentCandidate a, ConstituentCandidate b) {
            ClassifierResults aResults = a.getResults();
            ClassifierResults bResults = b.getResults();
            double diff = bResults.acc - aResults.acc;
            if (diff == 0) {
                return 0;
            } else if (diff < 0) {
                return 1;
            } else {
                return -1;
            }
        }
    }, ConstituentCandidate::getConstituent);
    private final Map<SimpleBatchFilter, List<Candidate>> filterCandidateMap = new HashMap<>();
    private final List<Candidate> predefinedCandidates = new ArrayList<>();
    private AbstractIterator<Constituent> constituentIterator = new RoundRobinIterator<>();
    private long seed = 0;

    private static class Candidate implements Serializable {
        private final Classifier classifier;

        public Classifier getClassifier() {
            return classifier;
        }

        public ClassifierResults getResults() {
            return results;
        }

        public Candidate(Classifier classifier, ClassifierResults results) {
            this.classifier = classifier;
            this.results = results;
        }

        private final ClassifierResults results;

    }

    private static class ConstituentCandidate extends Candidate {
        private final Constituent constituent;

        public ConstituentCandidate(Classifier classifier, ClassifierResults results, Constituent constituent) {
            super(classifier, results);
            this.constituent = constituent;
        }

        public Constituent getConstituent() {
            return constituent;
        }
    }

    public ElasticEnsemble() {
        //setSavePath("ee/" + System.nanoTime());
    }


    public static void main(String[] args) throws Exception {
//        final TrainTestSplit dataset = loadTscDataset("TSCProblems2018", "Coffee");
//        ElasticEnsemble elasticEnsemble = new ElasticEnsemble();
//        elasticEnsemble.setMinCheckpointInterval(1, TimeUnit.HOURS);
//        elasticEnsemble.add(originalConfiguration); // todo make this setup better, perhaps builder pattern
//        elasticEnsemble.setSeed(0);
//        ClassifierResults classifierResults = Utilities.run(elasticEnsemble, dataset);
//        classifierResults.setNumClasses(dataset.getTest().numClasses());
//        classifierResults.setNumInstances(dataset.getTest().numInstances());
//        classifierResults.findAllStatsOnce();
//        System.out.println(classifierResults);

//        Queue<Thread> threadQueue = new LinkedList<>();
//        for(int i = 0; i < ; i++) {
//            int finalI = i;
//            Thread thread = new Thread(() -> {
//                try {
//                    run(dataset, finalI);
//                    threadQueue.remove(Thread.currentThread());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });
//            threadQueue.add(thread);
//            thread.start();
//        }
    }

    @Override
    public void copyFromSerObject(Object obj) {
        ElasticEnsemble other = (ElasticEnsemble) obj;
        filterFoldsMap.clear();
        filterFoldsMap.putAll(other.filterFoldsMap);
        filterCandidateMap.clear();
        filterCandidateMap.putAll(other.filterCandidateMap);
        constituentObtainers.clear();
        predefinedCandidates.clear();
        predefinedCandidates.addAll(other.predefinedCandidates);
        constituentObtainers.addAll(other.constituentObtainers);
        setConstituentIterator(other.getConstituentIterator());
        setCandidateSelector(other.getCandidateSelector());
        setSeed(other.seed);
    }

    public AbstractIterator<Constituent> getConstituentIterator() {
        return constituentIterator;
    }

    public void setConstituentIterator(AbstractIterator<Constituent> constituentIterator) {
        this.constituentIterator = constituentIterator;
    }

    public CandidateSelector<ConstituentCandidate> getCandidateSelector() {
        return candidateSelector;
    }

    public void setCandidateSelector(CandidateSelector<ConstituentCandidate> candidateSelector) {
        this.candidateSelector = candidateSelector;
    }

    private static class DistanceMeasureClassifierHolder<A extends DistanceMeasure, B extends Classifier> {
        private final A distanceMeasure;

        public A getDistanceMeasure() {
            return distanceMeasure;
        }

        public B getClassifier() {
            return classifier;
        }

        private final B classifier;

        private DistanceMeasureClassifierHolder(final A distanceMeasure, final B classifier) {
            this.distanceMeasure = distanceMeasure;
            this.classifier = classifier;
        }
    }

    private static Iterator<Classifier> makeLcssNnGenerator(double populationStandardDeviation) {
        final ParameterInterface<Lcss, Double> warpingWindow = new ParameterInterface<Lcss, Double>() {
            @Override
            public void setParameterValue(Lcss object, Double value) {
                object.setWarpingWindow(value);
            }

            @Override
            public Double getParameterValue(Lcss object) {
                return object.getWarpingWindow();
            }
        };
        final ParameterInterface<Lcss, Double> tolerance = new ParameterInterface<Lcss, Double>() {
            @Override
            public void setParameterValue(Lcss object, Double value) {
                object.setTolerance(value);
            }

            @Override
            public Double getParameterValue(Lcss object) {
                return object.getTolerance();
            }
        };

        final Box<Lcss> lcssBox = new Box<>();
        final Box<Nn> nnBox = new Box<>();

        final PopulatedParameter<Double> wwr = new PopulatedParameter<>(new Parameter<Lcss, Double>(lcssBox, warpingWindow), new LinearInterpolator(0, 1, 10));
        final PopulatedParameter<Double> tol = new PopulatedParameter<>(new Parameter<>(lcssBox, tolerance), new LinearInterpolator(0.2 * populationStandardDeviation, populationStandardDeviation, 10));


        final CombinedIndexer params = new CombinedIndexer();
        params.getIndexers().add(wwr);
        params.getIndexers().add(tol);

        return new LinearIterator<>(new IndexerObtainer<>(params, (Supplier<Classifier>) () -> {
            Nn nn = new Nn();
            Lcss lcss = new Lcss();
            nn.setDistanceMeasure(lcss);
            lcssBox.set(lcss);
            nnBox.set(nn);
            return nn;
        }));
    }

//    private static Iterator<Classifier> makeDtwNnGenerator() {
//        Box<Dtw> dtwBox = new Box<>();
//        ParameterInterface<Dtw, Double> warpingWindow = new ParameterInterface<Dtw, Double>() {
//            @Override
//            public void setParameterValue(Dtw object, Double value) {
//                object.setWarpingWindow(value);
//            }
//
//            @Override
//            public Double getParameterValue(Dtw object) {
//                return object.getWarpingWindow();
//            }
//        };
//        PopulatedParameter<Dtw, Double> warpingWindowRange = new PopulatedParameter<Dtw, Double>(dtwBox, warpingWindow);
//        warpingWindowRange.getValues().addAll(arrayToList(interpolate(0, 1, 100)));
//        return makeNnDistanceMeasureIterator<Classifier>(() -> {
//            Dtw dtw = new Dtw();
//            dtwBox.set(dtw);
//            return dtw;
//        }, warpingWindowRange);
//    }
//
//    private static Iterator<Classifier> makeMsmNnGenerator(double populationStandardDeviation) {
//
//        ParameterInterface<Msm, Double> warpingWindow = new ParameterInterface<Msm, Double>() {
//            @Override
//            public void setParameterValue(Msm object, Double value) {
//                object.setWarpingWindow(value);
//            }
//
//            @Override
//            public Double getParameterValue(Msm object) {
//                return object.getWarpingWindow();
//            }
//        };
//        Parameter<Msm, Double> cost = new Parameter<Msm, Double>() {
//            @Override
//            public void set(Double value) {
//                .setCost(value);
//            }
//
//            @Override
//            public Double get(Double object) {
//                return object.getCost();
//            }
//        };
//        Box<Msm> msmBox = new Box<>();
//        double[] msmParams = new double[]{
//            // <editor-fold defaultstate="collapsed" desc="hidden for space">
//            0.01,
//            0.01375,
//            0.0175,
//            0.02125,
//            0.025,
//            0.02875,
//            0.0325,
//            0.03625,
//            0.04,
//            0.04375,
//            0.0475,
//            0.05125,
//            0.055,
//            0.05875,
//            0.0625,
//            0.06625,
//            0.07,
//            0.07375,
//            0.0775,
//            0.08125,
//            0.085,
//            0.08875,
//            0.0925,
//            0.09625,
//            0.1,
//            0.136,
//            0.172,
//            0.208,
//            0.244,
//            0.28,
//            0.316,
//            0.352,
//            0.388,
//            0.424,
//            0.46,
//            0.496,
//            0.532,
//            0.568,
//            0.604,
//            0.64,
//            0.676,
//            0.712,
//            0.748,
//            0.784,
//            0.82,
//            0.856,
//            0.892,
//            0.928,
//            0.964,
//            1,
//            1.36,
//            1.72,
//            2.08,
//            2.44,
//            2.8,
//            3.16,
//            3.52,
//            3.88,
//            4.24,
//            4.6,
//            4.96,
//            5.32,
//            5.68,
//            6.04,
//            6.4,
//            6.76,
//            7.12,
//            7.48,
//            7.84,
//            8.2,
//            8.56,
//            8.92,
//            9.28,
//            9.64,
//            10,
//            13.6,
//            17.2,
//            20.8,
//            24.4,
//            28,
//            31.6,
//            35.2,
//            38.8,
//            42.4,
//            46,
//            49.6,
//            53.2,
//            56.8,
//            60.4,
//            64,
//            67.6,
//            71.2,
//            74.8,
//            78.4,
//            82,
//            85.6,
//            89.2,
//            92.8,
//            96.4,
//            100// </editor-fold>
//        };
//        PopulatedParameter<Double> costRange = new PopulatedParameter<Double>(cost, new ListElementObtainer<Double>(arrayToList(msmParams)));
//
//
//        PopulatedParameter<Msm, Double> warpingWindowRange = new PopulatedParameter<Msm, Double>(msmBox, warpingWindow);
//        warpingWindowRange.getValues().addAll(arrayToList(interpolate(1, 1, 1))); // bake off didn't have msm warping window
//        PopulatedParameter<Msm, Double> costRange = new PopulatedParameter<Msm, Double>(msmBox, cost);
//        for(double val : msmParams) {
//            costRange.getValues().add(val);
//        }
//        return makeNnDistanceMeasureIterator<Classifier>(() -> {
//            Msm msm = new Msm();
//            msmBox.set(msm);
//            return msm;
//        }, warpingWindowRange, costRange);
//    }
//
//    private static Iterator<Classifier> makeTweNnGenerator() {
//        Box<Twe> tweBox = new Box<>();
//        ParameterInterface<Twe, Double> lambda = new ParameterInterface<Twe, Double>() { // todo rename lambda to something more intuitive
//            @Override
//            public void setParameterValue(Twe object, Double value) {
//                object.setLambda(value);
//            }
//
//            @Override
//            public Double getParameterValue(Twe object) {
//                return object.getLambda(); // todo do we really need a getter?? Don't like typing it every time but might be useful
//            }
//        };
//        ParameterInterface<Twe, Double> nu = new ParameterInterface<Twe, Double>() { // todo rename nu to something more intuitive!
//            @Override
//            public void setParameterValue(Twe object, Double value) {
//                object.setNu(value);
//            }
//
//            @Override
//            public Double getParameterValue(Twe object) {
//                return object.getNu();
//            }
//        };
//        PopulatedParameter<Twe, Double> lambdaRange = new PopulatedParameter<Twe, Double>(tweBox, lambda);
//        double[] lambdaValues = new double[] {
//            // <editor-fold defaultstate="collapsed" desc="hidden for space">
//            0,
//            0.011111111,
//            0.022222222,
//            0.033333333,
//            0.044444444,
//            0.055555556,
//            0.066666667,
//            0.077777778,
//            0.088888889,
//            0.1,// </editor-fold>
//        };
//        double[] nuValues = new double[] {
//            // <editor-fold defaultstate="collapsed" desc="hidden for space">
//            0,
//            0.011111111,
//            0.022222222,
//            0.033333333,
//            0.044444444,
//            0.055555556,
//            0.066666667,
//            0.077777778,
//            0.088888889,
//            0.1,// </editor-fold>
//        };
//        for(double val : lambdaValues) {
//            lambdaRange.getValues().add(val);
//        }
//        PopulatedParameter<Twe, Double> nuRange = new PopulatedParameter<Twe, Double>(tweBox, nu);
//        for(double val : nuValues) {
//            nuRange.getValues().add(val);
//        }
//        return makeNnDistanceMeasureIterator<Classifier>(() -> {
//            Twe twe = new Twe();
//            tweBox.set(twe);
//            return twe;
//        }, nuRange, lambdaRange);
//    }
//
//    private static Iterator<Classifier> makeErpNnGenerator(double populationStandardDeviation) {
//        Box<Erp> erpBox = new Box<>();
//        ParameterInterface<Erp, Double> warpingWindow = new ParameterInterface<Erp, Double>() {
//            @Override
//            public void setParameterValue(Erp object, Double value) {
//                object.setWarpingWindow(value);
//            }
//
//            @Override
//            public Double getParameterValue(Erp object) {
//                return object.getWarpingWindow();
//            }
//        };
//        ParameterInterface<Erp, Double> penalty = new ParameterInterface<Erp, Double>() {
//            @Override
//            public void setParameterValue(Erp object, Double value) {
//                object.setPenalty(value);
//            }
//
//            @Override
//            public Double getParameterValue(Erp object) {
//                return object.getPenalty();
//            }
//        };
//        PopulatedParameter<Erp, Double> warpingWindowRange = new PopulatedParameter<Erp, Double>(erpBox, warpingWindow);
//        warpingWindowRange.getValues().addAll(arrayToList(interpolate(0, 0.25, 10)));
//        PopulatedParameter<Erp, Double> penaltyRange = new PopulatedParameter<Erp, Double>(erpBox, penalty);
//        penaltyRange.getValues().addAll(arrayToList(interpolate(0.2 * populationStandardDeviation, populationStandardDeviation, 10)));
//        return makeNnDistanceMeasureIterator<Classifier>(() -> {
//            Erp erp = new Erp();
//            erpBox.set(erp);
//            return erp;
//        }, penaltyRange, warpingWindowRange);
//    }
//
//    private static Iterator<Classifier> makeEuclideanNnGenerator() {
//        return makeNnDistanceMeasureIterator<Classifier>(Euclidean::new);
//    }
//
//    private static Iterator<Classifier> makeWdtwNnGenerator() {
//        Box<Wdtw> wdtwBox = new Box<>();
//        ParameterInterface<Wdtw, Double> warpingWindow = new ParameterInterface<Wdtw, Double>() {
//            @Override
//            public void setParameterValue(Wdtw object, Double value) {
//                object.setWarpingWindow(value);
//            }
//
//            @Override
//            public Double getParameterValue(Wdtw object) {
//                return object.getWarpingWindow();
//            }
//        };
//        ParameterInterface<Wdtw, Double> weight = new ParameterInterface<Wdtw, Double>() {
//            @Override
//            public void setParameterValue(Wdtw object, Double value) {
//                object.setWeight(value);
//            }
//
//            @Override
//            public Double getParameterValue(Wdtw object) {
//                return object.getWeight();
//            }
//        };
//        PopulatedParameter<Wdtw, Double> warpingWindowRange = new PopulatedParameter<Wdtw, Double>(wdtwBox, warpingWindow);
//        warpingWindowRange.getValues().addAll(arrayToList(interpolate(1, 1, 1))); // bake off didn't have warping window for wdtw
//        PopulatedParameter<Wdtw, Double> weightRange = new PopulatedParameter<Wdtw, Double>(wdtwBox, weight);
//        weightRange.getValues().addAll(arrayToList(interpolate(0, 1, 100)));
//        return makeNnDistanceMeasureIterator<Classifier>(() -> {
//            Wdtw wdtw = new Wdtw();
//            wdtwBox.set(wdtw);
//            return wdtw;
//        }, weightRange, warpingWindowRange);
//    }
//
//    private static Iterator<Classifier> makeNnDistanceMeasureIterator<Classifier>(Supplier<DistanceMeasure> distanceMeasureSupplier,
//                                                                         PopulatedParameter<?, ?>... populatedParameters) {
//        Box<DistanceMeasure> distanceMeasureBox = new Box<>();
//        Box<Nn> nnBox = new Box<>();
//        return makeDistanceMeasureIterator<Classifier>(() -> {
//            Nn nn = new Nn();
//            nnBox.set(nn);
//            return nn;
//        }, distanceMeasureSupplier, populatedParameters);
//    }
//
//    private static Iterator<Classifier> makeDistanceMeasureIterator<Classifier>(Supplier<DistanceMeasureClassifier> distanceMeasureClassifierSupplier,
//                                                                       Supplier<DistanceMeasure> distanceMeasureSupplier,
//                                                                       PopulatedParameter<?, ?>... populatedParameters) {
//        Box<DistanceMeasure> distanceMeasureBox = new Box<>();
//        Box<DistanceMeasureClassifier> distanceMeasureClassifierBox = new Box<>();
//        ParameterRangeSet
//        SingleParameterIterator<Classifier> singleParameterIterator<Classifier> = new SingleParameterIterator<Classifier>(parameterRangeSet) {
//            @Override
//            protected void setup() {
//                distanceMeasureBox.set(distanceMeasureSupplier.supply());
//                distanceMeasureClassifierBox.set(distanceMeasureClassifierSupplier.supply());
//            }
//
//            @Override
//            protected Classifier generate() {
//                DistanceMeasureClassifier distanceMeasureClassifier = distanceMeasureClassifierBox.get();
//                DistanceMeasure distanceMeasure = distanceMeasureBox.get();
//                distanceMeasureClassifier.setDistanceMeasure(distanceMeasure);
//                return distanceMeasureClassifier;
//            }
//
//            @Override
//            public void setSeed(long seed) {
//
//            }
//        };
//        for(PopulatedParameter<?, ?> populatedParameter : populatedParameters) {
//            singleParameterIterator<Classifier>.getParameterRangeSet().getParameterRanges().add(populatedParameter);
//        }
//        return singleParameterIterator<Classifier>;
//    }

//    public final static ConstituentObtainer originalConfiguration = new ConstituentObtainer() {
//        @Override
//        public Collection<Constituent> obtain(Instances instances) {
//            List<Constituent> constituentList = new ArrayList<>();
//            SimpleBatchFilter firstDerivativeFilter = new DerivativeFilter(); // first derivative filter
//            double populationStandardDeviation = populationStandardDeviation(instances);
//            constituentList.add(new Constituent(makeErpNnGenerator(populationStandardDeviation)));
//            constituentList.add(new Constituent(makeMsmNnGenerator(populationStandardDeviation)));
//            constituentList.add(new Constituent(makeDtwNnGenerator()));
//            constituentList.add(new Constituent(makeDtwNnGenerator(), firstDerivativeFilter));
//            constituentList.add(new Constituent(makeWdtwNnGenerator()));
//            constituentList.add(new Constituent(makeWdtwNnGenerator(), firstDerivativeFilter));
//            constituentList.add(new Constituent(makeLcssNnGenerator(populationStandardDeviation)));
//            constituentList.add(new Constituent(makeTweNnGenerator()));
//            constituentList.add(new Constituent(makeEuclideanNnGenerator()));
//            return constituentList;
//        }
//    };

//    private void writeResults(int id, EnsembleModule module) throws IOException {
//        Gson gson = new Gson();
//        File resultsDir = new File(PARALLEL_RESULTS_DIR_PATH);
//        resultsDir.mkdirs();
//        File resultFile = new File(resultsDir, id + ".json");
//        Writer writer = new FileWriter(resultFile);
//        BufferedWriter bufferedWriter = new BufferedWriter(writer);
//        bufferedWriter.write(gson.toJson(module));
//        bufferedWriter.close();
//        writer.close();
//    }

    public ElasticEnsemble add(ConstituentObtainer constituentObtainer) {
        constituentObtainers.add(constituentObtainer);
        return this;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    private void fromOldPastResults(final Instances instances, final int foldIndex) throws IOException {
        String dirPath = "EEConstituentResults";
        File resultsDir = new File(dirPath);
        String datasetName = instances.relationName();
        File[] constituentDirs = resultsDir.listFiles();
        Collection<Constituent> constituents = new ArrayList<>();
        if(constituentDirs != null) {
            for(File constituentDir : constituentDirs) {
                File datasetDir = new File(new File(constituentDir, "Predictions"), datasetName);
                if(datasetDir.exists() && datasetDir.isDirectory()) {
                    File trainResults = new File(datasetDir, "trainFold" + foldIndex + ".csv");
//                            File testResults = new File(datasetDir, "testFold" + foldIndex + ".csv");
                    if(!trainResults.exists() || !trainResults.isFile()) {
                        throw new IllegalArgumentException("File does not exist or is not a file");
                    }
                    BufferedReader reader = new BufferedReader(new FileReader(trainResults));
                    reader.readLine(); // discard info line
                    String parameterCombinationIndexString = reader.readLine();
                    String accuracyString = reader.readLine();
                    reader.close();

                    ClassifierResults results = new ClassifierResults(trainResults.getPath());
//                    Candidate candidate = new Candidate(classifier, results);
//                    predefinedCandidates.add(candidate);
                }
            }
        }
    }

    private static Classifier getClassifierByString(String identifier, Instances instances) {
        identifier = identifier.toLowerCase();
        if(identifier.contains("MSM")) {
            double populationStandardDeviation = populationStandardDeviation(instances);
//            Iterator<Classifier> classifierGenerator = makeMsmNnGenerator(populationStandardDeviation);
//            classifierGenerator.
        }
        return null;
    }

    @Override
    public void buildClassifier(Instances trainInstances) throws Exception {
        // reset any iterators, etc, to initial state
        List<Constituent> constituentList = new LinkedList<>();
        for(ConstituentObtainer constituentObtainer : constituentObtainers) {
            constituentList.addAll(constituentObtainer.obtain(trainInstances));
        }
        candidateSelector.reset();
        random.setSeed(seed);
        candidateSelector.setSeed(seed);
        filterCandidateMap.clear();
        constituentIterator.setIndexedObtainer(new ListElementObtainer<>(constituentList));
        for(Constituent constituent : constituentList) {
            Iterator<Classifier> classifierGenerator = constituent.getClassifierIterator();
            classifierGenerator.reset();
            classifierGenerator.setSeed(seed);
        }
        // if checkpointing enabled
        if(isCheckpointing()) {
            System.out.println("Checkpointing enabled");
            // load previous work from file
            try {
                loadFromRelativeFile(SAVE_FILENAME);
            } catch (Exception e) {
                // file not found / error occurred
                // must be checkpointing but not have a checkpoint file yet - i.e. first checkpoint run
                System.out.println("First checkpoint!");
            }
        }
        startContract();
        // while contract not exceeded and member / parameter space still has values to be searched
        while (constituentIterator.hasNext() && !contractExceeded()) {
            // get next module
            Constituent constituent = constituentIterator.get();
            Iterator<Classifier> classifierGenerator = constituent.getClassifierIterator();
            // check whether module has a new candidate to try
            if(classifierGenerator.hasNext()) {
                // get next candidate
                Classifier classifier = classifierGenerator.get();
                // set classifier's seed for reproducibility
                classifier.setSeed(seed);
                // check whether instances have been generated for module's filter
                SimpleBatchFilter filter = constituent.getFilter();
                Folds folds = filterFoldsMap.get(filter);
                if(folds == null) {
                    // instances haven't been filtered yet
                    // find filter for the module
                    Instances filteredInstances;
                    if(filter == null) {
                        // no filter required for the module
                        filteredInstances = trainInstances;
                    } else {
                        filteredInstances = filter.process(trainInstances);
                    }
                    // build folds from the filtered instances
                    folds = new Folds.Builder(filteredInstances)
                            .setSeed(seed) // seed to make it reproducible
                            .build();
                    // put the newly generated folds into the map for reuse with other modules that use the same filter
                    filterFoldsMap.put(filter, folds);
                }
                if(filter == null) {
                    System.out.println(classifier.toString());
                } else {
                    System.out.println(filter.toString() + "-" + classifier.toString());
                }
                // benchmark classifier
                ClassifierResults results = Utilities.run(classifierGenerator::get, folds);
                results.setNumInstances(folds.getNumInstances());
                results.setNumClasses(folds.getNumClasses());
                results.findAllStatsOnce();
                // check whether classifier's performance makes the cut
                ConstituentCandidate candidate = new ConstituentCandidate(classifier, results, constituent);
                candidateSelector.consider(candidate);
            } else {
                constituentIterator.remove();
            }
            classifierGenerator.shift();
            constituentIterator.shift();
            checkpoint(SAVE_FILENAME);
        }
        System.out.println();
        System.out.println("Selected candidates:");
        List<ConstituentCandidate> selectedCandidates = candidateSelector.getSelectedCandidates();
        for(ConstituentCandidate candidate : selectedCandidates) {
            System.out.println(candidate.getClassifier().toString());
            List<Candidate> candidatesForFilter = filterCandidateMap.computeIfAbsent(candidate.getConstituent().getFilter(), k -> new ArrayList<>());
            Classifier classifier = candidate.getClassifier();
            classifier.buildClassifier(trainInstances);
            candidatesForFilter.add(new Candidate(classifier, candidate.getResults()));
        }
        lastCheckpoint(SAVE_FILENAME); // todo selected candidates empty
    }

    @Override
    public double classifyInstance(Instance testInstance) throws Exception {
        return Utilities.max(distributionForInstance(testInstance));
    }

    @Override
    public double[] distributionForInstance(Instance testInstance) throws Exception {
        double[] distribution = new double[testInstance.numClasses()];
        for(SimpleBatchFilter filter : filterCandidateMap.keySet()) {
            List<Candidate> candidates = filterCandidateMap.get(filter);
            Instance filteredInstance = Utilities.filter(filter, testInstance);
            for(Candidate candidate : candidates) {
                double[] candidateDistribution = candidate.getClassifier().distributionForInstance(filteredInstance);
                candidateDistribution = Utilities.multiply(candidateDistribution, candidate.getResults().acc);
                distribution = Utilities.add(distribution, candidateDistribution);
            }
        }
        distribution = Utilities.normalise(distribution);
        return distribution;
    }

    @Override
    public Capabilities getCapabilities() {
        throw new UnsupportedOperationException();
    }

    public static abstract class ConstituentObtainer implements Obtainer<Instances, Collection<Constituent>> {} // just synatactic sugar alias

    public static class Constituent implements Serializable {
        private final Iterator<Classifier> classifierIterator;
        private final SimpleBatchFilter filter;

        public Constituent(Iterator<Classifier> classifierIterator, SimpleBatchFilter filter) {
            this.classifierIterator = classifierIterator;
            this.filter = filter;
        }

        public Constituent(Iterator<Classifier> classifierIterator) {
            this(classifierIterator, null);
        }

        public SimpleBatchFilter getFilter() {
            return filter;
        }

        public Iterator<Classifier> getClassifierIterator() {
            return classifierIterator;
        }
    }

    @Override
    public String toString() {
        return "ElasticEnsemble"; // todo params
    }
}
