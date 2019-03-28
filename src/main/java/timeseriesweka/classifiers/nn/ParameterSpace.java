package timeseriesweka.classifiers.nn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParameterSpace {
    private String key;
    private List<? extends Object> values = new ArrayList<>();

    public ParameterSpace(final String key) {
        this.key = key;
    }

    public ParameterSpace(final String key, final List<? extends Object> values) {
        this(key);
        this.values = values;
    }

    public List<? extends Object> getValues() {
        return values;
    }

    public void setValues(final List<? extends Object> values) {
        this.values = values;
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public int size() {
        return values.size();
    }
}
