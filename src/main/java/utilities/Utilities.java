/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package utilities;


import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.util.*;
import java.util.function.Function;
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

    public static double divide(double a, double b) {
        if(b == 0) {
            return 0;
        } else {
            return a / b;
        }
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

//    public static <B, A extends List<? extends B>> int[] fromPermutation(int combination, A a, Function<B, Integer> func) {
//        int maxCombination = numCombinations(binSizes) - 1;
//        if(combination > maxCombination || binSizes.length == 0 || combination < 0) {
//            throw new IllegalArgumentException();
//        }
//        int[] result = new int[binSizes.length];
//        for(int index = 0; index < binSizes.length; index++) {
//            int binSize = binSizes[index];
//            if(binSize > 1) {
//                result[index] = combination % binSize;
//                combination /= binSize;
//            } else {
//                result[index] = 0;
//                if(binSize <= 0) {
//                    throw new IllegalArgumentException();
//                }
//            }
//        }
//        return result;
//    }

    public static int[] fromPermutation(int combination, int... binSizes) {
        int maxCombination = numPermutations(binSizes) - 1;
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

    public static List<Integer> fromPermutation(int combination, List<Integer> binSizes) {
        int maxCombination = numPermutations(binSizes) - 1;
        if(combination > maxCombination || binSizes.size() == 0 || combination < 0) {
            throw new IllegalArgumentException();
        }
        List<Integer> result = new ArrayList<>();
        for(int index = 0; index < binSizes.size(); index++) {
            int binSize = binSizes.get(index);
            if(binSize > 1) {
                result.add(combination % binSize);
                combination /= binSize;
            } else {
                result.add(0);
                if(binSize <= 0) {
                    throw new IllegalArgumentException();
                }
            }
        }
        return result;
    }

    public static List<Integer> primitiveArrayToList(int[] values) {
        List<Integer> list = new ArrayList<>();
        for(int i = 0; i < values.length; i++) {
            list.add(i);
        }
        return list;
    }

    public static int toPermutation(int[] values, int[] binSizes) {
        return toPermutation(primitiveArrayToList(values), primitiveArrayToList(binSizes));
    }

    public static int toPermutation(List<Integer> values, List<Integer> binSizes) {
        if(values.size() != binSizes.size()) {
            throw new IllegalArgumentException("incorrect number of args");
        }
        int combination = 0;
        for(int i = binSizes.size() - 1; i >= 0; i--) {
            int binSize = binSizes.get(i);
            if(binSize > 1) {
                int value = values.get(i);
                combination *= binSize;
                combination += value;
            } else if(binSize <= 0) {
                throw new IllegalArgumentException();
            }
        }
        return combination;
    }

    public static int sum(List<Integer> array) {
        int sum = 0;
        for(Integer value : array) {
            sum += value;
        }
        return sum;
    }

    public static int numPermutations(List<Integer> binSizes) {
        List<Integer> maxValues = new ArrayList<>();
        for(int i = 0; i < binSizes.size(); i++) {
            maxValues.add(binSizes.get(i) - 1);
        }
        return toPermutation(maxValues, binSizes) + 1;
    }

    public static int numPermutations(int[] binSizes) {
        return numPermutations(primitiveArrayToList(binSizes));
    }

    public static void main(String[] args) {
//        for(int i = 0; i < 48; i++) {
//            int[] result = fromPermutation(i, 4, 3, 4);
//            for(int j : result) {
//                System.out.print(j);
//                System.out.print(", ");
//            }
//            System.out.println();
//        }
        System.out.println(asDirectoryPath("abc/def"));
    }



    public static void sortDatasetNames(String datasetsDirPath, List<String> datasetNames, Comparator<Instances> comparator) {
        datasetNames.sort((datasetNameA, datasetNameB) -> {
            File datasetFileA = new File(datasetsDirPath, datasetNameA);
            File datasetFileB = new File(datasetsDirPath, datasetNameB);
            try {
                Instances datasetA = Utilities.loadDataset(datasetFileA);
                Instances datasetB = Utilities.loadDataset(datasetFileB);
                return comparator.compare(datasetA, datasetB);
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalStateException(e);
            }
        });
    }

    public static List<String> readDatasetNameList(String datasetNameListPath) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(datasetNameListPath));
        List<String> datasetNames = new ArrayList<>();
        String datasetName = bufferedReader.readLine();
        while (datasetName != null) {
            datasetNames.add(datasetName);
            datasetName = bufferedReader.readLine();
        }
        bufferedReader.close();
        return datasetNames;
    }

    public static void writeDatasetNameList(String datasetNameListPath, List<String> datasetNames) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(datasetNameListPath));
        for(String datasetName : datasetNames) {
            bufferedWriter.write(datasetName);
            bufferedWriter.write("\n");
        }
        bufferedWriter.close();
    }

    public static List<Integer> naturalNumbersFromZero(int size) {
        List<Integer> list = new ArrayList<>();
        for(int i = 0; i <= size; i++) {
            list.add(i);
        }
        return list;
    }

    public static List<List<Instance>> instancesByClass(List<Instance> instances) {
        List<List<Instance>> instancesByClass = new ArrayList<>();
        int numClasses = instances.get(0).numClasses();
        for(int i = 0; i < numClasses; i++) {
            instancesByClass.add(new ArrayList<>());
        }
        for(Instance instance : instances) {
            instancesByClass.get((int) instance.classValue()).add(instance);
        }
        return instancesByClass;
    }

    public static Map<Double, List<Instance>> instancesByClassMap(Instances instances) {
        List<List<Instance>> instancesByClass = instancesByClass(instances);
        Map<Double, List<Instance>> instancesByClassMap = new TreeMap<>();
        for(int i = 0; i < instancesByClass.size(); i++) {
            instancesByClassMap.put((double) i, instancesByClass.get(i));
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
        box.accept(duration + box.get());
        return result;
    }

    public static void time(Runnable function, Box<Long> box) {
        long timeStamp = System.nanoTime();
        function.run();
        long duration = System.nanoTime() - timeStamp;
        box.accept(duration + box.get());
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

    public static int argMax(double[] array, Random random) {
        int[] indices = argMax(array);
        if(indices.length == 1) {
            return indices[0];
        }
        return indices[random.nextInt(indices.length)];
    }

    public static <A> A randomElement(List<A> list, Random random) {
        if(list.size() <= 0) {
            throw new IllegalArgumentException();
        } else if(list.size() == 1) {
            return list.get(0);
        } else {
            return list.get(random.nextInt(list.size()));
        }
    }


    public static <I, C> I best(Iterable<I> iterable, Comparator<C> comparator, Function<I, C> converter, Random random) {
        return randomElement(best(iterable.iterator(), comparator, converter), random);
    }

    public static <I, C> I best(Iterator<I> iterator, Comparator<C> comparator, Function<I, C> converter, Random random) {
        return randomElement(best(iterator, comparator, converter), random);
    }

    public static <I, C> Integer argBest(List<I> list, Comparator<C> comparator, Function<I, C> converter, Random random) {
        return randomElement(argBest(list, comparator, converter), random);
    }

    public static <I, C> List<I> best(Iterator<I> iterator, Comparator<C> comparator, Function<I, C> converter) {
        if(!iterator.hasNext()) {
            throw new IllegalArgumentException();
        }
        I best = iterator.next();
        C conversion = converter.apply(best);
        List<I> draws = new ArrayList<>();
        draws.add(best);
        while(iterator.hasNext()) {
            I other = iterator.next();
            C otherConversion = converter.apply(other);
            double comparisonResult = comparator.compare(otherConversion, conversion);
            if(comparisonResult >= 0) {
                if(comparisonResult > 0) {
                    draws.clear();
                    best = other;
                    conversion = otherConversion;
                }
                draws.add(other);
            }
        }
        return draws;
    }

    public static <I, C> List<Integer> argBest(List<I> list, Comparator<C> comparator, Function<I, C> converter) {
        if(list.size() <= 0) {
            throw new IllegalArgumentException();
        }
        I best = list.get(0);
        C conversion = converter.apply(best);
        List<Integer> draws = new ArrayList<>();
        draws.add(0);
        for(int i = 1; i < list.size(); i++) {
            I other = list.get(i);
            C otherConversion = converter.apply(other);
            double comparisonResult = comparator.compare(otherConversion, conversion);
            if(comparisonResult >= 0) {
                if(comparisonResult > 0) {
                    draws.clear();
                    best = other;
                    conversion = otherConversion;
                }
                draws.add(i);
            }
        }
        return draws;
    }

    public static <I, C> C bestConvertion(List<I> list, Comparator<C> comparator, Function<I, C> converter, Random random) {
        List<C> bestConvertions = bestConvertion(list, comparator, converter);
        return randomElement(bestConvertions, random);
    }

    public static <I, C> List<C> bestConvertion(List<I> list, Comparator<C> comparator, Function<I, C> converter) {
        if(list.size() <= 0) {
            throw new IllegalArgumentException();
        }
        C best = converter.apply(list.get(0));
        List<C> draws = new ArrayList<>();
        draws.add(best);
        for(int i = 1; i < list.size(); i++) {
            I other = list.get(i);
            C otherConversion = converter.apply(other);
            double comparisonResult = comparator.compare(otherConversion, best);
            if(comparisonResult >= 0) {
                if(comparisonResult > 0) {
                    draws.clear();
                    best = otherConversion;
                }
                draws.add(otherConversion);
            }
        }
        return draws;
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

    public static void setOpenPermissions(File file) {
        File parent = file.getParentFile();
        if(parent != null) setOpenPermissions(parent);
        file.setExecutable(true, true);
        file.setWritable(true, true);
        file.setReadable(true, true);
        file.setExecutable(true, false);
        file.setWritable(true, false);
        file.setReadable(true, false);
    }

    public static void mkdir(File dir) {
        File parentFile = dir.getParentFile();
        if (parentFile != null) {
            mkdir(parentFile);
        }
        if(dir.mkdirs()) {
            setOpenPermissions(dir);
        }
    }

    public static List<Double> incrementalDiffList(double min, double max, int size) {
        if(min > max) {
            double temp = min;
            min = max;
            max = temp;
        }
        List<Double> values = new ArrayList<>();
        values.add(min);
        double diff = (max - min) / (size - 1);
        double value = min;
        for(int i = 1; i <= size - 2; i++) {
            value = value + diff;
            values.add(value);
        }
        values.add(max);
        return values;
    }

    public static List<Integer> incrementalDiffListInt(int min, int max, int size) {
        if(min > max) {
            int temp = min;
            min = max;
            max = temp;
        }
        List<Integer> values = new ArrayList<>();
        values.add(min);
        for(int i = 1; i <= size - 2; i++) {
            values.add((int) (((double) (max - min)) * ((double) i / (size - 1)) + min));
        }
        values.add(max);
        return values;
    }

    public static evaluation.storage.ClassifierResults trainAndTest(Classifier classifier, Instances trainInstances, Instances testInstances, Random random) throws Exception {
        classifier.buildClassifier(trainInstances);
        return test(classifier, testInstances, random);
    }

    public static evaluation.storage.ClassifierResults trainAndTest(Classifier classifier, Instances trainInstances, Instances testInstances, evaluation.storage.ClassifierResults results, Random random) throws Exception {
        classifier.buildClassifier(trainInstances);
        return test(classifier, testInstances, results, random);
    }

    public static evaluation.storage.ClassifierResults test(Classifier classifier, Instances testInstances, evaluation.storage.ClassifierResults results, Random random) throws Exception {
        for(Instance testInstance : testInstances) {
            double classValue = testInstance.classValue();
            double[] predictions = classifier.distributionForInstance(testInstance);
            results.addPrediction(classValue, predictions, argMax(predictions, random), -1, null);
        }
        return results;
    }

    public static evaluation.storage.ClassifierResults test(Classifier classifier, Instances testInstances, Random random) throws Exception {
        evaluation.storage.ClassifierResults results = test(classifier, testInstances, new evaluation.storage.ClassifierResults(), random);
        results.setNumClasses(testInstances.numClasses());
        results.findAllStatsOnce();
        return results;
    }

    public static String toString(final int[] combination) {
        StringBuilder stringBuilder = new StringBuilder();
        for(int i : combination) {
            stringBuilder.append(i);
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }

    public static void mkdirParent(final File file) {
        File parent = file.getParentFile();
        if(parent != null) {
            mkdir(parent);
        }
    }

    public static List<Double> linearInterpolate(double min, double max, int num) {
        List<Double> list = new ArrayList<>();
        for(int i = 0; i < num; i++) {
            if(min == 0 && max == 1) {
                list.add((double) (i / num));
            } else {
                list.add(min + (max - min) * ((double) i / (num - 1)));
            }
        }
        return list;
    }


    public static List<Double> linearInterpolate(int size, int divider) {
        List<Double> list = new ArrayList<>();
        for(int i = 0; i < size; i++) {
            list.add((double) i / divider);
        }
        return list;
    }

    public static String join(final String[] options, final String s) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(options[0]);
        for(int i = 1; i < options.length; i++) {
            stringBuilder.append(s);
            stringBuilder.append(options[i]);
        }
        return stringBuilder.toString();
    }

    public static List<Integer> zeroInts(int size) {
        List<Integer> list = new ArrayList<>();
        for(int i = 0; i < size; i++) {
            list.add(0);
        }
        return list;
    }

    public static List<Double> zeroDoubles(int size) {
        List<Double> list = new ArrayList<>();
        for(int i = 0; i < size; i++) {
            list.add(0.0);
        }
        return list;
    }
}
