package timeseriesweka.classifiers.Nn.Specialised.Wddtw;

import development.go.Ee.Tuned;
import evaluation.tuning.ParameterSpace;
import evaluation.tuning.Tuner;
import timeseriesweka.classifiers.Nn.Specialised.Wdtw.TunedWdtwNn;
import timeseriesweka.classifiers.Nn.Specialised.Wdtw.WdtwNn;
import timeseriesweka.measures.dtw.Dtw;
import timeseriesweka.measures.wdtw.Wdtw;
import utilities.Utilities;
import weka.core.Instances;

import java.util.Collections;
import java.util.List;

public class TunedWddtwNn extends TunedWdtwNn {
    public TunedWddtwNn() {
        setClassifier(new WddtwNn());
    }
}
