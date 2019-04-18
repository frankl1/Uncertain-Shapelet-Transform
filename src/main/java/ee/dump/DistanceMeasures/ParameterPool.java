package ee.dump.DistanceMeasures;

import ee.Randomised;
import ee.sampling.Distribution;

import java.util.*;

public class ParameterPool implements Iterator<Map<String, Object>>, Randomised {

    private final Map<String, List<? extends Object>> predefinedParameters = new HashMap<>();
    private final Map<String, Distribution> distributionParameters = new HashMap<>();

    public void addParameter(String name, List<? extends Object> values) {
        predefinedParameters.put(name, values);
    }

    public void addParameter(String name, Distribution distribution) {
        distributionParameters.put(name, distribution);
    }

    public int limit = -1;
    public int count = 0;

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getLimit() {
        return limit;
    }

    @Override
    public boolean hasNext() {
        return limit < 0 || count < limit;
    }

    @Override
    public Map<String, Object> next() {
        Map<String, Object> permutation = new HashMap<>();
        for(Map.Entry<String, List<? extends Object>> entry : predefinedParameters.entrySet()) {
            List<? extends Object> list = entry.getValue();
            permutation.put(entry.getKey(), list.get(random.nextInt(list.size())));
        }
        for(Map.Entry<String, Distribution> entry : distributionParameters.entrySet()) {
            Distribution distribution = entry.getValue();
            Object value = distribution.sample();
            permutation.put(entry.getKey(), value);
        }
        return permutation;
    }

    private Random random = new Random();

    @Override
    public void setRandom(Random random) {
        this.random = random;
        for(Distribution distribution : distributionParameters.values()) {
            distribution.setRandom(random);
        }
    }

    @Override
    public Random getRandom() {
        return random;
    }
}
