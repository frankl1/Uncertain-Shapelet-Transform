package development.go;

import development.go.Ee.Constituents.ParameterSpaces.*;
import scala.tools.nsc.interpreter.Results;
import timeseriesweka.measures.DistanceMeasure;
import utilities.*;
import weka.core.Instance;
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

    private static ClassifierStats loadStats(File file) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(
            new GZIPInputStream(
                new BufferedInputStream(
                    new FileInputStream(file)
                )
            )
        );
        Object object = in.readObject();
        in.close();
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
        final File resultsDir = new File("/run/user/33190/gvfs/sftp:host=hpc.uea.ac.uk/gpfs/home/vte14wgu/experiments/sample-train/results/snn2");
        final File datasetsDir = new File("/scratch/Datasets/TSCProblems2019");
        final File dataScripts = new File("/scratch/dataScripts");
        final int numFolds = 1;
        Utilities.mkdir(dataScripts);
        List<String> datasetNames = Arrays.asList(
//            "ArrowHead",
//            "Beef",
//            "BeetleFly",
//            "BirdChicken",
//            "CBF",
//            "Car",
//            "Coffee",
//            "DiatomSizeReduction",
//            "ECG200",
//            "ECGFiveDays",
//            "FaceFour",
//            "GunPoint"
//            "Herring",
//            "ItalyPowerDemand",
            "Lightning2",
            "Lightning7"
//            "Meat",
//            "MoteStrain",
//            "OliveOil",
//            "Plane",
//            "ShapeletSim",
//            "SonyAIBORobotSurface1",
//            "SonyAIBORobotSurface2",
//            "Symbols",
//            "ToeSegmentation1",
//            "ToeSegmentation2",
//            "Trace",
//            "TwoLeadECG",
//            "Wine"
        );
        Collections.sort(datasetNames);
        List<ParameterSpace<? extends DistanceMeasure>> parameterSpaces = new ArrayList<>();
        parameterSpaces.add(new DtwParameterSpace());
        parameterSpaces.add(new DdtwParameterSpace());
        parameterSpaces.add(new WdtwParameterSpace());
        parameterSpaces.add(new WddtwParameterSpace());
        parameterSpaces.add(new LcssParameterSpace());
        parameterSpaces.add(new MsmParameterSpace());
        parameterSpaces.add(new TweParameterSpace());
        parameterSpaces.add(new ErpParameterSpace());
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dataScripts, "data.m")));
//        List<BufferedWriter> writers = new ArrayList<>();
//        for(ParameterSpace<?> parameterSpace : parameterSpaces) {
//            writers.add(new BufferedWriter(new FileWriter(new File(dataScripts, parameterSpace.build().toString() + ".m"))));
//        }
        String variableName = "data";
        for(int datasetIndex = 0; datasetIndex < datasetNames.size(); datasetIndex++) {
            String datasetName = datasetNames.get(datasetIndex);
            for(int parameterSpaceIndex = 0; parameterSpaceIndex < parameterSpaces.size(); parameterSpaceIndex++) {
//                BufferedWriter writer = writers.get(k);
                for(int seed = 0; seed < numFolds; seed++) {
                    Instances trainInstances = ClassifierTools.loadData(new File(datasetsDir, datasetName + "/" + datasetName + "_TRAIN.arff"));
                    Instances testInstances = ClassifierTools.loadData(new File(datasetsDir, datasetName + "/" + datasetName + "_TEST.arff"));
                    Instances[] split = InstanceTools.resampleTrainAndTestInstances(trainInstances, testInstances, seed);
                    trainInstances = split[0];
                    testInstances = split[1];
                    ParameterSpace<? extends DistanceMeasure> parameterSpace = parameterSpaces.get(parameterSpaceIndex);
                    parameterSpace.useInstances(trainInstances);
                    parameterSpace.setCombination(0);
//                    String variableName = datasetName + "_" + parameterSpace.build().toString();
                    for(int combination = 0; combination < parameterSpace.size(); combination++) {
                        parameterSpace.setCombination(combination);
                        DistanceMeasure distanceMeasure = parameterSpace.build();
                        File resultFile = new File(resultsDir, datasetName + "/" + seed + "/" + distanceMeasure.toString() + "/" + distanceMeasure.getParameters() + ".gzip");
                        ObjectInputStream objectInputStream = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(resultFile))));
                        long benchmark = objectInputStream.readLong();
                        for(int instIndex = 0; instIndex <= trainInstances.numInstances(); instIndex++) {
                            for(int trainOrTest = 0; trainOrTest < 2; trainOrTest++) {
                                Map<String, String> values = new HashMap<>();
                                values.put("acc", String.valueOf(objectInputStream.readDouble())); // acc
                                values.put("balacc", String.valueOf(objectInputStream.readDouble())); // balacc
                                values.put("nll", String.valueOf(objectInputStream.readDouble())); // nll
                                values.put("mcc", String.valueOf(objectInputStream.readDouble())); // mcc
                                values.put("auroc", String.valueOf(objectInputStream.readDouble())); // meanAuroc
                                values.put("f1", String.valueOf(objectInputStream.readDouble())); // f1
                                values.put("prec", String.valueOf(objectInputStream.readDouble())); // precision
                                values.put("rec", String.valueOf(objectInputStream.readDouble())); // recall
                                values.put("sens", String.valueOf(objectInputStream.readDouble())); // sens
                                values.put("spec", String.valueOf(objectInputStream.readDouble())); // spec
                                values.put("testTime", String.valueOf(objectInputStream.readLong())); // test time
                                values.put("trainTime", String.valueOf(objectInputStream.readLong())); // train time
                                values.put("mem", String.valueOf(objectInputStream.readLong())); // mem
//                                for(int j = 0; j < 14; j++) {
//                                for(String key : values.keySet()) {
                                String key = "acc";
                                writer.write(variableName);
//                                writer.write("_");
//                                writer.write(key);
//                                if(j % 2 == 0) {
//                                    writer.write("_train");
//                                } else {
//                                    writer.write("_test");
//                                }
                                writer.write("{");
                                writer.write(String.valueOf(parameterSpaceIndex + 1));
                                writer.write(",");
                                writer.write(String.valueOf(datasetIndex + 1));
                                writer.write(",");
                                writer.write(String.valueOf(instIndex + 1));
                                writer.write("}");
                                writer.write("(");
                                writer.write(String.valueOf(trainOrTest + 1));
                                writer.write(",");
                                writer.write(String.valueOf(combination + 1));
                                writer.write(") = ");
                                writer.write(values.get(key));
//                                    writer.write(values.get(j));
                                writer.write(";");
                                writer.newLine();
                            }
//                                }
                        }
                    }
                }
            }
        }
//        for(Writer writer : writers) {
//            writer.close();
//        }
        writer.close();
    }

    private static ClassifierResults readResults(ObjectInputStream objectInputStream) throws IOException {
        ClassifierResults results = new ClassifierResults();
        results.acc = objectInputStream.readDouble();
        results.balancedAcc = objectInputStream.readDouble();
        results.nll = objectInputStream.readDouble();
        results.mcc = objectInputStream.readDouble();
        results.meanAUROC = objectInputStream.readDouble();
        results.f1 = objectInputStream.readDouble();
        results.precision = objectInputStream.readDouble();
        results.recall = objectInputStream.readDouble();
        results.sensitivity = objectInputStream.readDouble();
        results.specificity = objectInputStream.readDouble();
        results.setTestTime(objectInputStream.readLong());
        results.setTrainTime(objectInputStream.readLong());
        results.memory = objectInputStream.readLong();
        return results;
    }
}
