package development.go.Ee;

import timeseriesweka.classifiers.ensembles.EnsembleModule;
import timeseriesweka.classifiers.Nn.NeighbourWeighting.WeightByDistance;
import timeseriesweka.classifiers.Nn.Nn;
import timeseriesweka.Sampling.PredefinedSampler;
import timeseriesweka.measures.dtw.Dtw;
import utilities.ArrayUtilities;
import utilities.Utilities;
import weka.classifiers.AbstractClassifier;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PG extends AbstractClassifier {
    private EnsembleModule[] modules;
    private Random random = new Random();

    @Override
    public void buildClassifier(final Instances trainInstances) throws Exception {
        List<List<Instance>> instancesByClass = Utilities.instancesByClass(trainInstances);
        int size = 1;
        List<Integer> sizes = new ArrayList<>();
        for(List<Instance> instances : instancesByClass) {
            size *= instances.size();
            sizes.add(instances.size());
        }
        modules = new EnsembleModule[size];
        for(int i = 0; i < size; i++) {
            List<Integer> permutation = Utilities.fromPermutation(i, sizes);
            Instances instances = new Instances(trainInstances, 0);
            for(int j = 0; j < permutation.size(); j++) {
                List<Instance> classInstances = instancesByClass.get(j);
                instances.add(classInstances.get(permutation.get(j)));
            }
            Nn nn = new Nn();
            nn.setNeighbourWeighter(new WeightByDistance());
            nn.setSampleSizePercentage(1);
            PredefinedSampler predefinedSampler = new PredefinedSampler();
            predefinedSampler.setPredefinedInstances(instances);
            nn.setSampler(predefinedSampler);
            Dtw dtw = new Dtw();
            dtw.setWarpingWindow(1);
            nn.setDistanceMeasure(dtw);
            nn.setUseRandomTieBreak(false);
            nn.buildClassifier(trainInstances);
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
            double[] constituentDistribution = modules[i].getClassifier().distributionForInstance(testInstance);
            ArrayUtilities.normalise(constituentDistribution);
//            double acc = modules[i].trainResults.getAcc();
//            ArrayUtilities.multiply(constituentDistribution, acc);
            ArrayUtilities.add(distribution, constituentDistribution);
        }
        ArrayUtilities.normalise(distribution);
        return distribution;
    }

}
