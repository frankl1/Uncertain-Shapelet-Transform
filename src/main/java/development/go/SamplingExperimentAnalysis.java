package development.go;

import timeseriesweka.classifiers.ee.constituents.generators.*;
import timeseriesweka.measures.DistanceMeasure;
import utilities.ClassifierResults;
import utilities.ClassifierStats;
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
            return str;
        }
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

    public static void main(String[] args) throws Exception {
        int numFolds = 10;
        File resultsDir = new File("/run/user/33190/gvfs/sftp:host=hpc.uea.ac.uk/gpfs/home/vte14wgu/experiments/sample-train/results-tick-fix");
        File datasetDir = new File("/run/user/33190/gvfs/sftp:host=hpc.uea.ac.uk/gpfs/home/ajb/TSCProblems2018");
        List<Integer> folds = new ArrayList<>(Collections.singletonList(0));
        List<Integer> seedings = new ArrayList<>(Collections.singletonList(0));
        List<Integer> ks = new ArrayList<>(Collections.singletonList(1));
        List<ParameterisedSupplier<? extends DistanceMeasure>> parameterisedSuppliers = new ArrayList<>();
        parameterisedSuppliers.add(new DtwParameterisedSupplier());
        parameterisedSuppliers.add(new DdtwParameterisedSupplier());
        parameterisedSuppliers.add(new WdtwParameterisedSupplier());
        parameterisedSuppliers.add(new WddtwParameterisedSupplier());
        parameterisedSuppliers.add(new LcssParameterisedSupplier());
        parameterisedSuppliers.add(new MsmParameterisedSupplier());
        parameterisedSuppliers.add(new TweParameterisedSupplier());
        parameterisedSuppliers.add(new ErpParameterisedSupplier());
        parameterisedSuppliers.add(new EuclideanParameterisedSupplier());
        List<String> datasetNames = datasetNamesFromFile(new File("/run/user/33190/gvfs/sftp:host=hpc.uea.ac.uk/gpfs/home/vte14wgu/experiments/sample-train/datasetList.txt"));
        datasetNames.sort(String::compareToIgnoreCase);
        double overallProgress = 0;
        for(String datasetName : datasetNames) {
            Instances dataset = Utilities.loadDataset(new File(datasetDir, datasetName));
            for(ParameterisedSupplier<? extends DistanceMeasure> parameterisedSupplier : parameterisedSuppliers) {
                parameterisedSupplier.setParameterRanges(dataset);
            }
            double datasetProgress = 0;
            Folds datasetFolds = new Folds.Builder(dataset, numFolds)
                .setSeed(f)
                .stratify(true)
                .build();
            for(Integer fold : folds) {
                double foldProgress = 0;
                Instances trainInstances = datasetFolds.getTrain(fold);
                for(Integer seeding : seedings) {
                    double seedingProgress = 0;
                    for(Integer k : ks) {
                        double kProgress = 0;
                        for(ParameterisedSupplier<? extends DistanceMeasure> parameterisedSupplier : parameterisedSuppliers) {
                            double distanceMeasureProgress = 0;
                            if(parameterisedSupplier instanceof WdtwParameterisedSupplier) {
                                System.out.println();
                            }
                            for(int distanceMeasureParameterIndex = 0;
                                    distanceMeasureParameterIndex < parameterisedSupplier.size();
                                    distanceMeasureParameterIndex++) {
                                DistanceMeasure distanceMeasure = parameterisedSupplier.get(distanceMeasureParameterIndex);
                                double distanceMeasureParameterProgress = 0;
                                double nextPercentage = 0;
                                int numInstances = trainInstances.numInstances();
                                for(int i = 0; i < numInstances; i++) {
                                    double percentage = (double) i / numInstances;
                                    if(percentage >= nextPercentage) {
                                        nextPercentage += 0.01;
                                        File resultsFile = new File(resultsDir, datasetName
                                            + "/" + fold
                                            + "/" + seeding
                                            + "/" + k
                                            + "/" + distanceMeasure.toString()
                                            + "/" + distanceMeasure.getParameters()
                                            + "/" + percentage + ".gzip");
                                        if(resultsFile.exists()) {
                                            distanceMeasureParameterProgress++;
                                        }
                                    }
                                }
                                distanceMeasureParameterProgress /= parameterisedSupplier.size();
                                distanceMeasureProgress += distanceMeasureParameterProgress;
                            }
                            distanceMeasureProgress /= parameterisedSupplier.size();
                            kProgress += distanceMeasureProgress;
                        }
                        kProgress /= parameterisedSuppliers.size();
                        seedingProgress += kProgress;
                    }
                    seedingProgress /= ks.size();
                    foldProgress += seedingProgress;
                }
                foldProgress /= seedings.size();
                datasetProgress += foldProgress;
            }
            datasetProgress /= folds.size();
            overallProgress += datasetProgress;
        }
        overallProgress /= datasetNames.size();
        System.out.println("Overall: " + overallProgress);
    }
}
