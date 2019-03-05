package development.go.Ee;

import development.go.Ee.Constituents.ConstituentBuilder;
import timeseriesweka.classifiers.nn.Nn;
import timeseriesweka.measures.DistanceMeasure;
import utilities.ClassifierResults;
import weka.classifiers.AbstractClassifier;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class Ee extends AbstractClassifier {

    private static class Constituent {
        private final Nn nn;

        public Constituent(final Nn nn) {
            this.nn = nn;
            this.results = new ClassifierResults();
        }

        public Nn getNn() {
            return nn;
        }

        public ClassifierResults getResults() {
            return results;
        }

        private final ClassifierResults results;
    }

    private final List<ConstituentBuilder> constituentBuilders = new ArrayList<>();

    public void addConstituentBuilder(ConstituentBuilder builder) {
        constituentBuilders.add(builder);
    }

    private Random random = new Random();
    private Selector<Constituent> selector = new BestPerType<>(constituent -> constituent.getNn().getDistanceMeasure(), Comparator.comparingDouble(constituent -> constituent.getResults().acc));

    @Override
    public void buildClassifier(final Instances trainInstances) throws Exception {
        for(ConstituentBuilder constituentBuilder : constituentBuilders) {
            constituentBuilder.useInstances(trainInstances);
        }
        while (true) { // todo change

        }
    }
}
