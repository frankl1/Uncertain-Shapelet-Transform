package timeseriesweka.classifiers;

import timeseriesweka.classifiers.ee.constituents.generators.*;
import timeseriesweka.classifiers.ensembles.elastic_ensemble.*;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.dtw.Dtw;
import utilities.*;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class NnOld extends AbstractClassifier {
    private Instances trainInstances;

    public DistanceMeasure getDistanceMeasure() {
        return distanceMeasure;
    }

    public void setDistanceMeasure(final DistanceMeasure distanceMeasure) {
        this.distanceMeasure = distanceMeasure;
    }

    private DistanceMeasure distanceMeasure = new Dtw();

    @Override
    public void buildClassifier(final Instances trainInstances) throws Exception {
        this.trainInstances = trainInstances;
    }

    @Override
    public double classifyInstance(final Instance instance) throws Exception {
        return Utilities.argMax(distributionForInstanceOther(instance))[0];
    }

    //    @Override
    public double[] distributionForInstanceOther(final Instance testInstance) throws Exception {
        double bestDistance = Double.POSITIVE_INFINITY;
        final double[] neighbours = new double[testInstance.numClasses()];
        for(int i = 0; i < trainInstances.numInstances(); i++) {
            Instance trainInstance = trainInstances.get(i);
            final double distance = distanceMeasure.distance(testInstance, trainInstance); // todo cutoff
            if(distance <= bestDistance) {
                if(distance < bestDistance) {
                    bestDistance = distance;
                    for(int j = 0; j < neighbours.length; j++) {
                        neighbours[j] = 0;
                    }
                }
                neighbours[(int) trainInstance.classValue()]++;
            }
        }
        ArrayUtilities.normalise(neighbours);
        return neighbours;
    }

    @Override
    public double[] distributionForInstance(Instance instance) throws Exception {
        double[] res=new double[instance.numClasses()];
        int r=(int)classifyInstance(instance);
        res[r]=1;
        return res;
    }

    public static void main(String[] args) throws Exception {
        ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        executorService.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        String datasetsPath = "/scratch/Datasets/TSCProblems2015/";
        System.out.println(datasetsPath);
        List<String> datasetNames = new ArrayList<>();
        for(File file : new File(datasetsPath).listFiles(new FileFilter() {
            @Override
            public boolean accept(final File file) {
                return file.isDirectory();
            }
        })) {
            datasetNames.add(file.getName());
        }
//        BufferedReader reader = new BufferedReader(new FileReader("/scratch/datasetList.txt"));
//        String line;
//        while ((line = reader.readLine()) != null) {
//            datasetNames.add(line);
//        }
        int foldIndex = 0;
        String type = "DTW";
        for(String datasetName : datasetNames) {
            String datasetPath = datasetsPath + datasetName;
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    try {
                        Instances[] split = Utilities.loadSplitInstances(new File(datasetPath));
                        Instances trainInstances = split[0];
                        Instances testInstances = split[1];
                        DTW1NN orig = new DTW1NN();
                        Dtw1Nn2 orig2 = new Dtw1Nn2();
                        NnOld nn = new NnOld();
                        DtwParameterisedSupplier ps = new DtwParameterisedSupplier();
                        ps.setParameterRanges(trainInstances);
                        String previousTestResult = "/run/user/33190/gvfs/smb-share:server=cmptscsvr.cmp.uea.ac.uk,share=ueatsc/Results_7_2_19/JayMovingInProgress/EEConstituentResults/" + type + "_Rn_1NN/Predictions/" + datasetName + "/testFold" + foldIndex + ".csv";
                        String previousTrainResult = "/run/user/33190/gvfs/smb-share:server=cmptscsvr.cmp.uea.ac.uk,share=ueatsc/Results_7_2_19/JayMovingInProgress/EEConstituentResults/" + type + "_Rn_1NN/Predictions/" + datasetName + "/trainFold" + foldIndex + ".csv";
                        BufferedReader testReader = new BufferedReader(new FileReader(previousTestResult));
                        testReader.readLine();
                        int testParam = Integer.parseInt(testReader.readLine());
                        double testAcc = Double.parseDouble(testReader.readLine());
                        testReader.close();
                        BufferedReader trainReader = new BufferedReader(new FileReader(previousTrainResult));
                        trainReader.readLine();
                        int trainParam = Integer.parseInt(trainReader.readLine());
                        double trainAcc = Double.parseDouble(trainReader.readLine());
                        trainReader.close();
                        nn.setDistanceMeasure(ps.get(testParam));
                        orig.setParamsFromParamId(trainInstances, testParam);
                        orig2.setParamsFromParamId(trainInstances, testParam);
                        ClassifierResults origTestResults = trainAndTest(orig, trainInstances, testInstances);
                        ClassifierResults orig2TestResults = trainAndTest(orig, trainInstances, testInstances);
                        ClassifierResults nnTestResults = trainAndTest(nn, trainInstances, testInstances);
                        nn.setDistanceMeasure(ps.get(trainParam));
                        orig.setParamsFromParamId(trainInstances, trainParam);
                        orig2.setParamsFromParamId(trainInstances, trainParam);
                        double nnLoocv = nn.loocv();
                        double origLoocv = orig.loocvAccAndPreds(trainInstances,  trainParam)[0];
                        double orig2Loocv = orig.loocvAccAndPreds(trainInstances,  trainParam)[0];
                        System.out.println(datasetName
                            + ", " + origLoocv
                            + ", " + orig2Loocv
                            + ", " + nnLoocv
                            + ", " + trainAcc
                            + ", " + origTestResults.acc
                            + ", " + orig2TestResults.acc
                            + ", " + nnTestResults.acc
                            + ", " + testAcc
                        );
                    } catch (Exception e) {
//                        e.printStackTrace();
                    }
                }
            });
        }
        executorService.shutdown();
//        int weight = 1;
//        for(Instance instance : trainInstances) {
//            instance.setWeight(weight++);
//        }
//        for(Instance instance : testInstances) {
//            instance.setWeight(weight++);
//        }
//        MSM1NN orig = new MSM1NN();
//        ERP1NN orig = new ERP1NN();
//        TWE1NN orig = new TWE1NN();
//        WDTW1NN orig = new WDTW1NN();
//        WDTW1NN orig = new WDTW1NN();
//        LCSS1NN orig = new LCSS1NN();
//        MsmParameterisedSupplier ps = new MsmParameterisedSupplier();
//        WddtwParameterisedSupplier ps = new WddtwParameterisedSupplier();
//        Instances all = new Instances(trainInstances);
//        all.addAll(testInstances);
//        WdtwParameterisedSupplier ps = new WdtwParameterisedSupplier();
//        LcssParameterisedSupplier ps = new LcssParameterisedSupplier();
//        TweParameterisedSupplier ps = new TweParameterisedSupplier();
//        ErpParameterisedSupplier ps = new ErpParameterisedSupplier();
//        Instances derivTrainInstances = new DerivativeFilter().process(trainInstances);
//        Instances derivTestInstances = new DerivativeFilter().process(testInstances);
//        for(int param = 1; param < 2; param++) {
//            orig.setParamsFromParamId(trainInstances, param);
//            nn.setDistanceMeasure(ps.get(param));
//            ClassifierResults origTestResults = trainAndTest(orig, derivTrainInstances, derivTestInstances);
//            for(Instance i : all) {
//                for(Instance j : all) {
//                    double a = nn.distanceMeasure.distance(i, j);
//                    double b = orig.distance(i, j, Double.POSITIVE_INFINITY);
//                    if(a != b) {
//                        System.out.println(asString(extractTimeSeries(i)));
//                        System.out.println(asString(extractTimeSeries(j)));
//                        System.out.println(a);
//                        System.out.println(b);
//                        throw new IllegalArgumentException(i.weight() + " err " + j.weight());
//                    }
//                }
//            }
//            ClassifierResults origTestResults = trainAndTest(orig, trainInstances, testInstances);
//            ClassifierResults nnTestResults = trainAndTest(nn, trainInstances, testInstances);
//            double nnTrainAcc = nn.loocv();
//            double origTrainAcc = orig.loocvAccAndPreds(derivTrainInstances, param)[0];
//            double origTrainAcc = orig.loocvAccAndPreds(trainInstances, param)[0];
//            System.out.print(origTrainAcc + ", ");
//            System.out.println(datasetName);
//            System.out.print(origTestResults.acc + ", ");
//            System.out.print(nnTrainAcc + ", ");
//            System.out.println(nnTestResults.acc);
//            if(/*origTrainAcc != nnTrainAcc || */ origTestResults.acc != nnTestResults.acc) {
//                System.out.println(param);
////                System.out.println("----");
////                System.out.println(origTestResults.toString());
////                System.out.println("----");
////                System.out.println(nnTestResults.toString());
////                System.out.println("----");
//            }
//        }



//        BufferedReader reader = new BufferedReader(new FileReader("/scratch/datasetList.txt"));
//        String[] datasetNames = new String[] {
////            "SmoothSubspace",
//            "GunPoint",
////            "Fish",
////            "Adiac",
////            "ItalyPowerDemand"
//        };
//        for(String datasetName : datasetNames) {
//            String datasetPath = datasetsPath + datasetName;
//            for(int i = 0; i < 1; i++) {
//                final int finalI = i;
////                executorService.submit(() -> {
////                    try {
//                        Instances[] split = Utilities.loadSplitInstances(new File(datasetPath));
//                        int weight = 0;
//                        for(int j = 0; j < split[0].numInstances(); j++, weight++) {
//                            split[0].get(j).setWeight(weight);
//                        }
//                        for(int j = 0; j < split[1].numInstances(); j++, weight++) {
//                            split[1].get(j).setWeight(weight);
//                        }
//                        split = InstanceTools.resampleTrainAndTestInstances(split[0], split[1], finalI);
//                        Instances trainInstances = split[0];
//                        Nn nn = new Nn();
//                        nn.setRandomTieBreak(false);
//                        List<ParameterisedSupplier<? extends DistanceMeasure>> parameterisedSuppliers = new ArrayList<>();
//                        parameterisedSuppliers.add(new DtwParameterisedSupplier());
//                        parameterisedSuppliers.add(new DdtwParameterisedSupplier());
//                        parameterisedSuppliers.add(new WdtwParameterisedSupplier());
//                        parameterisedSuppliers.add(new WddtwParameterisedSupplier());
//                        parameterisedSuppliers.add(new LcssParameterisedSupplier());
//                        parameterisedSuppliers.add(new MsmParameterisedSupplier());
//                        parameterisedSuppliers.add(new TweParameterisedSupplier());
//                        parameterisedSuppliers.add(new ErpParameterisedSupplier());
//                        Efficient1NN[] jays = new Efficient1NN[] {
//                            new DTW1NN(),
//                            null,
//                            new WDTW1NN(),
//                            null,
//                            new LCSS1NN(),
//                            new MSM1NN(),
//                            new TWE1NN(),
//                            new ERP1NN()
//                        };
//                        Instances testInstances = split[1];
//                        for(int k = 0; k < parameterisedSuppliers.size(); k++) {
//                            Efficient1NN e = jays[k];
//                            ParameterisedSupplier<? extends DistanceMeasure> parameterisedSupplier = parameterisedSuppliers.get(k);
//                            parameterisedSupplier.setParameterRanges(trainInstances);
//                            String window = "";
//                            String type = parameterisedSupplier.toString().toUpperCase();
//                            if(type.equalsIgnoreCase("dtw") || type.equalsIgnoreCase("ddtw")) {
//                                window = "_Rn";
//                            }
//                            String path = "/run/user/33190/gvfs/smb-share:server=cmptscsvr.cmp.uea.ac.uk,share=ueatsc/Results_7_2_19/JayMovingInProgress/EEConstituentResults/" + type + window +
//                                "_1NN/Predictions/" + datasetName + "/testFold" + finalI + ".csv";
//                            BufferedReader reader = new BufferedReader(new FileReader(path));
//                            reader.readLine();
//                            int param = Integer.parseInt(reader.readLine());
//                            DistanceMeasure distanceMeasure = parameterisedSupplier.get(param);
//                            nn.setDistanceMeasure(distanceMeasure);
//                            if(e != null) e.setParamsFromParamId(trainInstances, param);
//                            if(e != null) e.setParamsFromParamId(trainInstances, 99);
//                            if(e != null) e.setParamsFromParamId(trainInstances, 0);
//                            ClassifierResults nnResults = trainAndTest(nn, trainInstances, testInstances);
//                            double testAcc = nnResults.acc;
//                            double oldTestAcc = Double.parseDouble(reader.readLine());
//                            reader.close();
//                            path = "/run/user/33190/gvfs/smb-share:server=cmptscsvr.cmp.uea.ac.uk,share=ueatsc/Results_7_2_19/JayMovingInProgress/EEConstituentResults/" + type + window +
//                                "_1NN/Predictions/" + datasetName + "/testFold" + finalI + ".csv";
//                            reader = new BufferedReader(new FileReader(path));
//                            reader.readLine();
//                            reader.readLine();
//                            double trainAcc = nn.loocv();
//                            double oldTrainAcc = Double.parseDouble(reader.readLine());
//                            reader.close();
//
//                            System.out.println(datasetName + "_" + finalI + "-" + type + ": " + trainAcc + " vs " + oldTrainAcc + ", " + testAcc + " vs " + oldTestAcc);
////                            if(testAcc != oldTestAcc || trainAcc != oldTrainAcc) {
////                                throw new Exception();
////                            }
//                        }
////                        DTW1NN orig = new DTW1NN();
////                        orig.setParamsFromParamId(trainInstances, 0);
////                        Instances testInstances = new Instances(trainInstances); // todo change this back, only for sanity checking!
//
////                        Instance trainInstance = trainInstances.get(4);
////                        Instance testInstance = trainInstances.get(0);
//
////                    System.out.println(nn.distanceMeasure.distance(trainInstance, testInstance));
////                        System.out.println("---");
////                    System.out.println(orig.distance(trainInstance, testInstance, Double.MAX_VALUE));
////
////                    System.exit(1);
//
////                        StringBuilder stringBuilder = new StringBuilder();
////                        stringBuilder.append(trainInstances.relationName());
////                        stringBuilder.append("_");
////                        stringBuilder.append(finalI);
////                        stringBuilder.append(": ");
//
////                        System.out.println(nnResults.toString());
////                        stringBuilder.append(nnResults.acc);
////                        stringBuilder.append(", ");
////                        double nnLoocv = nn.loocv();
////                        stringBuilder.append(nnLoocv);
////                        stringBuilder.append(", ");
////                        ClassifierResults origResults = trainAndTest(orig, trainInstances, testInstances);
//////                        System.out.println(origResults.toString());
////                        stringBuilder.append(origResults.acc);
////                        stringBuilder.append(", ");
////                        double origLoocv = orig.loocvAccAndPreds(trainInstances, 100)[0];
////                        stringBuilder.append(origLoocv);
////                        stringBuilder.append(", ");
////                        double accDiff = nnResults.acc - origResults.acc;
////                        stringBuilder.append(accDiff);
////                        stringBuilder.append(", ");
////                        double cvDiff = nnLoocv - origLoocv;
////                        stringBuilder.append(cvDiff);
////                        String str = stringBuilder.toString();
////                        System.out.println(str);
////                    if(cvDiff != 0 || accDiff != 0) {
////                        System.exit(1);
////                    }
////                    } catch (Exception e) {
////                        e.printStackTrace();
////                    }
////                });
//            }
//        }
//        executorService.shutdown();
    }

    private double loocv() throws Exception {
//        ClassifierResults results = new ClassifierResults();
        int correct = 0;
        Instances wrappedLeftOut = new Instances(trainInstances, 0);
        for(int i = 0; i < trainInstances.numInstances(); i++) {
            Instance leftOut = trainInstances.remove(i);
//            if(leftOut.weight() == 17) {
//                boolean b = true;
//            }
            wrappedLeftOut.add(leftOut);
            if(leftOut.classValue() == classifyInstance(leftOut)) {
                correct++;
            }
//            trainAndTest(this, trainInstances, wrappedLeftOut, results);
            wrappedLeftOut.clear();
            trainInstances.add(i, leftOut);
        }
        return (double) correct / trainInstances.numInstances();
//        results.setNumClasses(trainInstances.numClasses());
//        results.setNumInstances(trainInstances.numInstances());
//        results.findAllStatsOnce();
//        System.out.println(results.toString());
//        System.out.println();
//        return results.acc;
    }

    private static ClassifierResults trainAndTest(Classifier classifier, Instances trainInstances, Instances testInstances) throws Exception {
        classifier.buildClassifier(trainInstances);
        return test(classifier, testInstances);
    }

    private static ClassifierResults trainAndTest(Classifier classifier, Instances trainInstances, Instances testInstances, ClassifierResults results) throws Exception {
        classifier.buildClassifier(trainInstances);
        return test(classifier, testInstances, results);
    }

    private static ClassifierResults test(Classifier classifier, Instances testInstances) throws Exception {
        ClassifierResults results = test(classifier, testInstances, new ClassifierResults());
        results.setNumInstances(testInstances.numInstances());
        results.setNumClasses(testInstances.numClasses());
        results.findAllStatsOnce();
        return results;
    }

    private static ClassifierResults test(Classifier classifier, Instances testInstances, ClassifierResults results) throws Exception {
        int i = 0;
        for(Instance testInstance : testInstances) {
            if(testInstance.weight() == 140) {
                boolean b = true;
            }
            double classValue = testInstance.classValue();
            if(testInstance.classValue() != classifier.classifyInstance(testInstance)) {
                boolean b = true;
            }
            double[] prediction = classifier.distributionForInstance(testInstance);
            results.storeSingleResult(classValue, prediction);
//            double pred = classifier.classifyInstance(testInstance);
//            if(classValue != pred) {
//                boolean b = true;
//                System.out.println(i);
//            }
            i++;
        }
        return results;
    }
}
