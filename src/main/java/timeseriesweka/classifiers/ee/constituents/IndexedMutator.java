package timeseriesweka.classifiers.ee.constituents;

import utilities.range.ValueRange;

public class IndexedMutator<A, B> implements IndexedMutatorInterface<A, Integer> {
    private Mutable<A, B> mutator;
    private ValueRange<B> valueRange;

    public Mutable<A, B> getMutator() {
        return mutator;
    }

    public void setMutator(final Mutable<A, B> mutator) {
        this.mutator = mutator;
    }

    public ValueRange<B> getValueRange() {
        return valueRange;
    }

    public void setValueRange(final ValueRange<B> valueRange) {
        this.valueRange = valueRange;
    }

    public IndexedMutator(Mutable<A, B> mutator) {
        this(mutator, new ValueRange<>());
    }

    public IndexedMutator(Mutable<A, B> mutator, ValueRange<B> valueRange) {
        this.mutator = mutator;
        this.valueRange = valueRange;
    }

    @Override
    public <C extends A, D extends Integer> void setValue(final C subject, final D value) {
        mutator.setValue(subject, valueRange.get(value));
    }

    public <C extends A> Integer getValue(final C subject) {
        return valueRange.get(mutator.getValue(subject));
    }

    public int size() {
        return valueRange.size();
    }
}
