package timeseriesweka.classifiers.ee.selection;

import utilities.ClassifierResults;
import utilities.Reproducible;
import weka.classifiers.Classifier;

import java.util.Collection;
import java.util.List;

public interface Selector<A> extends Reproducible {
    void consider(A a, double stat); // todo doesn't have to be double to eval on... generic?
    Collection<Weighted<A>> getSelected();
    void reset();
}
