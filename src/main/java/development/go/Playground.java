package development.go;

import utilities.Utilities;
import weka.core.Instances;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Playground {
    public static void main(String[] args) throws IOException {
        String datasetNameListPath = "/scratch/datasetList.txt";
        String datasetsDir = "/scratch/Datasets/TSCProblems2015/";
        List<String> datasetNames = readDatasetNameList(datasetNameListPath);
        Comparator<Instances> comparator = new Comparator<Instances>() {
            @Override
            public int compare(final Instances instancesA, final Instances instancesB) {
                return instancesA.size() - instancesB.size();
            }
        };
        sortDatasetNames(datasetsDir, datasetNames, comparator);
        writeDatasetNameList(datasetNameListPath, datasetNames);
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
}