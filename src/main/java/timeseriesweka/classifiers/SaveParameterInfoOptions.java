package timeseriesweka.classifiers;

import utilities.Utilities;
import weka.core.OptionHandler;

public interface SaveParameterInfoOptions extends OptionHandler, SaveParameterInfo {
    @Override
    default String getParameters() {
        return Utilities.join(getOptions(), ",");
    }
}
