package timeseriesweka.classifiers.ensembles.ee.CandidateSelector;

import utilities.Obtainer;
import utilities.SerializedComparator;

import java.util.*;

public class BestCandidatePerClassifier<E, F> implements CandidateSelector<E> {
    private final Map<F, List<E>> candidateMap = new HashMap<>();
    private final SerializedComparator<E> comparator;
    private final Random random = new Random();
    private final Obtainer<E, F> identifier;

    public void setSeed(long seed) {
        random.setSeed(seed);
    }

    public BestCandidatePerClassifier(SerializedComparator<E> comparator, Obtainer<E, F> identifier) {
        this.comparator = comparator;
        this.identifier = identifier;
    }

    @Override
    public void consider(E candidate) {
        F key = identifier.obtain(candidate);
        List<E> list = candidateMap.get(key);
        if(list == null) {
            list = new ArrayList<>();
            list.add(candidate);
            candidateMap.put(key, list);
        } else {
            E otherCandidate = list.get(0);
            int comparison = comparator.compare(otherCandidate, candidate); // todo make sure these are the right way around
            if(comparison > 0) {
                list.clear();
            }
            if(comparison >= 0) {
                list.add(candidate);
            }
        }
    }

    @Override
    public List<E> getSelectedCandidates() {
        List<E> selectedCandidates = new ArrayList<>();
        for(F key : candidateMap.keySet()) {
            List<E> candidates = candidateMap.get(key);
            int randomCandidateIndex = random.nextInt(candidates.size());
            E candidate = candidates.get(randomCandidateIndex);
            selectedCandidates.add(candidate);
        }
        return selectedCandidates;
    }

    @Override
    public void reset() {
        candidateMap.clear();
    }

}
