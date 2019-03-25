package development.go;

import evaluation.storage.ClassifierResults;
import scala.tools.nsc.interpreter.Results;
import timeseriesweka.classifiers.nn.Nn;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;
import utilities.*;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SamplingExperimentAnalysis {

    private static String parseFileName(String str) {
        int index = str.lastIndexOf(".");
        if(index >= 0) {
            return str.substring(0, index);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static String parseVariable(String str, String variableName) {
        if(variableName.equalsIgnoreCase("q")) {
            int index = str.indexOf("{");
            if(index >= 0) {
                str = str.substring(index + 1);
                index = str.lastIndexOf("}");
                str = str.substring(0, index);
                return str;
            }
        } else {
            str = str.substring(0, str.indexOf("{")) + str.substring(str.lastIndexOf("}") + 1, str.length());
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
        }
        throw new IllegalArgumentException();
    }

//    private static ClassifierStats loadStats(File file) throws IOException, ClassNotFoundException {
//        ObjectInputStream in = new ObjectInputStream(
//            new GZIPInputStream(
//                new BufferedInputStream(
//                    new FileInputStream(file)
//                )
//            )
//        );
//        Object object = in.readObject();
//        in.close();
//        return (ClassifierStats) object;
//    }


    public static void main(String[] args) throws Exception {

//        Nn nn = new Nn();
//        int seed =0;
//        Dtw dtw = new Dtw();
//        dtw.setWarpingWindow(0);
//        nn.setDistanceMeasure(dtw);
//            System.out.println(nn.toString() + " " + nn.getDistanceMeasure().getParameters());
//            nn.setSeed(seed);
//            nn.setCvTrain(true);
//            nn.setUseEarlyAbandon(false);
//            nn.setKPercentage(0);
//            nn.setUseRandomTieBreak(false);
//            File datasetFile = new File("/scratch/Datasets/TSCProblems2015/GunPoint");
//        String datasetName = datasetFile.getName();
//        Instances trainInstances = ClassifierTools.loadData(datasetFile + "/" + datasetName + "_TRAIN.arff");
//        Instances testInstances = ClassifierTools.loadData(datasetFile + "/" + datasetName + "_TEST.arff");
//        Instances[] splitInstances = InstanceTools.resampleTrainAndTestInstances(trainInstances, testInstances, seed);
//        trainInstances = splitInstances[0];
//        testInstances = splitInstances[1];
//        nn.buildClassifier(trainInstances);
//        ClassifierResults trainResults = nn.getTrainResults();
//        ClassifierResults testResults = nn.getTestResults(testInstances);
//        System.out.println(trainResults.writeFullResultsToString());
//        System.out.println();
//        System.out.println(testResults.writeFullResultsToString());

        String resultsPath = "/run/user/33190/gvfs/sftp:host=hpc.uea.ac.uk/gpfs/home/vte14wgu/experiments/sample-train/results/nn2/Predictions/GunPoint/dtw/-w 0.0/fold0.csv.gzip";
        ObjectInputStream reader = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(resultsPath))));
        for(int i = 0; i <= 100; i += 10) {
            double percentage = reader.readDouble();
            System.out.println(percentage);
            String trainResultsString = (String) reader.readObject();
            ClassifierResults trainResults = ClassifierResults.parse(trainResultsString);
            System.out.println(trainResults.getAcc());
//            System.out.println();
            String testResultsString = (String) reader.readObject();
            ClassifierResults testResults = ClassifierResults.parse(testResultsString);
            System.out.println(testResults.getAcc());
//            System.out.println();
            System.out.println();
        }

//        File datasetFile = new File("/scratch/Datasets/TSCProblems2015/GunPoint");
//        String datasetName = datasetFile.getName();
//        Instances trainInstances = ClassifierTools.loadData(datasetFile + "/" + datasetName + "_TRAIN.arff");
//        Instances testInstances = ClassifierTools.loadData(datasetFile + "/" + datasetName + "_TEST.arff");
//        int seed = 0;
//        Instances[] splitInstances = InstanceTools.resampleTrainAndTestInstances(trainInstances, testInstances, seed);
//        trainInstances = splitInstances[0];
//        testInstances = splitInstances[1];
//        for(int i = 0; i < trainInstances.size(); i++) {
//            System.out.println(reader.readLine());
//        }


//        try {
//            final File resultsDir = new File("/run/user/33190/gvfs/sftp:host=hpc.uea.ac.uk/gpfs/home/vte14wgu/experiments/sample-train/results/sample");
//            final File datasetDir = new File("/scratch/Datasets/TSCProblems2015");
//            final File dataScripts = new File("/scratch/dataScripts");
//            final int numFolds = 1;
//            Utilities.mkdir(dataScripts);
//            List<String> datasetNames = Arrays.asList(
//                "ArrowHead",
//                "Beef",
//                "BeetleFly",
//                "BirdChicken",
//                "CBF",
//                "Car",
//                "Coffee",
//                "DiatomSizeReduction",
//                "ECG200",
//                "ECGFiveDays",
//                "FaceFour",
//                "GunPoint",
//                "Herring",
//                "ItalyPowerDemand",
//                "Lightning2",
//                "Lightning7",
//                "Meat",
//                "MoteStrain",
//                "OliveOil",
//                "Plane",
//                "ShapeletSim",
//                "SonyAIBORobotSurface1",
//                "SonyAIBORobotSurface2",
//                "Symbols",
//                "SyntheticControl",
//                "ToeSegmentation1",
//                "ToeSegmentation2",
//                "Trace",
//                "TwoLeadECG",
//                "Wine"
//            );
//            Collections.sort(datasetNames);
//            List<ParameterSpace<? extends DistanceMeasure>> parameterSpaces = new ArrayList<>();
//            parameterSpaces.add(new DtwParameterSpace());
//            parameterSpaces.add(new DdtwParameterSpace());
//            parameterSpaces.add(new WdtwParameterSpace());
//            parameterSpaces.add(new WddtwParameterSpace());
//            parameterSpaces.add(new LcssParameterSpace());
//            parameterSpaces.add(new MsmParameterSpace());
//            parameterSpaces.add(new TweParameterSpace());
//            parameterSpaces.add(new ErpParameterSpace());
//
//            double[/*trainOrTest*/][/*dataset*/][/*dm*/][/*param*/][/*inst*/] results = new double[2][datasetNames.size()][parameterSpaces.size()][][];
//
//            for (int datasetIndex = 0; datasetIndex < datasetNames.size(); datasetIndex++) {
//                String dataset = datasetNames.get(datasetIndex);
//                Instances trainInstances = ClassifierTools.loadData(new File(datasetDir, dataset + "/" + dataset + "_TRAIN.arff"));
//                Instances testInstances = ClassifierTools.loadData(new File(datasetDir, dataset + "/" + dataset + "_TEST.arff"));
//                for (int distanceMeasureIndex = 0; distanceMeasureIndex < parameterSpaces.size(); distanceMeasureIndex++) {
//                    for (int seed = 0; seed < numFolds; seed++) {
//                        Instances[] split = InstanceTools.resampleTrainAndTestInstances(trainInstances, testInstances, seed);
//                        trainInstances = split[0];
//                        testInstances = split[1];
//                        ParameterSpace<? extends DistanceMeasure> parameterSpace = parameterSpaces.get(distanceMeasureIndex);
//                        parameterSpace.useInstances(trainInstances);
//                        results[0][datasetIndex][distanceMeasureIndex] = new double[parameterSpace.size()][trainInstances.numInstances() + 1];
//                        results[1][datasetIndex][distanceMeasureIndex] = new double[parameterSpace.size()][trainInstances.numInstances() + 1];
//                        for (int combination = 0; combination < parameterSpace.size(); combination++) {
//                            parameterSpace.setCombination(combination);
//                            DistanceMeasure distanceMeasure = parameterSpace.build();
//                            File file = new File(resultsDir, dataset + "/" + seed + "/" + distanceMeasure.toString() + "/" + distanceMeasure.getParameters() + ".gzip");
//                            try {
//                                ObjectInputStream reader = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(file))));
//                                long benchmark = reader.readLong();
//                                for (int instanceIndex = 0; instanceIndex <= trainInstances.numInstances(); instanceIndex++) {
//                                    for (int trainOrTest = 0; trainOrTest < 2; trainOrTest++) {
//                                        ClassifierResults classifierResults = readResults(reader);
//                                        results[trainOrTest][datasetIndex][distanceMeasureIndex][combination][instanceIndex] = classifierResults.balancedAcc;
//                                    }
//                                }
//                                reader.close();
//                            } catch (Exception e) {
//                                System.out.println(dataset + " " + distanceMeasure.toString() + " " + distanceMeasure.getParameters());
//                                file.delete();
//                            }
//                        }
//                    }
//                }
//            }
//
//            BufferedWriter writer = new BufferedWriter(new FileWriter("/scratch/playground/balAcc.m"));
//            String varName = "d";
//                for (int trainOrTest = 0; trainOrTest < results.length; trainOrTest++) {
//                    double[][][][] datasets = results[trainOrTest];
//                    for (int datasetIndex = 0; datasetIndex < datasets.length; datasetIndex++) {
//                        double[][][] distanceMeasures = datasets[datasetIndex];
//                        for (int distanceMeasureIndex = 0; distanceMeasureIndex < distanceMeasures.length; distanceMeasureIndex++) {
//                            writer.write(varName);
//                            writer.write("{");
//                            writer.write(String.valueOf(trainOrTest + 1));
//                            writer.write(",");
//                            writer.write(String.valueOf(datasetIndex + 1));
//                            writer.write(",");
//                            writer.write(String.valueOf(distanceMeasureIndex + 1));
//                            writer.write("} = [");
//                            double[][] param = distanceMeasures[distanceMeasureIndex];
//                            for (int i = 0; i < param.length; i++) {
//                                for (int j = 0; j < param[i].length; j++) {
//                                    writer.write(String.valueOf(param[i][j]));
//                                    writer.write(",");
//                                }
//                                writer.write(";");
//                            }
//                            writer.write("];");
//                            writer.write(System.lineSeparator());
//                        }
//                    }
//                }
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


//        for(int distanceMeasureIndex = 0; distanceMeasureIndex < parameterSpaces.size(); distanceMeasureIndex++) {
//            ParameterSpace<? extends DistanceMeasure> parameterSpace = parameterSpaces.get(distanceMeasureIndex);
//            for(int datasetIndex = 0; datasetIndex < datasetNames.size(); datasetIndex++) {
//                String dataset = datasetNames.get(datasetIndex);
//                Instances trainInstances = ClassifierTools.loadData(new File(datasetDir, dataset + "/" + dataset + "_TRAIN.arff"));
//                Instances testInstances = ClassifierTools.loadData(new File(datasetDir, dataset + "/" + dataset + "_TEST.arff"));
//                BufferedReader[] readers = new BufferedReader[numFolds];
//                for(int i = 0; i < readers.length; i++) {
//                    Instances[] split = InstanceTools.resampleTrainAndTestInstances(trainInstances, testInstances, i);
//                    trainInstances = split[0];
//                    testInstances = split[1];
//                    parameterSpace.useInstances(trainInstances);
//                    parameterSpace.setCombination();
//                    DistanceMeasure distanceMeasure = parameterSpace.build();
//                    readers[i] = new BufferedReader(new FileReader(new File(resultsDir, dataset + "/" + i + "/" + parameterSpace.build().toString() + )))
//                }
//                for(int instanceIndex = 0; instanceIndex <= trainInstances.numInstances(); instanceIndex++) {
//                    double sum = 0;
//                    for(int seed = 0; seed < numFolds; seed++) {
//
//                    }
//                }
//            }
//        }
    }

//    private static ClassifierResults readResults(ObjectInputStream objectInputStream) throws IOException {
//        ClassifierResults results = new ClassifierResults();
//        results.acc = objectInputStream.readDouble();
//        results.balancedAcc = objectInputStream.readDouble();
//        results.nll = objectInputStream.readDouble();
//        results.mcc = objectInputStream.readDouble();
//        results.meanAUROC = objectInputStream.readDouble();
//        results.f1 = objectInputStream.readDouble();
//        results.precision = objectInputStream.readDouble();
//        results.recall = objectInputStream.readDouble();
//        results.sensitivity = objectInputStream.readDouble();
//        results.specificity = objectInputStream.readDouble();
//        results.setTestTime(objectInputStream.readLong());
//        results.setTrainTime(objectInputStream.readLong());
//        results.memory = objectInputStream.readLong();
//        return results;
//    }
}
