package development.go;

import utilities.ClassifierResults;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.function.BiFunction;

public class ExperimentAnalysis {

    public static void main(String[] args) throws FileNotFoundException {
        StringBuilder stepMatrix = new StringBuilder();
        StringBuilder performanceMatrix = new StringBuilder();
        File resultsDir = new File("/run/user/33190/gvfs/sftp:host=hpc.uea.ac.uk/gpfs/home/vte14wgu/experiments/sample-train/results");
        File[] datasetDirs = resultsDir.listFiles();
        Arrays.sort(datasetDirs, (file, other) -> file.getName().compareToIgnoreCase(other.getName()));
        for(int datasetIndex = 0; datasetIndex < datasetDirs.length; datasetIndex++) {
            File datasetDir = datasetDirs[datasetIndex];
            Map<Double, Double> statOverPercentage = new TreeMap<>();
            Map<Double, Integer> counts = new TreeMap<>();
            for(File resultFile : datasetDir.listFiles()) {
                String name = resultFile.getName();
                name = name.substring(0, name.lastIndexOf(".")); // strip file extension
                String percentageString = name.substring(name.lastIndexOf("p") + 1, name.length());
                double percentage = Double.parseDouble(percentageString);
                ClassifierResults results = new ClassifierResults();
                results.loadFromFile(resultFile.getPath());
                results.findAllStatsOnce();
                if(Double.isNaN(results.balancedAcc)) {
                    results.balancedAcc = 0;
                }
                statOverPercentage.compute(percentage, (key, previous) -> {
                    double stat = results.getTrainTime();//balancedAcc;
                    if(previous != null) {
                        stat += previous;
                    }
                    return stat;
                });
                counts.compute(percentage, (key, previous) -> {
                    if(previous == null) {
                        return 1;
                    } else {
                        return previous + 1;
                    }
                });
//                System.out.print(datasetIndex);
//                System.out.print(", ");
//                System.out.print(percentage);
//                System.out.print(", ");
//                System.out.print(results.balancedAcc);
//                System.out.println(";");
            }
            if(statOverPercentage.keySet().size() == 101) {
                for(Double key : statOverPercentage.keySet()) {
                    statOverPercentage.compute(key, (key1, previous) -> {
                        if(previous == null) {
                            throw new IllegalStateException();
                        }
                        return previous / counts.get(key1);
                    });
//                System.out.print(datasetIndex);
//                System.out.print(", ");
//                System.out.print(key);
//                System.out.print(", ");
//                System.out.print(statOverPercentage.get(key));
//                System.out.println(";");
                    stepMatrix.append(key);
                    stepMatrix.append(", ");
                    performanceMatrix.append(statOverPercentage.get(key));
                    performanceMatrix.append(", ");
                }
                stepMatrix.append(";\n");
                performanceMatrix.append(";\n");
            }
        }
        System.out.println(stepMatrix.toString());
        System.out.println();
        System.out.println(performanceMatrix.toString());
    }
}
