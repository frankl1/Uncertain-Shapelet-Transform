package utilities;

import weka.core.OptionHandler;

public interface OptionsSetter extends OptionHandler {

    boolean setOption(String key, String value);

    @Override
    default void setOptions(String[] options) throws Exception {
        setOptions(this, options);
    }

    static void setOptions(OptionsSetter optionsSetter, String[] options) {
        for(int i = 0; i < options.length; i += 2) {
            String key = options[i];
            String value = options[i + 1];
            if(!optionsSetter.setOption(key, value)) {
                throw new IllegalArgumentException("unknown key " + key + " and value " + value);
            }
        }
    }
}
