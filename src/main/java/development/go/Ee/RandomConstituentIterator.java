package development.go.Ee;

import development.go.Ee.ParameterIteration.SourcedIterator;

import java.util.*;

public class RandomConstituentIterator implements SourcedIterator<Integer, List<AbstractTuned>> {
    private final List<AbstractTuned> constituents = new ArrayList<>();
    private final List<AbstractTuned> originalConstituents = new ArrayList<>();
    private Random random = new Random();

    @Override
    public void setSource(final List<AbstractTuned> source) {
        constituents.clear();
        originalConstituents.clear();
        constituents.addAll(source);
        originalConstituents.addAll(constituents);
        for(int i = constituents.size() - 1; i >= 0; i--) {
            if(constituents.get(i).size() <= 0) {
                constituents.remove(i);
            }
        }
    }

    @Override
    public void setRandom(final Random random) {
        this.random = random;
    }

    @Override
    public void setSeed(final long seed) {
        random.setSeed(seed);
    }

    @Override
    public boolean hasNext() {
        return !constituents.isEmpty();
    }

    @Override
    public Integer next() {
        int constituentIndex = random.nextInt(constituents.size());
        AbstractTuned constituent = constituents.get(constituentIndex);
        List<Integer> untestedParameterIndices = constituent.getUntestedParameterIndices();
        int parameter = untestedParameterIndices.get(random.nextInt(untestedParameterIndices.size()));
        constituent.parameterIndexTested(parameter);
        if(constituent.getUntestedParameterIndices().isEmpty()) {
            constituents.remove(constituentIndex);
        }
        int originalConstituentIndex = originalConstituents.indexOf(constituent);
        for(int i = 0; i < originalConstituentIndex; i++) {
            parameter += originalConstituents.get(i).size();
        }
        return parameter;
    }
}
