package timeseriesweka.classifiers.ee;

import timeseriesweka.classifiers.Reproducible;
import timeseriesweka.classifiers.ee.Constituents.*;
import timeseriesweka.classifiers.ee.Iteration.AbstractIndexIterator;
import timeseriesweka.classifiers.ee.Iteration.ElementIterator;
import timeseriesweka.classifiers.ee.Iteration.RoundRobinIndexIterator;
import utilities.ClassifierResults;
import utilities.CrossValidator;
import utilities.StatisticalUtilities;
import utilities.instances.Experimenter;
import utilities.instances.Folds;
import utilities.instances.TrainTestSplit;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;
import java.util.function.Function;

public class Ee extends AbstractClassifier implements Reproducible {

    private final List<Function<Instances, Iterator<Classifier>>> constituentBuilders = new LinkedList<>();
    private AbstractIndexIterator constituentIndexIterator = new RoundRobinIndexIterator();

    @Override
    public void buildClassifier(Instances trainInstances) throws Exception {
        // fold train instances
        Folds folds = new Folds.Builder(trainInstances, 30).build(); // todo num folds? loocv from Jay's
        // build constituents
        List<Iterator<Classifier>> constituents = new LinkedList<>();
        for(Function<Instances, Iterator<Classifier>> constituentBuilder : constituentBuilders) {
            Iterator<Classifier> constituent = constituentBuilder.apply(trainInstances);
            constituents.add(constituent);
        }
        // setup constituent iterator
        ElementIterator<Iterator<Classifier>> constituentIterator = new ElementIterator<>();
        constituentIterator.setIndexIterator(constituentIndexIterator);
        constituentIterator.setList(constituents);
        // iterate through constituent iterators
        while (constituentIterator.hasNext()) {
            Iterator<Classifier> classifierIterator = constituentIterator.next();
            // if constituent has no classifiers left
            if(!classifierIterator.hasNext()) {
                // remove constituent
                constituentIterator.remove();
            } else {
                // get next classifier
                Classifier classifier = classifierIterator.next();
                // run classifier
                ClassifierResults classifierResults = Experimenter.experiment(classifier, folds);
                // check if classifier makes the cut // todo
            }
        }
    }

    public static void main(String[] args) {
        Ee ee = new Ee();

    }

    @Override
    public void setSeed(final long seed) {

    }

    public List<Function<Instances, Iterator<Classifier>>> getConstituentBuilders() {
        return constituentBuilders;
    }

    public Function<Instances, Iterator<Classifier>> getDtwConstituentBuilder() {
        return instances -> {
            DtwConstituent dtwConstituent = new DtwConstituent();
            dtwConstituent.getWarpingWindowValueRange().getRange().add(0, 99);
            return dtwConstituent;
        };
    }

    public Function<Instances, Iterator<Classifier>> getErpConstituentBuilder() {
        return instances -> {
            double stdDev = /* todo */ ;
            ErpConstituent erpConstituent = new ErpConstituent();
            ValueRange<Double> warpingWindowValueRange = erpConstituent.getWarpingWindowValueRange();
            warpingWindowValueRange.setIndexed(new LinearInterpolater(0, 0.25));
            warpingWindowValueRange.getRange().add(0, 9);
            ValueRange<Double> penaltyValueRange = erpConstituent.getPenaltyValueRange();
            penaltyValueRange.setIndexed(new LinearInterpolater(0.2 * stdDev, stdDev));
            penaltyValueRange.getRange().add(0, 9);
            return erpConstituent;
        };
    }

    public Function<Instances, Iterator<Classifier>> getLcssConstituentBuilder() {
        return instances -> {
            double stdDev = /* todo */ ;
            LcssConstituent lcssConstituent = new LcssConstituent();
            ValueRange<Double> warpingWindowValueRange = lcssConstituent.getWarpingWindowValueRange();
            warpingWindowValueRange.setIndexed(new LinearInterpolater(0, (double) (instances.numAttributes() - 1) / 4));
            warpingWindowValueRange.getRange().add(0, 9);
            ValueRange<Double> toleranceValueRange = lcssConstituent.getToleranceValueRange();
            toleranceValueRange.setIndexed(new LinearInterpolater(0.2 * stdDev, stdDev));
            toleranceValueRange.getRange().add(0, 9);
            return lcssConstituent;
        };
    }

    public Function<Instances, Iterator<Classifier>> getMsmConstituentBuilder() {
        return instances -> {
            MsmConstituent msmConstituent = new MsmConstituent();
            ValueRange<Double> toleranceValueRange = msmConstituent.getCostValueRange();
            Double[] costs = {
                // <editor-fold defaultstate="collapsed" desc="hidden for space">
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
                100.0// </editor-fold>
            };
            toleranceValueRange.setIndexed(new ElementObtainer<>(Arrays.asList(costs)));
            toleranceValueRange.getRange().add(0, costs.length - 1);
            return msmConstituent;
        };
    }

    public Function<Instances, Iterator<Classifier>> getTweConstituentBuilder() {
        return instances -> {
            TweConstituent tweConstitent = new TweConstituent();
            ValueRange<Double> warpingWindowValueRange = tweConstitent.getNuValueRange();
            warpingWindowValueRange.setIndexed(new LinearInterpolater(0, 9));
            warpingWindowValueRange.getRange().add(0, 9);
            ValueRange<Double> lambdaValueRange = tweConstitent.getLambdaValueRange();
            lambdaValueRange.setIndexed(new LinearInterpolater(0, 9));
            lambdaValueRange.getRange().add(0, 9);
            return tweConstitent;
        };
    }

    public Function<Instances, Iterator<Classifier>> getWdtwConstituentBuilder() {
        return instances -> {
            WdtwConstituent wdtwConstitent = new WdtwConstituent();
            ValueRange<Double> warpingWindowValueRange = wdtwConstitent.getWeightValueRange();
            warpingWindowValueRange.setIndexed(new LinearInterpolater(0, 99));
            warpingWindowValueRange.getRange().add(0, 99);
            return wdtwConstitent;
        };
    }

    public void loadClassicConfig() {

    }
}
