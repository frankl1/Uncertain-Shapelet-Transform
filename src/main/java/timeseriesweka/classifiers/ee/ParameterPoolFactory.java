package timeseriesweka.classifiers.ee;

import utilities.parameters.ParameterPool;
import timeseriesweka.classifiers.Nn.Nn;
import timeseriesweka.measures.DistanceMeasureFactory;
import timeseriesweka.measures.ddtw.Ddtw;
import timeseriesweka.measures.dtw.Dtw;
import timeseriesweka.measures.erp.Erp;
import timeseriesweka.measures.lcss.Lcss;
import timeseriesweka.measures.msm.Msm;
import timeseriesweka.measures.twe.Twe;
import timeseriesweka.measures.wddtw.Wddtw;
import timeseriesweka.measures.wdtw.Wdtw;
import utilities.StatisticUtilities;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Arrays;

import static utilities.Utilities.incrementalDiffList;

public class ParameterPoolFactory {

    public static ParameterPool msmParameterPool(Instances instances) {
        ParameterPool parameterPool = new ParameterPool();
        parameterPool.add(Nn.DISTANCE_MEASURE_KEY, new ArrayList<>(Arrays.asList(DistanceMeasureFactory.getInstance().getKey(Msm.class))));
        parameterPool.add(Msm.PENALTY_KEY, new ArrayList<>(Arrays.asList(
                0.01,
                0.01375,
                0.0175,
                0.02125,
                0.025,
                0.02875,
                0.0325,
                0.03625,
                0.04,
                0.04375,
                0.0475,
                0.05125,
                0.055,
                0.05875,
                0.0625,
                0.06625,
                0.07,
                0.07375,
                0.0775,
                0.08125,
                0.085,
                0.08875,
                0.0925,
                0.09625,
                0.1,
                0.136,
                0.172,
                0.208,
                0.244,
                0.28,
                0.316,
                0.352,
                0.388,
                0.424,
                0.46,
                0.496,
                0.532,
                0.568,
                0.604,
                0.64,
                0.676,
                0.712,
                0.748,
                0.784,
                0.82,
                0.856,
                0.892,
                0.928,
                0.964,
                1.0,
                1.36,
                1.72,
                2.08,
                2.44,
                2.8,
                3.16,
                3.52,
                3.88,
                4.24,
                4.6,
                4.96,
                5.32,
                5.68,
                6.04,
                6.4,
                6.76,
                7.12,
                7.48,
                7.84,
                8.2,
                8.56,
                8.92,
                9.28,
                9.64,
                10.0,
                13.6,
                17.2,
                20.8,
                24.4,
                28.0,
                31.6,
                35.2,
                38.8,
                42.4,
                46.0,
                49.6,
                53.2,
                56.8,
                60.4,
                64.0,
                67.6,
                71.2,
                74.8,
                78.4,
                82.0,
                85.6,
                89.2,
                92.8,
                96.4,
                100.0
        )));
        return parameterPool;
    }

    public static ParameterPool tweParameterPool(Instances instances) {
        ParameterPool parameterPool = new ParameterPool();
        parameterPool.add(Nn.DISTANCE_MEASURE_KEY, new ArrayList<>(Arrays.asList(DistanceMeasureFactory.getInstance().getKey(Twe.class))));
        parameterPool.add(Twe.PENALTY_KEY, new ArrayList<>(Arrays.asList(
                0.00001,
                0.0001,
                0.0005,
                0.001,
                0.005,
                0.01,
                0.05,
                0.1,
                0.5,
                1
        )));
        parameterPool.add(Twe.PENALTY_KEY, new ArrayList<>(Arrays.asList(
                0,
                0.011111111,
                0.022222222,
                0.033333333,
                0.044444444,
                0.055555556,
                0.066666667,
                0.077777778,
                0.088888889,
                0.1
        )));
        return parameterPool;
    }

    public static ParameterPool lcssParameterPool(Instances instances) {
        ParameterPool parameterPool = new ParameterPool();
        parameterPool.add(Nn.DISTANCE_MEASURE_KEY, new ArrayList<>(Arrays.asList(DistanceMeasureFactory.getInstance().getKey(Lcss.class))));
        parameterPool.add(Lcss.WARPING_WINDOW_KEY, incrementalDiffList(0, 1, 10));
        double maxTolerance = StatisticUtilities.populationStandardDeviation(instances);
        double minTolerance = maxTolerance * 0.2;
        parameterPool.add(Lcss.TOLERANCE_KEY, incrementalDiffList(minTolerance, maxTolerance, 10));
        return parameterPool;
    }

    public static ParameterPool erpParameterPool(Instances instances) {
        ParameterPool parameterPool = new ParameterPool();
        parameterPool.add(Nn.DISTANCE_MEASURE_KEY, new ArrayList<>(Arrays.asList(DistanceMeasureFactory.getInstance().getKey(Erp.class))));
        parameterPool.add(Erp.WARPING_WINDOW_KEY, incrementalDiffList(0, 1, 10));
        double maxPenalty = StatisticUtilities.populationStandardDeviation(instances);
        double minPenalty = maxPenalty * 0.2;
        parameterPool.add(Erp.PENALTY_KEY, incrementalDiffList(minPenalty, maxPenalty, 10));
        return parameterPool;
    }

    public static ParameterPool classicDtwParameterPool(Instances instances) {
        ParameterPool parameterPool = new ParameterPool();
        parameterPool.add(Nn.DISTANCE_MEASURE_KEY, new ArrayList<>(Arrays.asList(DistanceMeasureFactory.getInstance().getKey(Dtw.class))));
        parameterPool.add(Dtw.WARPING_WINDOW_KEY, incrementalDiffList(0, 0.99, 100));
        return parameterPool;
    }

    public static ParameterPool euclideanParameterPool(Instances instances) {
        ParameterPool parameterPool = new ParameterPool();
        parameterPool.add(Nn.DISTANCE_MEASURE_KEY, new ArrayList<>(Arrays.asList(DistanceMeasureFactory.getInstance().getKey(Dtw.class))));
        parameterPool.add(Dtw.WARPING_WINDOW_KEY, new ArrayList<>(Arrays.asList(0.0)));
        return parameterPool;
    }

    public static ParameterPool fullWindowDtwParameterPool(Instances instances) {
        ParameterPool parameterPool = new ParameterPool();
        parameterPool.add(Nn.DISTANCE_MEASURE_KEY, new ArrayList<>(Arrays.asList(DistanceMeasureFactory.getInstance().getKey(Dtw.class))));
        parameterPool.add(Dtw.WARPING_WINDOW_KEY, new ArrayList<>(Arrays.asList(1.0)));
        return parameterPool;
    }

    public static ParameterPool dtwParameterPool(Instances instances) {
        ParameterPool parameterPool = new ParameterPool();
        parameterPool.add(Nn.DISTANCE_MEASURE_KEY, new ArrayList<>(Arrays.asList(DistanceMeasureFactory.getInstance().getKey(Dtw.class))));
        parameterPool.add(Dtw.WARPING_WINDOW_KEY, incrementalDiffList(0, 1, 101));
        return parameterPool;
    }


    public static ParameterPool classicDdtwParameterPool(Instances instances) {
        ParameterPool parameterPool = new ParameterPool();
        parameterPool.add(Nn.DISTANCE_MEASURE_KEY, new ArrayList<>(Arrays.asList(DistanceMeasureFactory.getInstance().getKey(Ddtw.class))));
        parameterPool.add(Ddtw.WARPING_WINDOW_KEY, incrementalDiffList(0, 0.99, 100));
        return parameterPool;
    }

    public static ParameterPool ddtwParameterPool(Instances instances) {
        ParameterPool parameterPool = new ParameterPool();
        parameterPool.add(Nn.DISTANCE_MEASURE_KEY, new ArrayList<>(Arrays.asList(DistanceMeasureFactory.getInstance().getKey(Ddtw.class))));
        parameterPool.add(Ddtw.WARPING_WINDOW_KEY, incrementalDiffList(0, 1, 101));
        return parameterPool;
    }

    public static ParameterPool classicWdtwParameterPool(Instances instances) {
        ParameterPool parameterPool = new ParameterPool();
        parameterPool.add(Nn.DISTANCE_MEASURE_KEY, new ArrayList<>(Arrays.asList(DistanceMeasureFactory.getInstance().getKey(Wdtw.class))));
        parameterPool.add(Wdtw.WEIGHT_KEY, incrementalDiffList(0, 0.99, 100));
        return parameterPool;
    }

    public static ParameterPool wdtwParameterPool(Instances instances) {
        ParameterPool parameterPool = new ParameterPool();
        parameterPool.add(Nn.DISTANCE_MEASURE_KEY, new ArrayList<>(Arrays.asList(DistanceMeasureFactory.getInstance().getKey(Wdtw.class))));
        parameterPool.add(Wdtw.WEIGHT_KEY, incrementalDiffList(0, 1, 101));
        return parameterPool;
    }

    public static ParameterPool wddtwParameterPool(Instances instances) {
        ParameterPool parameterPool = new ParameterPool();
        parameterPool.add(Nn.DISTANCE_MEASURE_KEY, new ArrayList<>(Arrays.asList(DistanceMeasureFactory.getInstance().getKey(Wddtw.class))));
        parameterPool.add(Wddtw.WEIGHT_KEY, incrementalDiffList(0, 1, 101));
        return parameterPool;
    }

    public static ParameterPool classicWddtwParameterPool(Instances instances) {
        ParameterPool parameterPool = new ParameterPool();
        parameterPool.add(Nn.DISTANCE_MEASURE_KEY, new ArrayList<>(Arrays.asList(DistanceMeasureFactory.getInstance().getKey(Wddtw.class))));
        parameterPool.add(Wddtw.WEIGHT_KEY, incrementalDiffList(0, 0.99, 100));
        return parameterPool;
    }
}
