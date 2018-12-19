package timeseriesweka.classifiers.ensembles.ee;

import timeseriesweka.classifiers.ensembles.ee.Iteration.LinearIterator;
import timeseriesweka.classifiers.ensembles.ee.Iteration.RetargetableIterator;
import timeseriesweka.classifiers.ensembles.ee.Parameter.ParameterRangeSet;

import static utilities.Utilities.zeroToX;

public abstract class SingleParameterClassifierGenerator implements ClassifierGenerator {

    protected SingleParameterClassifierGenerator(final ParameterRangeSet parameterRangeSet) {
        this.parameterRangeSet = parameterRangeSet;
    }

    public void setParameterCombinationIterator(RetargetableIterator<Integer> parameterCombinationIterator) {
        this.parameterCombinationIterator = parameterCombinationIterator;
        reset();
    }

    private RetargetableIterator<Integer> parameterCombinationIterator = new LinearIterator<>();

    private final ParameterRangeSet parameterRangeSet;

    protected abstract void setup();

    protected abstract Classifier generate();

    @Override
    public void reset() {
        parameterCombinationIterator.setValues(zeroToX(parameterRangeSet.size()));
    }

    @Override
    public boolean hasNext() {
        return parameterCombinationIterator.hasNext();
    }

    @Override
    public Classifier next() {
        Classifier next = get();
        shift();
        return next;
    }

    @Override
    public void remove() {
        parameterCombinationIterator.remove();
    }

    @Override
    public Classifier get() {
        int parameterValueCombinationIndex = parameterCombinationIterator.get();
        setup();
        parameterRangeSet.setParameterValueCombination(parameterValueCombinationIndex);
        return generate();
    }

    @Override
    public void shift() {
        parameterCombinationIterator.shift();
    }
}
