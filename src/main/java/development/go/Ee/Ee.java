package development.go.Ee;

import akka.event.Logging;
import development.go.Ee.ParameterIteration.SourcedIterator;
import development.go.Ee.Selection.BestPerType;
import development.go.Ee.Selection.Selector;
import evaluation.storage.ClassifierResults;
import timeseriesweka.classifiers.Nn.Specialised.Ddtw.ClassicTunedDdtwNn;
import timeseriesweka.classifiers.Nn.Specialised.Ddtw.FullWindowTunedDdtwNn;
import timeseriesweka.classifiers.Nn.Specialised.Ddtw.TunedDdtwNn;
import timeseriesweka.classifiers.Nn.Specialised.Dtw.*;
import timeseriesweka.classifiers.Nn.Specialised.Erp.TunedErpNn;
import timeseriesweka.classifiers.Nn.Specialised.Lcss.TunedLcssNn;
import timeseriesweka.classifiers.Nn.Specialised.Msm.TunedMsmNn;
import timeseriesweka.classifiers.Nn.Specialised.Twe.TunedTweNn;
import timeseriesweka.classifiers.Nn.Specialised.Wddtw.ClassicTunedWddtwNn;
import timeseriesweka.classifiers.Nn.Specialised.Wddtw.TunedWddtwNn;
import timeseriesweka.classifiers.Nn.Specialised.Wdtw.ClassicTunedWdtwNn;
import timeseriesweka.classifiers.Nn.Specialised.Wdtw.TunedWdtwNn;
import timeseriesweka.classifiers.ensembles.EnsembleModule;
import timeseriesweka.classifiers.ensembles.voting.MajorityVote;
import timeseriesweka.classifiers.ensembles.voting.ModuleVotingScheme;
import timeseriesweka.classifiers.ensembles.weightings.ModuleWeightingScheme;
import timeseriesweka.classifiers.ensembles.weightings.TrainAcc;
import utilities.Utilities;
import weka.classifiers.AbstractClassifier;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

public class Ee extends AbstractTuned {
    private Logger logger = Logger.getLogger(getClass().getCanonicalName());
    protected List<AbstractTuned> constituents = new ArrayList<>();
    private SourcedIterator<Integer, List<AbstractTuned>> constituentIterator = new RandomConstituentIterator();
    private Selector<ParameterRecord> selector = new BestPerType<>((parameterRecord, t1) -> getComparator().compare(parameterRecord.getTrainResults(), t1.getTrainResults()));
    private Function<ParameterRecord, Object> typeExtractor = ParameterRecord::getClassifier;

    @Override
    public boolean isUseRandomTieBreak() {
        return useRandomTieBreak;
    }

    @Override
    public void setUseRandomTieBreak(final boolean useRandomTieBreak) {
        this.useRandomTieBreak = useRandomTieBreak;
    }

    private boolean useRandomTieBreak = true;

    public Ee() {
        constituents.clear();
        constituents.add(new TunedDtwNn());
        constituents.add(new TunedDdtwNn());
        constituents.add(new TunedWdtwNn());
        constituents.add(new TunedWddtwNn());
        constituents.add(new TunedLcssNn());
        constituents.add(new TunedMsmNn());
        constituents.add(new TunedErpNn());
        constituents.add(new TunedTweNn());
    }

    @Override
    protected AbstractClassifier getClassifierParameterPermutation(int parameterPermutationIndex) throws Exception {
        boolean match = false;
        int constituentIndex = 0;
        AbstractTuned constituent;
        do {
            constituent = constituents.get(constituentIndex);
            if(parameterPermutationIndex < constituent.size()) {
                match = true;
            } else {
                parameterPermutationIndex -= constituent.size();
                constituentIndex++;
            }
        } while (!match && constituentIndex < constituents.size());
        if(!match) {
            throw new IllegalArgumentException("index out of range");
        }
        return constituent.getClassifierParameterPermutation(parameterPermutationIndex);
    }

    @Override
    protected void recordParameterResult(final ParameterRecord parameterRecord) {
        selector.consider(parameterRecord, typeExtractor.apply(parameterRecord));
    }

    @Override
    protected List<Integer> getParameterPermutationIndices() {
        for(AbstractTuned constituent : constituents) {
            constituent.getParameterPermutationIndices();
        }
        return super.getParameterPermutationIndices();
    }

    @Override
    protected Iterator<Integer> getParameterPermutationIterator(int seed) {
        constituentIterator.setSource(constituents);
        constituentIterator.setSeed(seed);
        return constituentIterator;
    }

    @Override
    protected AbstractClassifier combineParameterResults(final Instances trainInstances) throws Exception {
        List<ParameterRecord> selected = selector.getSelected();
        EnsembleModule[] modules = new EnsembleModule[selected.size()];
        for(int i = 0; i < modules.length; i++) {
            ParameterRecord parameterRecord = selected.get(i);
            AbstractClassifier classifier = getClassifierParameterPermutation(parameterRecord.getParameterPermutationIndex());
            EnsembleModule module = new EnsembleModule(classifier.toString(), classifier, Utilities.join(classifier.getOptions(), ","));
            module.trainResults = parameterRecord.getTrainResults();
            modules[i] = module;
        }
        weightingScheme.defineWeightings(modules, trainInstances.numClasses());
        votingScheme.trainVotingScheme(modules, trainInstances.numClasses());
        trainResults = new ClassifierResults();
        for(int i = 0; i < trainInstances.size(); i++) {
            Instance trainInstance = trainInstances.get(i);
            double[] distribution = votingScheme.distributionForTrainInstance(modules, i);
            double prediction;
            if(useRandomTieBreak) {
                prediction = Utilities.argMax(distribution, random);
            } else {
                prediction = Utilities.argMax(distribution)[0];
            }
            long time = 0;
            for(EnsembleModule module : modules) {
                time += module.trainResults.getPredictionTime(i);
            }
            trainResults.addPrediction(trainInstance.classValue(), distribution, prediction, time, null);
        }
        return new AbstractClassifier() {
            @Override
            public void buildClassifier(final Instances trainInstances) throws Exception {

            }

            @Override
            public double[] distributionForInstance(final Instance instance) throws Exception {
                return votingScheme.distributionForInstance(modules, instance);
            }

            @Override
            public double classifyInstance(final Instance instance) throws Exception {
                return votingScheme.classifyInstance(modules, instance);
            }
        };
    }

    private ModuleVotingScheme votingScheme = new MajorityVote();
    private ModuleWeightingScheme weightingScheme = new TrainAcc();

    @Override
    public int size() {
        int size = 0;
        for(AbstractTuned constituent : constituents) {
            size += constituent.size();
        }
        return size;
    }

    @Override
    public void useTrainInstances(final Instances trainInstances) {
        for(AbstractTuned constituent : constituents) {
            constituent.useTrainInstances(trainInstances);
        }
        parameterSpace.clear();
        parameterSpace.addParameter("paramIndex", Utilities.naturalNumbersFromZero(size()));
    }

    public List<AbstractTuned> getConstituents() {
        return constituents;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName().toUpperCase();
    }

    public void classicSetup() {
        constituents.clear();
        constituents.add(new EuclideanTunedDtwNn());
//        constituents.add(new ClassicTunedDtwNn());
//        constituents.add(new ClassicTunedDtwNn());
//        constituents.add(new FullWindowTunedDtwNn());
//        constituents.add(new ClassicTunedDdtwNn());
//        constituents.add(new FullWindowTunedDdtwNn());
//        constituents.add(new ClassicTunedWdtwNn());
//        constituents.add(new ClassicTunedWddtwNn());
//        constituents.add(new TunedLcssNn());
//        constituents.add(new TunedMsmNn());
//        constituents.add(new TunedErpNn());
//        constituents.add(new TunedTweNn());
    }
}
