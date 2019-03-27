package timeseriesweka.classifiers.nn.Tuning;

import weka.core.Instances;

import java.util.function.Function;

public class TunedDtw extends AbstractTuned {
    @Override
    protected Function<Instances, PermutationBuilder> getPermutationBuilderFunction() {
        return null;
    }
}
