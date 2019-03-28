package timeseriesweka.classifiers;

import weka.core.OptionHandler;

public interface SaveParameterInfoOptions extends OptionHandler, SaveParameterInfo {
    @Override
    default String getParameters() {
        StringBuilder stringBuilder = new StringBuilder();
        String[] parameters = getOptions();
        if(parameters.length == 0) {
            return "";
        }
        for(int i = 0; i < parameters.length - 1; i++) {
            stringBuilder.append(parameters[i]);
            stringBuilder.append(",");
        }
        stringBuilder.append(parameters[parameters.length - 1]);
        return stringBuilder.toString();
    }
}
