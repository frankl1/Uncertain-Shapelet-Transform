package timeseriesweka.classifiers.nn;

import utilities.OptionsSetter;
import weka.core.Option;
import weka.core.OptionHandler;

import java.util.*;

public class ParameterPermutation implements OptionHandler, OptionsSetter {
    private final Map<String, Object> parameters = new HashMap<>();

    public void add(String key, Object value) {
        parameters.put(key, value);
    }

    public void remove(String key) {
        parameters.remove(key);
    }

    @Override
    public String toString() {
        String[] strings = getOptions();
        StringBuilder stringBuilder = new StringBuilder();
        if(strings.length > 0) {
            stringBuilder.append(strings[0]);
            for(int i = 0; i < strings.length; i++) {
                stringBuilder.append(",");
                stringBuilder.append(strings[i]);
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public Enumeration listOptions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setOption(final String key, final String value) {
        add(key, value);
        return true;
    }

    @Override
    public String[] getOptions() {
        String[] strings = new String[parameters.size() * 2];
        List<String> keys = new ArrayList<>(parameters.keySet());
        for(int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            strings[i * 2] = key;
            strings[i * 2 + 1] = parameters.get(key).toString();
        }
        return strings;
    }
}
