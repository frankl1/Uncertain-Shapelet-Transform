//package timeseriesweka.classifiers.nn;
//
//import timeseriesweka.classifiers.nn.Sampling.RandomRoundRobinSampler;
//import timeseriesweka.classifiers.nn.Sampling.Sampler;
//import utilities.ArrayUtilities;
//import utilities.Utilities;
//import weka.core.Instance;
//import weka.core.Instances;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
//import static utilities.Utilities.argMax;
//
//public class RandomStratifiedSampler implements Sampler {
//
//
////    @Override
////    public void setSeed(final long seed) {
////        random.setSeed(seed);
////    }
////
////    @Override
////    public void setRandom(final Random random) {
////        this.random = random;
////    }
////
////    private Instances[] instancesByClass;
////    private double[] classDistribution;
////    private double[] classSamplingProbabilities;
////    private int count;
////    private Random random = new Random();
////    private int maxCount;
////
////    @Override
////    public void setInstances(final Instances instances) {
////        instancesByClass = Utilities.instancesByClass(instances);
////        classDistribution = Utilities.classDistribution(instances);
////        classSamplingProbabilities = Utilities.classDistribution(instances);
////        count = 0;
////        maxCount = instances.numInstances();
////    }
////
////    @Override
////    public boolean hasNext() {
////        return count < maxCount;
////    }
////
////    private double findSampleClass() {
////        int[] highestProbabilityClasses = argMax(classSamplingProbabilities);
////        if(highestProbabilityClasses.length > 1) {
////            return highestProbabilityClasses[random.nextInt(highestProbabilityClasses.length)];
////        } else {
////            return highestProbabilityClasses[0];
////        }
////    }
////
////    @Override
////    public Instance next() {
////        int sampleClass = (int) findSampleClass();
////        Instances homogeneousInstances = instancesByClass[sampleClass]; // instances of the class value
////        Instance sampledInstance = homogeneousInstances.remove(random.nextInt(homogeneousInstances.numInstances()));
////        classSamplingProbabilities[sampleClass]--;
////        ArrayUtilities.add(classSamplingProbabilities, classDistribution);
////        return sampledInstance;
////    }
//}
