package development.go.Ee.ParameterIteration;

import development.go.Ee.ConstituentBuilders.ConstituentBuilder;
import timeseriesweka.classifiers.Nn.AbstractNn;

import java.util.*;

public class RandomIterationStrategy implements IterationStrategy {

    private Random random = new Random();
    private final Map<ConstituentBuilder, List<Integer>> map = new TreeMap<>();
    private final List<ConstituentBuilder> constituentBuilders = new ArrayList<>();

    @Override
    public void useConstituentBuilders(final List<ConstituentBuilder> constituentBuilders) {
        map.clear();
        for(ConstituentBuilder constituentBuilder : constituentBuilders) {
            List<Integer> parameters = new ArrayList<>();
            for(int i = 0; i < constituentBuilder.size(); i++) {
                parameters.add(i);
            }
            map.put(constituentBuilder, parameters);
        }
        this.constituentBuilders.clear();
        this.constituentBuilders.addAll(constituentBuilders);
    }

    @Override
    public boolean hasNext() {
        return !constituentBuilders.isEmpty();
    }

    @Override
    public AbstractNn next() {
        int constituentBuilderIndex = random.nextInt(constituentBuilders.size());
        ConstituentBuilder constituentBuilder = constituentBuilders.get(constituentBuilderIndex);
        List<Integer> parameters = map.get(constituentBuilder);
        int parameterIndex = random.nextInt(parameters.size());
        int parameter = parameters.remove(parameterIndex);
        constituentBuilder.setParameterPermutation(parameter);
        AbstractNn nn = constituentBuilder.build();
        if(parameters.isEmpty()) {
            map.remove(constituentBuilder);
            constituentBuilders.remove(constituentBuilderIndex);
        }
        return nn;
    }

    @Override
    public void setRandom(final Random random) {
        this.random = random;
    }

    @Override
    public void setSeed(final long seed) {
        random.setSeed(seed);
    }
}
