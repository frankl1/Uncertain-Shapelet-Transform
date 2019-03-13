//package development.go;
//
//import development.go.Ee.Constituents.ParameterSpaces.*;
//import timeseriesweka.classifiers.nn.Nn;
//import timeseriesweka.measures.DistanceMeasure;
//import utilities.ClassifierTools;
//import utilities.InstanceTools;
//import utilities.Utilities;
//import weka.core.Instances;
//
//import java.io.*;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//public class SamplingExperimentProgress {
//    public static void main(String[] args) throws IOException {
//        File datasetList = new File("/scratch/datasetList.txt");
//        File globalResultsDir = new File("/run/user/33190/gvfs/sftp:host=hpc.uea.ac.uk/gpfs/home/vte14wgu/experiments/sample-train/results/snn2");
//        File datasetDir = new File("/scratch/Datasets/TSCProblems2019");
//        int[] seeds = new int[1];
//        for (int i = 0; i < seeds.length; i++) {
//            seeds[i] = i;
//        }
//        double overallPercentageProgress = 0;
//        List<ParameterSpace<? extends DistanceMeasure>> parameterSpaces = new ArrayList<>();
//        parameterSpaces.add(new DtwParameterSpace());
//        parameterSpaces.add(new DdtwParameterSpace());
//        parameterSpaces.add(new WdtwParameterSpace());
//        parameterSpaces.add(new WddtwParameterSpace());
//        parameterSpaces.add(new LcssParameterSpace());
//        parameterSpaces.add(new MsmParameterSpace());
//        parameterSpaces.add(new TweParameterSpace());
//        parameterSpaces.add(new ErpParameterSpace());
//        while (overallPercentageProgress < 100) {
//            List<String> completeDatasets = new ArrayList<>();
//            BufferedReader reader = new BufferedReader(new FileReader(datasetList));
//            String datasetStr;
//            List<String> datasetNames = new ArrayList<>();
//            while ((datasetStr = reader.readLine()) != null) {
//                datasetNames.add(datasetStr);
//            }
//            Collections.sort(datasetNames);
//            int overallProgress = 0;
//            int overallMaxProgress = 0;
//            for(String dataset : datasetNames) {
//                System.out.print(dataset);
//                String resultsDir = globalResultsDir.getPath() + "/" + dataset;
//                int datasetProgress = 0;
//                int datasetMaxProgress = 0;
//                for (int seed : seeds) {
//                    Instances trainInstances = ClassifierTools.loadData(new File(datasetDir, dataset + "/" + dataset + "_TRAIN.arff"));
//                    Instances testInstances = ClassifierTools.loadData(new File(datasetDir, dataset + "/" + dataset + "_TEST.arff"));
//                    Instances[] split = InstanceTools.resampleTrainAndTestInstances(trainInstances, testInstances, seed);
//                    trainInstances = split[0];
//                    testInstances = split[1];
//                    int seedProgress = 0;
//                    int seedMaxProgress = 0;
//                    for (ParameterSpace<? extends DistanceMeasure> parameterSpace : parameterSpaces) {
//                        parameterSpace.useInstances(trainInstances);
//                        for (int k = 0; k < parameterSpace.size(); k++) {
//                            parameterSpace.setCombination(k);
//                            DistanceMeasure distanceMeasure = parameterSpace.build();
//                            Nn nearestNeighbour = new Nn();
//                            nearestNeighbour.setDistanceMeasure(distanceMeasure);
//                            String path = seed
//                                + "/" + nearestNeighbour.getDistanceMeasure()
//                                + "/" + nearestNeighbour.getDistanceMeasure().getParameters() + ".gzip";
//                            File file = new File(resultsDir, path);
//                            if(file.exists()) {
//                                seedProgress++;
//                            } else {
////                                System.out.println(distanceMeasure.toString() + " " + distanceMeasure.getParameters());
//                            }
//                            seedMaxProgress++;
//                        }
//                    }
//                    System.out.print(" ");
//                    System.out.print(String.format("%3.2f", (double) seedProgress / seedMaxProgress * 100));
//                    datasetProgress += seedProgress;
//                    datasetMaxProgress += seedMaxProgress;
//                }
//                if(seeds.length > 1) {
//                    System.out.print(" ");
//                    System.out.print(String.format("%3.2f", (double) datasetProgress / datasetMaxProgress * 100));
//                }
//                if(datasetProgress == datasetMaxProgress) {
//                    completeDatasets.add(dataset);
//                }
//                overallMaxProgress += datasetMaxProgress;
//                overallProgress += datasetProgress;
//                System.out.println();
//            }
//            reader.close();
//            overallPercentageProgress = (double) overallProgress / overallMaxProgress * 100;
//            System.out.println("-----");
//            System.out.println(String.format("Overall: %3.2f", overallPercentageProgress));
//            System.out.println("-----");
//            System.out.println("Complete:");
//            for(String str : completeDatasets) {
//                System.out.println("\"" + str + "\",");
//            }
//            System.out.println("-----");
//        }
//    }
//}
