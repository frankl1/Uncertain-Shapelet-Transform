package development.go.Ee;

import evaluation.storage.ClassifierResults;
import timeseriesweka.classifiers.AdvancedAbstractClassifier.AdvancedAbstractClassifier;
import timeseriesweka.classifiers.ensembles.EnsembleModule;
import timeseriesweka.classifiers.ensembles.voting.MajorityVote;
import timeseriesweka.classifiers.ensembles.voting.ModuleVotingScheme;
import timeseriesweka.classifiers.ensembles.weightings.EqualWeighting;
import timeseriesweka.classifiers.ensembles.weightings.ModuleWeightingScheme;
import utilities.Utilities;
import weka.classifiers.AbstractClassifier;
import weka.core.Instance;
import weka.core.Instances;

import java.io.Serializable;

public class Ensemble extends AdvancedAbstractClassifier implements Serializable {
    private ModuleVotingScheme votingScheme = new MajorityVote();
    private ModuleWeightingScheme weightingScheme = new EqualWeighting();

    public Ensemble(final ModuleWeightingScheme weightingScheme, final ModuleVotingScheme votingScheme) {
        setVotingScheme(votingScheme);
        setWeightingScheme(weightingScheme);
    }

    public ModuleVotingScheme getVotingScheme() {
        return votingScheme;
    }

    public void setVotingScheme(final ModuleVotingScheme votingScheme) {
        this.votingScheme = votingScheme;
    }

    public ModuleWeightingScheme getWeightingScheme() {
        return weightingScheme;
    }

    public void setWeightingScheme(final ModuleWeightingScheme weightingScheme) {
        this.weightingScheme = weightingScheme;
    }

    public EnsembleModule[] getModules() {
        return modules;
    }

    public void setModules(final EnsembleModule[] modules) {
        this.modules = modules;
    }

    private EnsembleModule[] modules = new EnsembleModule[0];

    @Override
    public void buildClassifier(final Instances trainInstances) throws Exception {

    }

    private boolean useRandomTieBreak = true;

    @Override
    public double classifyInstance(final Instance testInstance) throws Exception {
        return votingScheme.classifyInstance(modules, testInstance);
    }

    @Override
    public double[] distributionForInstance(final Instance testInstance) throws Exception {
        return votingScheme.distributionForInstance(modules, testInstance);
    }

    @Override
    public String getParameters() {
        throw new UnsupportedOperationException();
    }

    public boolean isUseRandomTieBreak() {
        return useRandomTieBreak;
    }

    public void setUseRandomTieBreak(final boolean useRandomTieBreak) {
        this.useRandomTieBreak = useRandomTieBreak;
    }
}
