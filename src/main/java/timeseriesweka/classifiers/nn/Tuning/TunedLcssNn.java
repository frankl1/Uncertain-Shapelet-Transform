package timeseriesweka.classifiers.nn.Tuning;

import timeseriesweka.classifiers.ParameterSplittable;
import timeseriesweka.classifiers.nn.Specialised.LcssNn;
import timeseriesweka.classifiers.nn.ParameterFinder;
import timeseriesweka.measures.lcss.Lcss;
import utilities.Utilities;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TunedLcssNn extends LcssNn implements ParameterSplittable {

    private int parameterId = -1;

    private ParameterFinder<Double> toleranceValuesFinder = instances -> new ArrayList<>(Collections.singletonList(Lcss.DEFAULT_TOLERANCE));
    private ParameterFinder<Double> warpingWindowValuesFinder = instances -> new ArrayList<>(Collections.singletonList(Lcss.DEFAULT_WARPING_WINDOW));

    private List<Double> warpingWindowValues = null;
    private List<Double> toleranceValues = null;

    private void populateParameters(Instances instances) {
        warpingWindowValues = warpingWindowValuesFinder.findParameters(instances);
        toleranceValues = toleranceValuesFinder.findParameters(instances);
    }

    private List<Integer> getParameterBins() {
        return new ArrayList<>(Arrays.asList(warpingWindowValues.size(), toleranceValues.size()));
    }

    private void setupParameters(Instances trainInstances) {
        populateParameters(trainInstances);
        List<Integer> parameterBins = getParameterBins();
        List<Integer> permutation = Utilities.fromPermutation(parameterId, parameterBins);
        lcss.setWarpingWindow(permutation.get(0));
        lcss.setTolerance(permutation.get(1));
    }

    private void postProcess() {

    }

    @Override
    public void buildClassifier(final Instances trainInstances) throws Exception {
        if(parameterId < 0) {
            postProcess();
        } else {
            setupParameters(trainInstances);
            super.buildClassifier(trainInstances);
        }
    }

    @Override
    public void setParamSearch(final boolean b) {
        if(!b) {
            parameterId = -1;
        }
    }

    @Override
    public void setParametersFromIndex(final int x) {
        parameterId = x;
    }

    @Override
    public String getParas() {
        return getParameters();
    }

    @Override
    public double getAcc() {
        return 0;
    }
}
