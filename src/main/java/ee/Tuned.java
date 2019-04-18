package ee;

import ee.parameter.ParameterPermutation;
import ee.parameter.ParameterPermutationIterator;
import evaluation.evaluators.CrossValidationEvaluator;
import evaluation.evaluators.Evaluator;
import evaluation.storage.ClassifierResults;
import timeseriesweka.classifiers.AdvancedAbstractClassifier.AdvancedAbstractClassifier;
import weka.classifiers.AbstractClassifier;
import weka.core.Instances;

import java.util.function.Function;
import java.util.function.Supplier;

public class Tuned extends AdvancedAbstractClassifier {
    private final Supplier<AbstractClassifier> classifierSupplier;
    private final Function<Instances, ParameterPermutationIterator> parameterPermutationIteratorObtainer;

    private ParameterPermutationIterator parameterPermutationIterator;

    public Tuned(final Supplier<AbstractClassifier> classifierSupplier, final Function<Instances, ParameterPermutationIterator> parameterPermutationIteratorObtainer) {
        this.classifierSupplier = classifierSupplier;
        this.parameterPermutationIteratorObtainer = parameterPermutationIteratorObtainer;
    }

    @Override
    public void buildClassifier(final Instances trainInstances) throws Exception {
        if(resetOnTrain) {
            trainTime = 0;
            trainTimeStamp = System.nanoTime();
            parameterPermutationIterator = parameterPermutationIteratorObtainer.apply(trainInstances);
        }
        while (parameterPermutationIterator.hasNext() && withinTrainContract()) {
            AbstractClassifier classifier = classifierSupplier.get();
            ParameterPermutation parameterPermutation = parameterPermutationIterator.next();
            classifier.setOptions(parameterPermutation.getOptions());
            Evaluator evaluator = new CrossValidationEvaluator(); // todo custom number of folds
            evaluator.setSeed(0); // todo
            ClassifierResults trainResults = evaluator.evaluate(classifier, trainInstances);

        }
    }
}
