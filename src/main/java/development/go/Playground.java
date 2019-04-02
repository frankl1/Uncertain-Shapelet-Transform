package development.go;

import utilities.Utilities;
import weka.core.Instances;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static utilities.Utilities.readDatasetNameList;
import static utilities.Utilities.writeDatasetNameList;

public class Playground {
    public static void main(String[] args) throws IOException {
        String datasetNameListPath = "/scratch/datasetList.txt";
        String out = "/scratch/datasetsByInstances.txt";
        String datasetsDir = "/scratch/Datasets/TSCProblems2015/";
        List<String> datasetNames = readDatasetNameList(datasetNameListPath);
        Comparator<Instances> comparator = new Comparator<Instances>() {
            @Override
            public int compare(final Instances instancesA, final Instances instancesB) {
                return instancesA.size() - instancesB.size();
            }
        };
        Utilities.sortDatasetNames(datasetsDir, datasetNames, comparator);
        writeDatasetNameList(out, datasetNames);
    }
}