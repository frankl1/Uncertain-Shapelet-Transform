package development.go;

import utilities.ClassifierResults;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.zip.GZIPInputStream;

public class SamplingExperimentAnalysis {

    private static String parseVariableValue(String id, String str) {
        int index = str.indexOf(id + "=");
        str = str.substring(index + 2);
        index = str.indexOf(",");
        if(index < 0) {
            index = str.lastIndexOf(".");
        }
        str = str.substring(0, index);
        return str;
    }

    private static ClassifierResults loadClassifierResults(File file) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(
            new GZIPInputStream(
                new BufferedInputStream(
                    new FileInputStream(file)
                )
            )
        );
        ClassifierResults results = (ClassifierResults) in.readObject();
        in.close();
        return results;
    }

    public static void main(String[] args) throws Exception {
        File resultsDir = new File("/run/user/33190/gvfs/sftp:host=hpc.uea.ac.uk/gpfs/home/vte14wgu/experiments/sample-train/results");
        String[] distanceMeasureNames = {"dtw", "ddtw", "erp", "lcss", "wdtw", "wddtw", "twe", "euclidean", "msm"};
        String[] percentages = {"0.0", "0.25", "0.5", "0.75", "1.0"};
        Arrays.sort(distanceMeasureNames);
        Arrays.sort(percentages);
        List<String> percentagesList = Arrays.asList(percentages);
        List<String> distanceMeasureNamesList = Arrays.asList(distanceMeasureNames);
        StringBuilder[][] stringBuilders = new StringBuilder[distanceMeasureNames.length][percentages.length];
        for(int i = 0; i < stringBuilders.length; i++) {
            for(int j = 0; j < stringBuilders[i].length; j++) {
                stringBuilders[i][j] = new StringBuilder();
            }
        }
        File[] datasetDirs = resultsDir.listFiles();
        Arrays.sort(datasetDirs, (file, t1) -> file.getName().compareToIgnoreCase(t1.getName()));
        for(File datasetDir : datasetDirs) {
            System.out.println(datasetDir.getName());
            File[] resultFiles = datasetDir.listFiles();
            Arrays.sort(resultFiles, (file, other) -> {
                double result = Double.parseDouble(parseVariableValue("p", file.getName())) - Double.parseDouble(parseVariableValue("p", other.getName()));
                if(result == 0) {
                    return Integer.parseInt(parseVariableValue("n", file.getName())) - Integer.parseInt(parseVariableValue("n", other.getName()));
                } else if(result > 0) {
                    return 1;
                } else {
                    return -1;
                }
            });
            for(File resultFile : resultFiles) {
                String filename = resultFile.getName();
                String percentage = parseVariableValue("p", filename);
                String distanceMeasureName = parseVariableValue("m", filename);
                int percentageIndex = percentagesList.indexOf(percentage);
                int distanceMeasureNameIndex = distanceMeasureNamesList.indexOf(distanceMeasureName);
                if(percentageIndex >= 0 && distanceMeasureNameIndex >= 0) {
                    ClassifierResults results = loadClassifierResults(resultFile);
                    double acc = results.acc;
                    stringBuilders[distanceMeasureNameIndex][percentageIndex].append(acc).append(", ");
                }
            }
            for(int i = 0; i < stringBuilders.length; i++) {
                for(int j = 0; j < stringBuilders[i].length; j++) {
                    stringBuilders[i][j].append(";\n");
                }
            }
        }
        for(int i = 0; i < stringBuilders.length; i++) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(distanceMeasureNames[i])));
            writer.write(distanceMeasureNames[i]);
            for(int j = 0; j < stringBuilders[i].length; j++) {
                writer.write("(:,:,");
                writer.write(j);
                writer.write(") = [");
                writer.write(stringBuilders[i][j].toString());
                writer.write("];");
                writer.newLine();
            }
            writer.close();
        }



//        StringBuilder stepMatrix = new StringBuilder();
//        StringBuilder performanceMatrix = new StringBuilder();
//        File resultsDir = new File("/run/user/33190/gvfs/sftp:host=hpc.uea.ac.uk/gpfs/home/vte14wgu/experiments/sample-train/results");
//        File[] datasetDirs = resultsDir.listFiles();
//        Arrays.sort(datasetDirs, (file, other) -> file.getName().compareToIgnoreCase(other.getName()));
//        for(int datasetIndex = 0; datasetIndex < datasetDirs.length; datasetIndex++) {
//            File datasetDir = datasetDirs[datasetIndex];
//            Map<Double, Double> statOverPercentage = new TreeMap<>();
//            Map<Double, Integer> counts = new TreeMap<>();
//            for(File resultFile : datasetDir.listFiles()) {
//                String name = resultFile.getName();
//                name = name.substring(0, name.lastIndexOf(".")); // strip file extension
//                String percentageString = name.substring(name.lastIndexOf("p") + 1, name.length());
//                double percentage = Double.parseDouble(percentageString);
//                ClassifierResults results = new ClassifierResults();
//                results.loadFromFile(resultFile.getPath());
//                results.findAllStatsOnce();
//                if(Double.isNaN(results.balancedAcc)) {
//                    results.balancedAcc = 0;
//                }
//                statOverPercentage.compute(percentage, (key, previous) -> {
//                    double stat = results.getTrainTime();//balancedAcc;
//                    if(previous != null) {
//                        stat += previous;
//                    }
//                    return stat;
//                });
//                counts.compute(percentage, (key, previous) -> {
//                    if(previous == null) {
//                        return 1;
//                    } else {
//                        return previous + 1;
//                    }
//                });
////                stringBuilder.append(datasetIndex);
////                stringBuilder.append(", ");
////                stringBuilder.append(percentage);
////                stringBuilder.append(", ");
////                stringBuilder.append(results.balancedAcc);
////                stringBuilder.appendln(";");
//            }
//            if(statOverPercentage.keySet().size() == 101) {
//                for(Double key : statOverPercentage.keySet()) {
//                    statOverPercentage.compute(key, (key1, previous) -> {
//                        if(previous == null) {
//                            throw new IllegalStateException();
//                        }
//                        return previous / counts.get(key1);
//                    });
////                stringBuilder.append(datasetIndex);
////                stringBuilder.append(", ");
////                stringBuilder.append(key);
////                stringBuilder.append(", ");
////                stringBuilder.append(statOverPercentage.get(key));
////                stringBuilder.appendln(";");
//                    stepMatrix.append(key);
//                    stepMatrix.append(", ");
//                    performanceMatrix.append(statOverPercentage.get(key));
//                    performanceMatrix.append(", ");
//                }
//                stepMatrix.append(";\n");
//                performanceMatrix.append(";\n");
//            }
//        }
//        stringBuilder.appendln(stepMatrix.toString());
//        stringBuilder.appendln();
//        stringBuilder.appendln(performanceMatrix.toString());
    }
}
