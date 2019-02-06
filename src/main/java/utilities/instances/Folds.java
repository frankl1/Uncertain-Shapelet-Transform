package utilities.instances;

import utilities.range.Range;
import weka.core.Instance;
import weka.core.Instances;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;

public class Folds implements Iterable<TrainTestSplit>, Serializable {
    private Folds(Instances instances, Range[] ranges) {
        this.instances = instances;
        this.ranges = ranges;
    }

    private final Range[] ranges;
    private final Instances instances;

    public Instances getTrain(int index) {
        Instances train = new Instances(instances);
        Range range = ranges[index];
        for(int i = range.size() - 1; i >= 0; i--) {
            train.remove((int) range.get(i));
        }
        return train;
    }

    public int getNumInstances() {
        return instances.numInstances();
    }

    public int getNumClasses() {
        return instances.numClasses();
    }

    public Instances getTest(int index) {
        Instances test = new Instances(instances, 0);
        Range range = ranges[index];
        for(int i = 0; i < range.size(); i++) {
            test.add(instances.get(range.get(i)));
        }
        return test;
    }

    public TrainTestSplit getTrainTestSplit(int index) {
        return new TrainTestSplit(getTrain(index), getTest(index));
    }

    public int size() {
        return ranges.length;
    }

    @Override
    public Iterator<TrainTestSplit> iterator() {
        return new Iterator<TrainTestSplit>() {
            private int foldIndex = 0;

            @Override
            public boolean hasNext() {
                return foldIndex < size();
            }

            @Override
            public TrainTestSplit next() {
                foldIndex++;
                return getTrainTestSplit(foldIndex - 1);
            }
        };
    }

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
            this.instances = new Instances(instances);
        }

        public Builder(Instances instances) {
            this(instances, instances.numInstances());
        }

        public Folds build() {
            if(stratify) {
                instances.sort(Comparator.comparingDouble(Instance::classValue));
            } else {
                Collections.shuffle(instances, random);
            }
            Range[] ranges = new Range[numFolds];
            for(int i = 0; i < ranges.length; i++) {
                ranges[i] = new Range();
            }
            int index = 0;
            while (index < instances.numInstances()) {
                ranges[index % ranges.length].add(index);
                index++;
            }
            return new Folds(instances, ranges);
        }

        private boolean stratify = false;
        private final Random random = new Random();

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
