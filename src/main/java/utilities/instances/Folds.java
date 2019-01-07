package utilities.instances;

import utilities.ClassifierResults;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Random;

import static utilities.instances.Distribution.binClasses;

public class Folds implements Iterable<TrainTestSplit>, Serializable {
    private Folds(Instances[] folds, int numInstances, int numClasses) {
        this.folds = folds;
        this.numInstances = numInstances;
        this.numClasses = numClasses;
    }

    private final Instances[] folds;
    private final int numInstances;
    private final int numClasses;

    public Instances getTrain(int index) {
        Instances train = new Instances(folds[0], 0);
        for(int i = 0; i < folds.length; i++) {
            if(i != index) {
                train.addAll(folds[i]);
            }
        }
        return train;
    }

    public int getNumInstances() {
        return numInstances;
    }

    public int getNumClasses() {
        return numClasses;
    }

    public Instances getTest(int index) {
        return folds[index];
    }

    public TrainTestSplit getTrainTestSplit(int index) {
        return new TrainTestSplit(getTrain(index), getTest(index));
    }

    public int getNumFolds() {
        return folds.length;
    }

    @Override
    public Iterator<TrainTestSplit> iterator() {
        return new Iterator<TrainTestSplit>() {
            private int foldIndex = 0;

            @Override
            public boolean hasNext() {
                return foldIndex < getNumFolds();
            }

            @Override
            public TrainTestSplit next() {
                foldIndex++;
                return getTrainTestSplit(foldIndex - 1);
            }
        };
    }

    // todo some way to put instances back in the right order

    public static class Builder implements Serializable {

        public Builder(Instances instances, int numFolds) {
            if(instances == null) {
                throw new IllegalArgumentException("Instances not specified");
            } else if(numFolds < 1) {
                throw new IllegalArgumentException("Invalid number of folds");
            }
            int numInstances = instances.numInstances();
            if(numFolds > numInstances) {
                throw new IllegalArgumentException("number of folds too large");
            }
            this.numFolds = numFolds;
            this.instances = instances;
        }

        public Builder(Instances instances) {
            this(instances, instances.numInstances());
        }

        private Folds fold() {
            int numInstancesPerFold = instances.numInstances() / numFolds;
            int overflow = instances.numInstances() % numFolds;
            Instances[] folds = new Instances[numFolds];
            for(int i = 0; i < folds.length; i++) {
                int numInstancesForThisFold = numInstancesPerFold;
                if(i < overflow) {
                    numInstancesForThisFold++;
                }
                folds[i] = new Instances(instances, 0);
                for(int j = 0; j < numInstancesForThisFold; j++) {
                    folds[i].add(instances.instance(random.nextInt(instances.numInstances())));
                }
            }
            return new Folds(folds, instances.numInstances(), instances.numClasses());
        }

        private Folds stratifiedFold() {
            throw new UnsupportedOperationException("Issue with stratified folding, needs to be fixed"); // todo what happens when num folds is same or close to num insts??
//            Instances[] folds = new Instances[numFolds];
//            for(int foldIndex = 0; foldIndex < folds.length; foldIndex++) {
//                folds[foldIndex] = new Instances(instances,0);
//            }
//            Instances[] classBins = binClasses(instances);
//            for(Instances classBin : classBins) {
//                int foldIndex = 0;
//                while (classBin.numInstances() > 0) {
//                    int instanceIndex = random.nextInt(classBin.numInstances());
//                    Instance removedInstance = classBin.remove(instanceIndex);
//                    folds[foldIndex].add(removedInstance);
//                    foldIndex = (foldIndex + 1) % numFolds;
//                }
//            }
//            return new Folds(folds, instances.numInstances(), instances.numClasses());
        }

        public Folds build() {
            if(stratify) {
                return stratifiedFold();
            } else {
                return fold();
            }
        }

        private boolean stratify = false;
        private final Random random = new Random();

        public boolean stratify() {
            return stratify;
        }

        public Builder stratify(boolean stratify) {
            this.stratify = stratify;
            return this;
        }

        public Builder setSeed(long seed) {
            random.setSeed(seed);
            return this;
        }

        private final int numFolds;
        private final Instances instances;
    }
}
