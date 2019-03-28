package development.go.Ee;

import timeseriesweka.classifiers.ensembles.EnsembleModule;
import timeseriesweka.classifiers.Nn.NeighbourWeighting.WeightByDistance;
import timeseriesweka.classifiers.Nn.Nn;
import timeseriesweka.measures.dtw.Dtw;
import utilities.ArrayUtilities;
import weka.classifiers.AbstractClassifier;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Random;

public class TSEF extends AbstractClassifier {
    private int num = 100;
    private EnsembleModule[] modules = new EnsembleModule[num];
    private int[][] intervals = new int[num][2];
    private Random random = new Random();

    private Instances trimInterval(Instances instances, int min, int max) {
        Instances trimmed = new Instances(instances);
        for(int i = trimmed.numAttributes() - 2; i > max; i--) {
            trimmed.deleteAttributeAt(i);
        }
        for(int i = min - 1; i >= 0; i--) {
            trimmed.deleteAttributeAt(i);
        }
        return trimmed;
    }

    private Instance trimInterval(Instance instance, int min, int max) {
        Instances dataset = new Instances(instance.dataset(), 0);
        dataset.add(instance);
        for(int i = dataset.numAttributes() - 2; i > max; i--) {
            dataset.deleteAttributeAt(i);
        }
        for(int i = min - 1; i >= 0; i--) {
            dataset.deleteAttributeAt(i);
        }
        return dataset.get(0);
    }


    @Override
    public void buildClassifier(final Instances trainInstances) throws Exception {
        int instanceLength = trainInstances.numAttributes() - 1;
        int minIntervalLength = (int) Math.sqrt(instanceLength);
        for(int i = 0; i < modules.length; i++) {
            Nn nn = new Nn();
            nn.setNeighbourWeighter(new WeightByDistance());
//            Nn.setSampleSizePercentage((double) trainInstances.numClasses() / trainInstances.numInstances());
            Dtw dtw = new Dtw();
            dtw.setWarpingWindow(1);
            nn.setDistanceMeasure(dtw);
            nn.setUseRandomTieBreak(false);
            int minIndex = random.nextInt(instanceLength - minIntervalLength);
            int maxIndex = random.nextInt(instanceLength - minIndex + 1 - minIntervalLength) + minIntervalLength + minIndex;
            if(maxIndex < minIndex) {
                int temp = maxIndex;
                maxIndex = minIndex;
                minIndex = temp;
            }
            System.out.println(i + ": " + minIndex + " - " + maxIndex);
            intervals[i] = new int[2];
            intervals[i][0] = minIndex;
            intervals[i][1] = maxIndex;
            Instances intervalTrainInstances = trimInterval(trainInstances, minIndex, maxIndex);
            nn.buildClassifier(intervalTrainInstances);
            EnsembleModule module = new EnsembleModule();
            module.trainResults = nn.getTrainResults();
            module.setClassifier(nn);
            modules[i] = module;
        }
    }

    @Override
    public double[] distributionForInstance(final Instance testInstance) throws Exception {
        double[] distribution = new double[testInstance.numClasses()];
        for(int i = 0; i < modules.length; i++) {
            Instance trimmedTestInstance = trimInterval(testInstance, intervals[i][0], intervals[i][1]);
            double[] constituentDistribution = modules[i].getClassifier().distributionForInstance(trimmedTestInstance);
            ArrayUtilities.normalise(constituentDistribution);
            ArrayUtilities.multiply(constituentDistribution, modules[i].trainResults.getAcc());
            ArrayUtilities.add(distribution, constituentDistribution);
        }
        ArrayUtilities.normalise(distribution);
        return distribution;
    }

}
