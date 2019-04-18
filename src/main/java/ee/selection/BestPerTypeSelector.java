package ee.selection;

import ee.Randomised;

import java.util.*;
import java.util.function.Function;

public class BestPerTypeSelector<A, B> implements Selector<A>, Randomised {

    private Function<A, B> typeExtractor;
    private Comparator<A> comparator;

    public BestPerTypeSelector(Function<A, B> typeExtractor, Comparator<A> comparator) {
        this.typeExtractor = typeExtractor;
        this.comparator = comparator;
    }

    private final Map<B, List<A>> bestCandidateTypes = new HashMap<>();

    @Override
    public void considerCandidate(A candidate) {
        B type = typeExtractor.apply(candidate);
        List<A> bestOfSameType = bestCandidateTypes.computeIfAbsent(type, key -> new ArrayList<>());
        if(bestOfSameType.isEmpty()) {
            bestOfSameType.add(candidate);
        } else {
            int comparison = comparator.compare(bestOfSameType.get(0), candidate);
            if(comparison >= 0) {
                if(comparison > 0) {
                    bestOfSameType.clear();
                }
                bestOfSameType.add(candidate);
            }
        }
    }

    @Override
    public List<A> getSelected() {
        List<A> selected = new ArrayList<>();
        for(List<A> list : bestCandidateTypes.values()) {
            selected.add(list.get(random.nextInt(list.size())));
        }
        return selected;
    }

    public Comparator<A> getComparator() {
        return comparator;
    }

    public void setComparator(Comparator<A> comparator) {
        this.comparator = comparator;
    }

    public Function<A, B> getTypeExtractor() {
        return typeExtractor;
    }

    public void setTypeExtractor(Function<A, B> typeExtractor) {
        this.typeExtractor = typeExtractor;
    }

    private Random random = new Random();;

    @Override
    public void setRandom(Random random) {
        this.random = random;
    }

    @Override
    public Random getRandom() {
        return random;
    }
}
