package timeseriesweka.classifiers.ensembles.ee.CandidateSelector;

import utilities.Reproducible;

import java.io.Serializable;
import java.util.List;

public interface CandidateSelector<E> extends Serializable, Reproducible {
    void consider(E candidate);
    List<E> getSelectedCandidates(); // todo no need for classifier results, can provide comparator for E
    void reset();
}
