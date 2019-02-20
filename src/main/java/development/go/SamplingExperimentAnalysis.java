package development.go;

import scala.tools.nsc.interpreter.Results;
import timeseriesweka.classifiers.ee.constituents.generators.*;
import timeseriesweka.measures.DistanceMeasure;
import utilities.ClassifierResults;
import utilities.ClassifierStats;
import utilities.InstanceTools;
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
        File resultsDir = new File("/run/user/33190/gvfs/sftp:host=hpc.uea.ac.uk/gpfs/home/vte14wgu/experiments/sample-train/results/nn3");
        File datasetsDir = new File("/scratch/TSCProblems2018");
        List<String> datasetNames = Arrays.asList(
            "Beef"
            );
        List<ParameterisedSupplier<? extends DistanceMeasure>> parameterisedSuppliers = new ArrayList<>();
        parameterisedSuppliers.add(new DtwParameterisedSupplier());
//        parameterisedSuppliers.add(new DdtwParameterisedSupplier());
//        parameterisedSuppliers.add(new WdtwParameterisedSupplier());
//        parameterisedSuppliers.add(new WddtwParameterisedSupplier());
//        parameterisedSuppliers.add(new LcssParameterisedSupplier());
//        parameterisedSuppliers.add(new MsmParameterisedSupplier());
//        parameterisedSuppliers.add(new TweParameterisedSupplier());
//        parameterisedSuppliers.add(new ErpParameterisedSupplier());
        String variableName = "data";
        int numSeeds = 1;
        BufferedWriter writer = new BufferedWriter(new FileWriter("matlabCommands.m"));
            //datasetNamesFromFile(new File("/run/user/33190/gvfs/sftp:host=hpc.uea.ac.uk/gpfs/home/vte14wgu/experiments/sample-train/datasetList.txt"));
        datasetNames.sort(String::compareToIgnoreCase);
        //  dataset     seed         distmeas    param        inst         statIndex   val
//        Map<String, Map<Integer, Map<String, Map<Integer, Map<Integer, Map<Integer, String>>>>>> results = new TreeMap<>();
        for(int datasetIndex = 0; datasetIndex < datasetNames.size(); datasetIndex++) {
            String datasetName = datasetNames.get(datasetIndex);
            File datasetFile = new File(datasetsDir, datasetName);
            Instances dataset = Utilities.loadDataset(datasetFile);
//            Map<Integer, Map<String, Map<Integer, Map<Integer, Map<Integer, String>>>>> datasetResults = results.computeIfAbsent(datasetName, key -> new TreeMap<>());
            for(int seed = 0; seed < numSeeds; seed++) {
                Instances[] splitInstances = InstanceTools.resampleInstances(dataset, seed, 0.5);
                Instances trainInstances = splitInstances[0];
//                Map<String, Map<Integer, Map<Integer, Map<Integer, String>>>> seedResults = datasetResults.computeIfAbsent(seed, key -> new TreeMap<>());
                for(int parameterisedSupplierIndex = 0; parameterisedSupplierIndex < parameterisedSuppliers.size(); parameterisedSupplierIndex++) {
                    ParameterisedSupplier<? extends DistanceMeasure> parameterisedSupplier = parameterisedSuppliers.get(parameterisedSupplierIndex);
                    parameterisedSupplier.setParameterRanges(trainInstances);
//                    Map<Integer, Map<Integer, Map<Integer, String>>> distanceMeasureResults = seedResults.computeIfAbsent(parameterisedSupplier.get(0).toString(), key -> new TreeMap<>());
                    for(int param = 0; param < parameterisedSupplier.size(); param++) {
                        DistanceMeasure distanceMeasure = parameterisedSupplier.get(param);
//                        Map<Integer, Map<Integer, String>> paramResults = distanceMeasureResults.computeIfAbsent(param, key -> new TreeMap<>());
                        File file = new File(resultsDir, datasetName
                            + "/" + seed
                            + "/" + distanceMeasure.toString()
                            + "/" + distanceMeasure.getParameters()
                            + ".gzip"
                        );
                        ObjectInputStream objectInputStream = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(file))));
                        long benchmark = objectInputStream.readLong();
                        for(int instIndex = 0; instIndex <= trainInstances.numInstances(); instIndex++) {
//                            Map<Integer, String> instIndexResults = paramResults.computeIfAbsent(instIndex, key -> new TreeMap<>());
//                            int j = 0;
                            for(int trainOrTest = 0; trainOrTest <= 1; trainOrTest++) {
                                List<String> values = new ArrayList<>();
                                values.add(String.valueOf(benchmark)); // bench
                                values.add(String.valueOf(objectInputStream.readDouble())); // acc
                                values.add(String.valueOf(objectInputStream.readDouble())); // balacc
                                values.add(String.valueOf(objectInputStream.readDouble())); // nll
                                values.add(String.valueOf(objectInputStream.readDouble())); // mcc
                                values.add(String.valueOf(objectInputStream.readDouble())); // meanAuroc
                                values.add(String.valueOf(objectInputStream.readDouble())); // f1
                                values.add(String.valueOf(objectInputStream.readDouble())); // precision
                                values.add(String.valueOf(objectInputStream.readDouble())); // recall
                                values.add(String.valueOf(objectInputStream.readDouble())); // sens
                                values.add(String.valueOf(objectInputStream.readDouble())); // spec
                                values.add(String.valueOf(objectInputStream.readLong())); // test time
                                values.add(String.valueOf(objectInputStream.readLong())); // train time
                                values.add(String.valueOf(objectInputStream.readLong())); // mem
//                                for(int j = 0; j < 14; j++) {
                                    writer.write(variableName);
                                    writer.write("(");
//                                    writer.write(String.valueOf(seed + 1));
//                                    writer.write(",");
//                                    writer.write(String.valueOf(parameterisedSupplierIndex + 1));
//                                    writer.write(",");
                                    writer.write(String.valueOf(param + 1));
                                    writer.write(",");
                                    writer.write(String.valueOf(instIndex + 1));
                                    writer.write(",");
//                                    writer.write(String.valueOf(j + 1));
                                    writer.write(String.valueOf(trainOrTest + 1));
                                    writer.write(") = ");
                                    writer.write(values.get(1));
//                                    writer.write(values.get(j));
                                    writer.write(";");
                                    writer.newLine();
//                                }
                            }
                        }
                    }
                }
            }
        }
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
