//package timeseriesweka.classifiers.ee;
//
//import timeseriesweka.classifiers.AdvancedClassifier;
//import timeseriesweka.classifiers.ee.constituents.Constituent;
//import timeseriesweka.classifiers.ee.iteration.AbstractIndexIterator;
//import timeseriesweka.classifiers.ee.iteration.RandomIndexIterator;
//import timeseriesweka.classifiers.ee.selection.BestPerTypeSelector;
//import timeseriesweka.classifiers.ee.selection.Selector;
//import utilities.ClassifierResults;
//import utilities.Utilities;
//import utilities.instances.Folds;
//import weka.classifiers.Classifier;
//import weka.core.Instance;
//import weka.core.Instances;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class Ee implements AdvancedClassifier {
//    private final List<Constituent> constituents = new ArrayList<>();
//    private final AdvancedClassifier[] selectedCandidates = null;
//    private Long seed = null;
//    private Selector<AdvancedClassifier> candidateSelector = new BestPerTypeSelector<>();
//
//    public AbstractIndexIterator getConstituentIndexIterator() {
//        return constituentIndexIterator;
//    }
//
//    public void setConstituentIndexIterator(final AbstractIndexIterator constituentIndexIterator) {
//        this.constituentIndexIterator = constituentIndexIterator;
//    }
//
//    private AbstractIndexIterator constituentIndexIterator = new RandomIndexIterator();
//
//    public List<Constituent> getConstituents() {
//        return constituents;
//    }
//
//    private void reset(Instances trainInstances) {
//        for(Constituent constituent : constituents) {
//            constituent.getGenerator().setParameterRanges(trainInstances);
//            constituent.reset();
//        }
//        constituentIndexIterator.getRange().clear();
//        constituentIndexIterator.getRange().add(0, constituents.size());
//    }
//
//    private Folds foldInstances(Instances instances) {
//        Folds.Builder foldsBuilder = new Folds.Builder(instances);
//        if(seed != null) {
//            foldsBuilder.setSeed(seed);
//        }
//        return foldsBuilder.build();
//    }
//
//    @Override
//    public void buildClassifier(final Instances trainInstances) throws Exception {
//        reset(trainInstances);
//        Folds folds = foldInstances(trainInstances);
//        while (constituentIndexIterator.hasNext()) {
//            int generatorIndex = constituentIndexIterator.next();
//            Constituent constituent = constituents.get(generatorIndex);
//            if(!constituent.hasNext()) {
//                constituentIndexIterator.remove();
//            } else {
//                AdvancedClassifier candidateClassifier = constituent.next();
//                ClassifierResults results = new ClassifierResults();
//                for(int i = 0; i < folds.size(); i++) {
//                    Instances trainFold = folds.getTrain(i);
//                    Instances testFold = folds.getTest(i);
//                    candidateClassifier.buildClassifier(trainFold);
//                    results.addAll(candidateClassifier.predict(testFold));
//                }
//            }
//        }
//    }
//
//    @Override
//    public double[] distributionForInstance(final Instance testInstance) throws Exception {
//        if(selectedCandidates == null) {
//            throw new IllegalStateException("not trained");
//        }
//
//    }
//
//    @Override
//    public double classifyInstance(final Instance testInstance) throws Exception {
//        return Utilities.maxIndex(distributionForInstance(testInstance));
//    }
//
//    @Override
//    public void setSeed(final long seed) {
//
//    }
//}
