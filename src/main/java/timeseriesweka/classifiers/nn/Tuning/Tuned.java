package timeseriesweka.classifiers.nn.Tuning;

import weka.core.Instances;

import java.util.function.Function;

public class Tuned extends AbstractTuned {
    public Tuned(final Function<Instances, PermutationBuilder> permutationBuilderFunction) {
        this.permutationBuilderFunction = permutationBuilderFunction;
    }

    public Function<Instances, PermutationBuilder> getPermutationBuilderFunction() {
        return permutationBuilderFunction;
    }

    public void setPermutationBuilderFunction(final Function<Instances, PermutationBuilder> permutationBuilderFunction) {
        this.permutationBuilderFunction = permutationBuilderFunction;
    }

    private Function<Instances, PermutationBuilder> permutationBuilderFunction;


}
