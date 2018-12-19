package timeseriesweka.classifiers.ensembles.ee.CandidateSelector;

import utilities.ClassifierResults;
import weka.classifiers.Classifier;

public class TrainedClassifier {
    private final ClassifierResults trainResults;
    private final Classifier classifier;

    public ClassifierResults getTrainResults() {
        return trainResults;
    }

    public Classifier getClassifier() {
        return classifier;
    }

    public TrainedClassifier(ClassifierResults trainResults, Classifier classifier) {
        this.trainResults = trainResults;
        this.classifier = classifier;
    }
}
