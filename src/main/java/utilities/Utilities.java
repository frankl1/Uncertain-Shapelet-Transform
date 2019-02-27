package utilities;

import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.util.*;
import java.util.function.Supplier;

public class Utilities {
    public static final int size(double[][] matrix) {
        int population = 0;
        for(int i = 0; i < matrix.length; i++) {
            population += matrix[i].length;
        }
        return population;
    }
/**

* 6/2/19: bug fixed so it properly ignores the class value, only place its used
* is in measures.DistanceMeasure
 * @param instance
 * @return array of doubles with the class value removed
*/
    public static final double[] extractTimeSeries(Instance instance) {
        if(instance.classIsMissing()) {
            return instance.toDoubleArray();
        } else {
            double[] timeSeries = new double[instance.numAttributes() - 1];
            for(int i = 0; i < instance.numAttributes(); i++) {
                if(i < instance.classIndex()) {
                    timeSeries[i] = instance.value(i);
                } else if (i != instance.classIndex()){
                    timeSeries[i - 1] = instance.value(i);
                }
            }
            return timeSeries;
        }
    }

    public static final double min(double... values) {
        double min = values[0];
        for(int i = 1; i < values.length; i++) {
            min = Math.min(min, values[i]);
        }
        return min;
    }

    public static final double sum(double[] array, int start, int end) {
        double sum = 0;
        for(int i = start; i < end; i++) {
            sum += array[i];
        }
        return sum;
    }

    public static final double sum(double[] array) {
        return sum(array, 0, array.length);
    }

    public static final double[] normalise(double[] array) {
        return normalise(array, sum(array));
    }

    public static double[] normalise(double[] array, double against) {
        for(int i = 0; i < array.length; i++) {
            array[i] /= against;
        }
        return array;
    }

    public static final double[] normalisePercentage(double[] array) {
        return normalise(array, sum(array) / 100);
    }

    public static final String sanitiseFolderPath(String path) {
        if(path.charAt(path.length() - 1) != '/') {
            path = path + '/';
        }
        return path;
    }

    public static final double max(double... values) {
        double max = values[0];
        for(int i = 1; i < values.length; i++) {
            max = Math.max(max, values[i]);
        }
        return max;
    }

    public static final double[] divide(double[] a, double[] b) {
        double[] result = new double[a.length];
        for(int i = 0; i < result.length; i++) {
            result[i] = a[i] / b[i];
        }
        return result;
    }

    public static final double[] divide(double[] array, int divisor) {
        double[] result = new double[array.length];
        for(int i = 0; i < result.length; i++) {
            result[i] = array[i] / divisor;
        }
        return result;
    }

    public static final int maxIndex(double[] array) {
        int maxIndex = 0;
        for (int i = 1; i < array.length; i++) {
            if(array[maxIndex] < array[i]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    public static final void zeroOrMore(int i) {
        if(i < 0) {
            throw new IllegalArgumentException("less than zero");
        }
    }

    public static final void moreThanOrEqualTo(int a, int b) {
        if(b < a) {
            throw new IllegalArgumentException("b cannot be less than a");
        }
    }

    public static final double log(double value, double base) { // beware, this is inaccurate due to floating point error!
        return Math.log(value) / Math.log(base);
    }

    public static final double[] interpolate(double min, double max, int num) {
        double[] result = new double[num];
        double diff = (max - min) / (num - 1);
        for(int i = 0; i < result.length; i++) {
            result[i] = min + diff * i;
        }
        return result;
    }

    // todo cleanup

    public static int[] fromCombination(int combination, int... binSizes) {
        int maxCombination = numCombinations(binSizes);
        if(combination > maxCombination || binSizes.length == 0 || combination < 0) {
            throw new IllegalArgumentException();
        }
        int[] result = new int[binSizes.length];
        for(int index = 0; index < binSizes.length; index++) {
            int binSize = binSizes[index];
            if(binSize > 1) {
                result[index] = combination % binSize;
                combination /= binSize;
            } else {
                result[index] = 0;
                if(binSize <= 0) {
                    throw new IllegalArgumentException();
                }
            }
        }
        return result;
    }

    public static int toCombinationOld(int... indices) {
        if(indices.length % 2 != 0) {
            throw new IllegalArgumentException("incorrect number of args, must be index followed by bin size");
        }
        int combination = 0;
        for(int i = indices.length / 2 - 1; i >= 0; i--) {
            int binSize = indices[i * 2 + 1];
            int value = indices[i * 2];
            combination *= binSize;
            combination += value;
        }
        return combination;
    }

    public static int toCombination(int[] values, int[] binSizes) {
        if(values.length != binSizes.length) {
            throw new IllegalArgumentException("incorrect number of args");
        }
        int combination = 0;
        for(int i = binSizes.length - 1; i >= 0; i--) {
            int binSize = binSizes[i];
            if(binSize > 1) {
                int value = values[i];
                combination *= binSize;
                combination += value;
            } else if(binSize <= 0) {
                throw new IllegalArgumentException();
            }
        }
        return combination;
    }

    public static int numCombinations(int[] binSizes) {
        int[] maxValues = new int[binSizes.length];
        for(int i = 0; i < binSizes.length; i++) {
            maxValues[i] = binSizes[i] - 1;
        }
        return toCombination(maxValues, binSizes) + 1;
    }

    public static void main(String[] args) {
//        for(int i = 0; i < 48; i++) {
//            int[] result = fromCombination(i, 4, 3, 4);
//            for(int j : result) {
//                System.out.print(j);
//                System.out.print(", ");
//            }
//            System.out.println();
//        }
        System.out.println(asDirectoryPath("abc/def"));
    }

    public static Instances[] instancesByClass(Instances instances) {
        Instances[] instancesByClass = new Instances[instances.numClasses()];
        for(int i = 0; i < instancesByClass.length; i++) {
            instancesByClass[i] = new Instances(instances, 0);
        }
        for(Instance instance : instances) {
            instancesByClass[(int) instance.classValue()].add(instance);
        }
        return instancesByClass;
    }

    public static Map<Double, Instances> instancesByClassMap(Instances instances) {
        Instances[] instancesByClass = instancesByClass(instances);
        Map<Double, Instances> instancesByClassMap = new TreeMap<>();
        for(int i = 0; i < instancesByClass.length; i++) {
            instancesByClassMap.put((double) i, instancesByClass[i]);
        }
        return instancesByClassMap;
    }

    public static double[] classDistribution(Instances instances) {
        double[] distribution = new double[instances.numClasses()];
        for(Instance instance : instances) {
            distribution[(int) instance.classValue()]++;
        }
        normalise(distribution);
        return distribution;
    }

    public static List<List<Integer>> instanceIndicesByClass(Instances instances) {
        List<List<Integer>> instanceIndicesByClass = new ArrayList<>();
        for(int i = 0; i < instances.numClasses(); i++) {
            instanceIndicesByClass.add(new ArrayList<>());
        }
        for(int i = 0; i < instances.numInstances(); i++) {
            Instance instance = instances.get(i);
            int index = (int) instance.classValue();
            instanceIndicesByClass.get(index).add(i);
        }
        return instanceIndicesByClass;
//        int[][] instanceIndicesByClassResult = new int[instanceIndicesByClass.size()][];
//        for(int i = 0; i < instanceIndicesByClass.size(); i++) {
//            List<Integer> indices = instanceIndicesByClass.get(i);
//            instanceIndicesByClassResult[i] = new int[indices.size()];
//            for(int j = 0; j < indices.size(); j++) {
//                instanceIndicesByClassResult[i][j] = indices.get(j);
//            }
//        }
//        return instanceIndicesByClassResult;
    }

    public static Instances loadDataset(File datasetDir) throws IOException {
        File datasetFile = new File(datasetDir, datasetDir.getName() + ".arff");
        if(datasetFile.exists()) {
            return instancesFromFile(datasetFile);
        }
        datasetFile = new File(datasetDir, datasetDir.getName() + "_TRAIN.arff");
        File testDatasetFile = new File(datasetDir, datasetDir.getName() + "_TEST.arff");
        if(datasetFile.exists() && testDatasetFile.exists()) {
            Instances instances = instancesFromFile(datasetFile);
            instances.addAll(instancesFromFile(testDatasetFile));
            return instances;
        }
        throw new IllegalArgumentException("failed to load: " + datasetDir.getPath());
    }

    public static Instances[] loadSplitInstances(File datasetDir) throws IOException {
        File trainFile = datasetDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File file) {
                String name = file.getName();
                int index = name.indexOf("_");
                if(index < 0) {
                    return false;
                }
                String end = name.substring(index + 1);
                return end.equalsIgnoreCase("train.arff");
            }
        })[0];
        File testFile = datasetDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File file) {
                String name = file.getName();
                int index = name.indexOf("_");
                if(index < 0) {
                    return false;
                }
                String end = name.substring(index + 1);
                return end.equalsIgnoreCase("test.arff");
            }
        })[0]; // todo checks / exceptions
        Instances testInstances = instancesFromFile(testFile);
        Instances trainInstances = instancesFromFile(trainFile);
        return new Instances[] {trainInstances, testInstances};
    }

    public static Instances loadDataset(String datasetDir) throws IOException {
        return loadDataset(new File(datasetDir));
    }

    public static Instances instancesFromFile(File file) throws IOException {
        Instances instances = new Instances(new BufferedReader(new FileReader(file)));
        instances.setClassIndex(instances.numAttributes() - 1);
        return instances;
    }

    public static Instances instancesFromFile(String path) throws IOException {
        return instancesFromFile(new File(path));
    }

    public static <A> A time(Supplier<A> function, Box<Long> box) {
        long timeStamp = System.nanoTime();
        A result = function.get();
        long duration = System.nanoTime() - timeStamp;
        box.setContents(duration + box.getContents());
        return result;
    }

    public static void time(Runnable function, Box<Long> box) {
        long timeStamp = System.nanoTime();
        function.run();
        long duration = System.nanoTime() - timeStamp;
        box.setContents(duration + box.getContents());
    }

    public static String asString(double[] array) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(array[0]);
        for(int i = 1; i < array.length; i++) {
            stringBuilder.append(", ");
            stringBuilder.append(array[i]);
        }
        return stringBuilder.toString();
    }

    public static int[] argMax(double[] array) {
        List<Integer> indices = new ArrayList<>();
        double max = array[0];
        indices.add(0);
        for(int i = 1; i < array.length; i++) {
            if(array[i] >= max) {
                if(array[i] > max) {
                    max = array[i];
                    indices.clear();
                }
                indices.add(i);
            }
        }
        int[] indicesCopy = new int[indices.size()];
        for(int i = 0; i < indicesCopy.length; i++) {
            indicesCopy[i] = indices.get(i);
        }
        return indicesCopy;
    }

    public static void percentageCheck(double percentage) {
        if(percentage < 0) {
            throw new IllegalArgumentException("percentage cannot be less than 0: " + percentage);
        } else if(percentage > 1) {
            throw new IllegalArgumentException("percentage cannot be more than 1: " + percentage);
        }
    }

    public static Instances instanceToInstances(Instance instance) {
        Instances instances = new Instances(instance.dataset(), 0);
        instances.add(instance);
        return instances;
    }

    public static String asDirectoryPath(final String path) {
        return new File(path).getPath() + "/"; // todo make sure this outputs trailing slash
    }
}
