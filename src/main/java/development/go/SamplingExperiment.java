package development.go;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import development.go.Ee.ConstituentBuilders.ConstituentBuilder;
import development.go.Ee.ConstituentBuilders.DistanceMeasureBuilders.*;
import evaluation.storage.ClassifierResults;
import net.sourceforge.sizeof.SizeOf;
import timeseriesweka.classifiers.nn.Nn;
import timeseriesweka.measures.DistanceMeasure;
import utilities.ClassifierTools;
import utilities.InstanceTools;
import utilities.Utilities;
import weka.core.Instances;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SamplingExperiment {

    // todo param validation
    @Parameter(names={"-r"}, description="results globalResultsDir", converter= FileConverter.class, required=true)
    private File globalResultsDir;
    @Parameter(names={"-s"}, description="dataset fold index", required=true)
    private Integer seed;
    @Parameter(names={"-d"}, description="datasets", required=true)
    private File datasetFile;
    @Parameter(names={"-k"}, description="killswitch", required=true)
    private String killSwitchPath;
    @Parameter(names={"-p"}, description="sampling percentages", required=true)
    private List<Double> samplingPercentages;

    private SamplingExperiment() {}

    public static void main(String[] args) {
        SamplingExperiment samplingExperiment = new SamplingExperiment();
        new JCommander(samplingExperiment).parse(args);
        samplingExperiment.run();
    }

    public void run() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File killswitch = new File(killSwitchPath);
                boolean stop = false;
                while (!stop) {
                    stop = !killswitch.exists();
                    if(!stop) {
                        try {
                            Thread.sleep(TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES));
                        } catch (InterruptedException e) {
                            stop = true;
                        }
                    }
                }
                System.out.println("killing");
                System.exit(2);
            }
        }).start();
        try {
            experiment();
//            System.out.println("verification");
//            experiment(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

//    private static void writeDouble(ObjectOutputStream objectOutputStream, double d) throws IOException {
//        if(Double.isNaN(d)) {
//            d = 0;
//        }
//        objectOutputStream.writeDouble(d);
//    }
//
//    private void writeResults(ObjectOutputStream objectOutputStream, ClassifierResults results) throws IOException {
//        results.findAllStatsOnce();
//        writeDouble(objectOutputStream, results.acc);
//        writeDouble(objectOutputStream, results.balancedAcc);
//        writeDouble(objectOutputStream, results.nll);
//        writeDouble(objectOutputStream, results.mcc);
//        writeDouble(objectOutputStream, results.meanAUROC);
//        writeDouble(objectOutputStream, results.f1);
//        writeDouble(objectOutputStream, results.precision);
//        writeDouble(objectOutputStream, results.recall);
//        writeDouble(objectOutputStream, results.sensitivity);
//        writeDouble(objectOutputStream, results.specificity);
//        objectOutputStream.writeLong(results.getTestTime());
//        objectOutputStream.writeLong(results.getTrainTime());
//        objectOutputStream.writeLong(results.memory);
//    }
//
//    private ClassifierResults readResults(ObjectInputStream objectInputStream) throws IOException {
//        ClassifierResults results = new ClassifierResults();
//        double acc = objectInputStream.readDouble(); // acc
//        double balacc = objectInputStream.readDouble(); // balacc
//        double nll = objectInputStream.readDouble(); // nll
//        double mcc = objectInputStream.readDouble(); // mcc
//        double meanAuroc = objectInputStream.readDouble(); // meanauroc
//        double fOne = objectInputStream.readDouble(); // f1
//        double precision = objectInputStream.readDouble(); // prec
//        double recall = objectInputStream.readDouble(); // reca
//        double sensitivity = objectInputStream.readDouble(); // sens
//        double specificity = objectInputStream.readDouble(); // spec
//        long testTime = objectInputStream.readLong(); // test time
//        long trainTime = objectInputStream.readLong(); // train time
//        long memory = objectInputStream.readLong(); // mem
//        results.acc = acc;
//        results.balancedAcc = balacc;
//        results.nll = nll;
//        results.mcc = mcc;
//        results.meanAUROC = meanAuroc;
//        results.f1 = fOne;
//        results.precision = precision;
//        results.specificity = specificity;
//        results.recall = recall;
//        results.setTestTime(testTime);
//        results.setTrainTime(trainTime);
//        results.sensitivity = sensitivity;
//        results.memory = memory;
//        return results;
//    }

//    private List<ClassifierResults> readResultsSequence(InputStream inputStream, int size) throws IOException {
//        ObjectInputStream objectInputStream = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(inputStream)));
//        List<ClassifierResults> list = new ArrayList<>();
//        long benchmark = objectInputStream.readLong();
//        for(int i = 0; i < size; i++) {
//            ClassifierResults results = readResults(objectInputStream);
//            results.setBenchmark(benchmark);
//            list.add(results);
//        }
//        return list;
//    }

    private static long benchmark() {
        Random random = new Random();
        List<Long> times = new ArrayList<>();
        for(int i = 0; i < 101; i++) {
            random.setSeed(0);
            List<Integer> list = new ArrayList<>();
            for(int j = 0; j < 1000000; j++) {
                list.add(random.nextInt());
            }
            long startTime = System.nanoTime();
            Collections.sort(list);
            long endTime = System.nanoTime();
            times.add(endTime - startTime);
        }
        Collections.sort(times);
        return times.get(times.size() / 2);
    }

    public void experiment() throws Exception {
        System.out.println(datasetFile.getName());
        System.out.println(seed);
        Utilities.mkdir(globalResultsDir);
        System.out.println("benchmarking");
        long benchmark = benchmark();
        System.out.println("experimenting");
        List<ConstituentBuilder> constituentBuilders = new ArrayList<>();
        constituentBuilders.add(new DtwBuilder());
        constituentBuilders.add(new DdtwBuilder());
        constituentBuilders.add(new WddtwBuilder());
        constituentBuilders.add(new WdtwBuilder());
        constituentBuilders.add(new LcssBuilder());
        constituentBuilders.add(new MsmBuilder());
        constituentBuilders.add(new TweBuilder());
        constituentBuilders.add(new ErpBuilder());
        String datasetName = datasetFile.getName();
        Instances trainInstances = ClassifierTools.loadData(datasetFile + "/" + datasetName + "_TRAIN.arff");
        Instances testInstances = ClassifierTools.loadData(datasetFile + "/" + datasetName + "_TEST.arff");
        Instances[] splitInstances = InstanceTools.resampleTrainAndTestInstances(trainInstances, testInstances, seed);
        trainInstances = splitInstances[0];
        testInstances = splitInstances[1];
        samplingPercentages.clear();
        for(int i = 0; i <= trainInstances.size(); i++) {
            samplingPercentages.add((double) i / trainInstances.size());
        }
        Collections.sort(samplingPercentages);
        for(ConstituentBuilder constituentBuilder : constituentBuilders) {
            constituentBuilder.setUpParameters(trainInstances);
        }
        for(ConstituentBuilder constituentBuilder : constituentBuilders) {
            for(int combination = 0; combination < constituentBuilder.size(); combination++) {
                constituentBuilder.setParameterPermutation(combination);
                Nn nn = constituentBuilder.build();
                File file = new File(globalResultsDir
                    + "/Predictions/" + datasetName
                    + "/" + nn.getDistanceMeasure()
                    + "/" + nn.getDistanceMeasure().getParameters()
                    + "/fold" + seed + ".csv.gzip");
                Utilities.mkdir(file.getParentFile());
                if(file.createNewFile()) {
                    System.out.println(nn.toString() + " " + nn.getDistanceMeasure().getParameters());
                    nn.setSeed(seed);
                    nn.setCvTrain(true);
                    nn.setUseEarlyAbandon(false);
                    nn.setKPercentage(0);
                    nn.setUseRandomTieBreak(false);
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(file))));
                    for(Double samplingPercentage : samplingPercentages) {
                        nn.setSampleSizePercentage(samplingPercentage);
                        objectOutputStream.writeDouble(samplingPercentage);
                        nn.buildClassifier(trainInstances);
                        ClassifierResults trainResults = nn.getTrainResults();
                        trainResults.setBenchmarkTime(benchmark);
                        objectOutputStream.writeObject(trainResults.writeFullResultsToString());
                        ClassifierResults testResults = nn.getTestResults(testInstances);
                        testResults.setBenchmarkTime(benchmark);
                        objectOutputStream.writeObject(testResults.writeFullResultsToString());
                    }
                    objectOutputStream.close();
                }
            }
        }
    }
}
