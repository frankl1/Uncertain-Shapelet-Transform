package utilities.parameters;

import utilities.Utilities;
import weka.core.OptionHandler;

import java.util.*;

public class ParameterPermutation implements OptionHandler {
    private final Map<String, Object> permutation = new LinkedHashMap<>();

    public void put(String name, Object value) {
        permutation.put(name, value);
    }

    public void putAll(ParameterPermutation parameterPermutation) {
        permutation.putAll(parameterPermutation.permutation);
    }

    @Override
    public String toString() {
        return Utilities.join(getOptions(), ",");
    }

    @Override
    public Enumeration listOptions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOptions(String[] options) throws Exception {
        for(int i = 0; i < options.length; i += 2) {
            put(options[i], options[i + 1]);
        }
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof ParameterPermutation) {
            ParameterPermutation otherPermutation = (ParameterPermutation) other;
            for(Map.Entry<String, Object> entry : permutation.entrySet()) {
                Object otherValue = otherPermutation.permutation.get(entry.getKey());
                if(otherValue == null || !otherValue.equals(entry.getValue())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public String[] getOptions() {
        List<String> options = new ArrayList<>();
        for(Map.Entry<String, Object> entry : permutation.entrySet()) {
            options.add(entry.getKey());
            options.add(String.valueOf(entry.getValue()));
        }
        return options.toArray(new String[0]);
    }
}
