package ee.parameter;

import weka.classifiers.AbstractClassifier;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ClassifierParameterList {
    public static class ClassifierParameterPool {

        private final Function<Instances, AbstractClassifier> classifierSupplier;
        private final Function<Instances, ParameterPool> parameterPoolSupplier;

        public ClassifierParameterPool(final Function<Instances, AbstractClassifier> classifierSupplier, final Function<Instances, ParameterPool> parameterPoolSupplier) {
            this.classifierSupplier = classifierSupplier;
            this.parameterPoolSupplier = parameterPoolSupplier;
        }
    }

    private final List<ClassifierParameterPool> list = new ArrayList<>();

    
}
