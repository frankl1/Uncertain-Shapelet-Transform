package utilities;

import weka.core.OptionHandler;

public interface OptionsSetter extends OptionHandler {

    boolean setOption(String key, String value);

    @Override
    default void setOptions(String[] options) throws Exception {
        for(int i = 0; i < options.length; i += 2) {
            String key = options[i];
            String value = options[i + 1];
            if(!setOption(key, value)) {
                throw new IllegalArgumentException("unknown key " + key + " and value " + value);
            }
        }
    }
}
