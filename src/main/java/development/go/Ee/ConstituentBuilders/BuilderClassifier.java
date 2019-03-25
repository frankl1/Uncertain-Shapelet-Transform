package development.go.Ee.ConstituentBuilders;

import timeseriesweka.classifiers.AdvancedAbstractClassifier.AdvancedAbstractClassifier;
import timeseriesweka.classifiers.ParameterSplittable;
import weka.core.Instance;
import weka.core.Instances;

public class BuilderClassifier extends AdvancedAbstractClassifier implements ParameterSplittable {
    public BuilderClassifier(final PermutedBuilder<? extends AdvancedAbstractClassifier> builder) {
        this.builder = builder;
    }

    private PermutedBuilder<? extends AdvancedAbstractClassifier> builder;
    private AdvancedAbstractClassifier classifier;

    @Override
    public void buildClassifier(final Instances trainInstances) throws Exception {
        builder.useInstances(trainInstances);
        classifier = builder.build();
        classifier.buildClassifier(trainInstances);
    }

    @Override
    public double[] distributionForInstance(final Instance testInstance) throws Exception {
        return classifier.distributionForInstance(testInstance);
    }

    @Override
    public double classifyInstance(final Instance testInstance) throws Exception {
        return classifier.classifyInstance(testInstance);
    }

    // todo pass advanced abstract classifier funcs onto classifier funcs

    @Override
    public void setParamSearch(final boolean b) {

    }

    @Override
    public void setParametersFromIndex(final int x) {
        builder.setPermutation(x);
    }

    @Override
    public String getParas() {
        return classifier.getParameters();
    }

    @Override
    public double getAcc() {
        return -1;
    }
}
