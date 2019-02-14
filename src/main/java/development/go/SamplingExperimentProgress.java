package development.go;

import timeseriesweka.classifiers.NearestNeighbour;
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
        File datasetList = new File("/run/user/33190/gvfs/sftp:host=hpc.uea.ac.uk/gpfs/home/vte14wgu/experiments/sample-train/datasetList.txt");
        File globalResultsDir = new File("/run/user/33190/gvfs/sftp:host=hpc.uea.ac.uk/gpfs/home/vte14wgu/experiments/sample-train/results/nns");
        File datasetDir = new File("/scratch/TSCProblems2018");
        BufferedReader reader = new BufferedReader(new FileReader(datasetList));
        String dataset;
        int[] seeds = new int[1];
        for (int i = 0; i < seeds.length; i++) {
            seeds[i] = i;
        }
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
        int overallProgress = 0;
        int overallMaxProgress = 0;
        while ((dataset = reader.readLine()) != null) {
            System.out.print(dataset);
            String resultsDir = globalResultsDir.getPath() + "/" + dataset;
            int datasetProgress = 0;
            int datasetMaxProgress = 0;
            for (int seed : seeds) {
                int seedProgress = 0;
                int seedMaxProgress = 0;
                for (ParameterisedSupplier<? extends DistanceMeasure> parameterisedSupplier : parameterisedSuppliers) {
                    Instances instances = Utilities.loadDataset(new File(datasetDir, dataset));
                    Instances[] splitInstances = InstanceTools.resampleInstances(instances, seed, 0.5);
                    Instances trainInstances = splitInstances[0];
                    parameterisedSupplier.setParameterRanges(trainInstances);
                    int numTrainInstances = trainInstances.numInstances();
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
                                File file = new File(resultsDir, resultsFilePrefix + "/" + percentage + ".gzip");
                                if (file.exists()) {
                                    seedProgress++;
                                }
                                seedMaxProgress++;
                            }
                        }
                    }
                }
                System.out.print(" ");
                System.out.print(String.format("%3.2f", (double) seedProgress / seedMaxProgress * 100));
                datasetProgress += seedProgress;
                datasetMaxProgress += seedMaxProgress;
            }
            System.out.print(" ");
            System.out.println(String.format("%3.2f", (double) datasetProgress / datasetMaxProgress * 100));
            overallMaxProgress += datasetMaxProgress;
            overallProgress += datasetProgress;
        }
        reader.close();
        System.out.println(String.format("%3.2f", (double) (double) overallProgress / overallMaxProgress * 100));
    }
}
