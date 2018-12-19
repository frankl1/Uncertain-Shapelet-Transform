package timeseriesweka.classifiers.ensembles.ee.CandidateSelector;

import utilities.ClassifierResults;
import weka.classifiers.Classifier;
import weka.filters.SimpleBatchFilter;

public class FilteredTrainedClassifier extends TrainedClassifier {
    private final SimpleBatchFilter filter;

    public FilteredTrainedClassifier(ClassifierResults trainResults, Classifier classifier, SimpleBatchFilter filter) {
        super(trainResults, classifier);
        this.filter = filter;
    }

    public SimpleBatchFilter getFilter() {
        return filter;
    }
}
