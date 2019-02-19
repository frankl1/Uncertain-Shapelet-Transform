package development.go;

import timeseriesweka.classifiers.nearest_neighbour.NearestNeighbour;
import timeseriesweka.classifiers.ee.constituents.generators.*;
import timeseriesweka.measures.DistanceMeasure;
import utilities.InstanceTools;
import utilities.Utilities;
import weka.core.Instances;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SamplingExperimentProgress {
    public static void main(String[] args) throws IOException {
        File datasetList = new File("datasetList.txt");
        File globalResultsDir = new File("results/nnscv");
        File datasetDir = new File("/gpfs/home/ajb/TSCProblems2019");
        int[] seeds = new int[1];
        for (int i = 0; i < seeds.length; i++) {
            seeds[i] = i;
        }
        double overallPercentageProgress = 0;
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
        while (overallPercentageProgress < 100) {
            BufferedReader reader = new BufferedReader(new FileReader(datasetList));
            String dataset;
            int overallProgress = 0;
            int overallMaxProgress = 0;
            while ((dataset = reader.readLine()) != null) {
                System.out.print(dataset);
                String resultsDir = globalResultsDir.getPath() + "/" + dataset;
                Instances instances = Utilities.loadDataset(new File(datasetDir, dataset));
                int datasetProgress = 0;
                int datasetMaxProgress = 0;
                for (int seed : seeds) {
                    Instances[] splitInstances = InstanceTools.resampleInstances(instances, seed, 0.5);
                    Instances trainInstances = splitInstances[0];
                    int numTrainInstances = trainInstances.numInstances();
                    int seedProgress = 0;
                    int seedMaxProgress = 0;
                    for (ParameterisedSupplier<? extends DistanceMeasure> parameterisedSupplier : parameterisedSuppliers) {
                        parameterisedSupplier.setParameterRanges(trainInstances);
                        for (int k = 0; k < parameterisedSupplier.size(); k++) {
                            DistanceMeasure distanceMeasure = parameterisedSupplier.get(k);
                            NearestNeighbour nearestNeighbour = new NearestNeighbour();
                            nearestNeighbour.setDistanceMeasure(distanceMeasure);
                            String resultsFilePrefix = seed
                                + "/" + nearestNeighbour.getDistanceMeasure()
                                + "/" + nearestNeighbour.getDistanceMeasure().getParameters();
                            double nextPercentage = 0;
                            for (int i = 0, j = 0; i <= numTrainInstances; i++) {
                                double percentage = (double) i / numTrainInstances;
                                if (percentage >= nextPercentage) {
                                    j++;
                                    nextPercentage = (double) j / 100;
                                    File file = new File(resultsDir, resultsFilePrefix + "/" + percentage);
                                    File train = new File(file, "train.gzip");
                                    File test = new File(file, "test.gzip");
                                    if (train.exists()) {
                                        seedProgress++;
                                    }
                                    if (test.exists()) {
                                        seedProgress++;
                                    }
                                    seedMaxProgress += 2;
                                }
                            }
                        }
                    }
                    System.out.print(" ");
                    System.out.print(String.format("%3.2f", (double) seedProgress / seedMaxProgress * 100));
                    datasetProgress += seedProgress;
                    datasetMaxProgress += seedMaxProgress;
                }
                if(seeds.length > 1) {
                    System.out.print(" ");
                    System.out.print(String.format("%3.2f", (double) datasetProgress / datasetMaxProgress * 100));
                }
                overallMaxProgress += datasetMaxProgress;
                overallProgress += datasetProgress;
                System.out.println();
            }
            reader.close();
            overallPercentageProgress = (double) overallProgress / overallMaxProgress * 100;
            System.out.println(String.format("%3.2f", overallPercentageProgress));
        }
    }
}
