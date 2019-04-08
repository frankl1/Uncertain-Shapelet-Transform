package timeseriesweka.classifiers;

import java.util.logging.Logger;

public interface Logable {
    void setLogger(Logger logger);
    Logger getLogger();
}
