package development.go;

import timeseriesweka.classifiers.ee.constituents.generators.*;
import timeseriesweka.measures.DistanceMeasure;
import utilities.ClassifierResults;
import utilities.ClassifierStats;
import utilities.InstanceTools;
import utilities.Utilities;
import utilities.instances.Folds;
import weka.core.Instances;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.zip.GZIPInputStream;

public class SamplingExperimentAnalysis {

    private static String parseFileName(String str) {
        int index = str.lastIndexOf(".");
        if(index >= 0) {
            return str.substring(0, index);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static String parseVariable(String str, String variableName) {
        int index = str.indexOf(variableName + "=");
        if(index >= 0) {
            str = str.substring(index);
            int nextIndex = str.indexOf(",");
            if(nextIndex < 0) {
                nextIndex = str.lastIndexOf(".");
            }
            if (nextIndex >= 0) {
                return str.substring(1 + variableName.length(), nextIndex);
            }
        }
        throw new IllegalArgumentException();
    }

    private static ClassifierStats loadStats(File file) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(
            new GZIPInputStream(
                new BufferedInputStream(
                    new FileInputStream(file)
                )
            )
        );
        Object object = in.readObject();
        return (ClassifierStats) object;
    }

    private static List<String> datasetNamesFromFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        List<String> datasetNames = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            datasetNames.add(line.trim());
        }
        return datasetNames;
    }

    public static void main(String[] args) throws IOException {
        int numFolds = 30;
        long time = System.nanoTime();
        File resultsDir = new File("/run/user/33190/gvfs/sftp:host=hpc.uea.ac.uk/gpfs/home/vte14wgu/experiments/sample-train/results/tick-seed");
//        File datasetsDir = new File("/run/user/33190/gvfs/sftp:host=hpc.uea.ac.uk/gpfs/home/ajb/TSCProblems2018");
        List<String> datasetNames = datasetNamesFromFile(new File("/run/user/33190/gvfs/sftp:host=hpc.uea.ac.uk/gpfs/home/vte14wgu/experiments/sample-train/datasetList.txt"));
        datasetNames.sort(String::compareToIgnoreCase);
        //  distM       dmParam     fold        percent  acc
        Map<String, Map<String, Map<Integer, Map<Double, ClassifierStats>>>> results = new TreeMap<>();
        for(String datasetName : datasetNames) {
            System.out.println(datasetName);
            File dataset = new File(resultsDir, datasetName);
            File[] resultFiles = dataset.listFiles();
            if(resultFiles != null) {
                for(File resultFile : resultFiles) {
                    try {
                        ClassifierStats stats = loadStats(resultFile);
                        String fileName = resultFile.getName();
//                            System.out.println(fileName);
                        String distanceMeasureName = parseVariable(fileName, "d");
                        String distanceMeasureParameters = parseVariable(fileName, "q");
                        double percentageTrainSample = Double.parseDouble(parseVariable(fileName, "p"));
                        int fold = Integer.parseInt(parseVariable(fileName, "f"));
                        Map<String, Map<Integer, Map<Double, ClassifierStats>>> distanceMeasureResults = results.computeIfAbsent(distanceMeasureName, key -> new TreeMap<>());
                        Map<Integer, Map<Double, ClassifierStats>> foldResults = distanceMeasureResults.computeIfAbsent(distanceMeasureParameters, key -> new TreeMap<>());
                        Map<Double, ClassifierStats> percentageSampleResults = foldResults.computeIfAbsent(fold, key -> new TreeMap<>());
                        percentageSampleResults.put(percentageTrainSample, stats);
                    } catch (IOException e) {
//                            e.printStackTrace();
                        System.out.println("ioe");
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        System.out.println(System.nanoTime() - time);
        System.out.println(Runtime.getRuntime().totalMemory());
        System.out.println(Runtime.getRuntime().maxMemory());
        System.out.println(Runtime.getRuntime().freeMemory());
    }
}
