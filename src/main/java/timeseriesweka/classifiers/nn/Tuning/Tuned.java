package timeseriesweka.classifiers.nn.Tuning;

import timeseriesweka.classifiers.AdvancedAbstractClassifier.AdvancedAbstractClassifier;

public class Tuned<A extends AdvancedAbstractClassifier> extends AbstractTuned<A> {

    private A classifier;

    public void setClassifier(final A classifier) {
        this.classifier = classifier;
    }

    public A getClassifier() {
        return classifier;
    }

    @Override
    protected A getClassifierInstance() {
        return classifier;
    }
}
