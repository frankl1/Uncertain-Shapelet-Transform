package development.go.Ee;

import evaluation.storage.ClassifierResults;
import weka.classifiers.AbstractClassifier;

public class ParameterRecord {
    public int getParameterPermutationIndex() {
        return parameterPermutationIndex;
    }

    private final int parameterPermutationIndex;
    private final AbstractClassifier classifier;
    private final ClassifierResults trainResults;
    private boolean built;

    public ParameterRecord(final int parameterPermutationIndex, final AbstractClassifier classifier, final ClassifierResults trainResults, final boolean built) {
        this.parameterPermutationIndex = parameterPermutationIndex;
        this.classifier = classifier;
        this.trainResults = trainResults;
        this.built = built;
    }

    public AbstractClassifier getClassifier() {
        return classifier;
    }

    public ClassifierResults getTrainResults() {
        return trainResults;
    }

    public boolean isBuilt() {
        return built;
    }

    public void setBuilt(final boolean built) {
        this.built = built;
    }
}
