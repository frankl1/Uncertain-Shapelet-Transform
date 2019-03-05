package development.go;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import development.go.Ee.Constituents.ParameterSpaces.*;
import timeseriesweka.classifiers.nn.Nn;
import timeseriesweka.measures.DistanceMeasure;
import utilities.ClassifierResults;
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

    private SamplingExperiment() {}

    // todo param validation
    @Parameter(names={"-r"}, description="results globalResultsDir", converter= FileConverter.class, required=true)
    private File globalResultsDir;
    @Parameter(names={"-f"}, description="dataset fold index", required=true)
    private List<Integer> foldIndices;
    @Parameter(names={"-d"}, description="datasets", required=true)
    private List<File> datasets;
    @Parameter(names={"-k"}, description="killswitch")
    private String killSwitchPath;

    public static void main(String[] args) {
        SamplingExperiment samplingExperiment = new SamplingExperiment();
        new JCommander(samplingExperiment).parse(args);
        samplingExperiment.run();
    }

    private long benchmark = -1;
    private Nn nn;
    
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
            experiment(true);
//            System.out.println("verification");
//            experiment(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    private static void writeDouble(ObjectOutputStream objectOutputStream, double d) throws IOException {
        if(Double.isNaN(d)) {
            d = 0;
        }
        objectOutputStream.writeDouble(d);
    }

    private void writeResults(ObjectOutputStream objectOutputStream, ClassifierResults results) throws IOException {
        results.findAllStatsOnce();
        writeDouble(objectOutputStream, results.acc);
        writeDouble(objectOutputStream, results.balancedAcc);
        writeDouble(objectOutputStream, results.nll);
        writeDouble(objectOutputStream, results.mcc);
        writeDouble(objectOutputStream, results.meanAUROC);
        writeDouble(objectOutputStream, results.f1);
        writeDouble(objectOutputStream, results.precision);
        writeDouble(objectOutputStream, results.recall);
        writeDouble(objectOutputStream, results.sensitivity);
        writeDouble(objectOutputStream, results.specificity);
        objectOutputStream.writeLong(results.getTestTime());
        objectOutputStream.writeLong(results.getTrainTime());
        objectOutputStream.writeLong(results.memory);
    }

    private ClassifierResults readResults(ObjectInputStream objectInputStream) throws IOException {
        ClassifierResults results = new ClassifierResults();
        double acc = objectInputStream.readDouble(); // acc
        double balacc = objectInputStream.readDouble(); // balacc
        double nll = objectInputStream.readDouble(); // nll
        double mcc = objectInputStream.readDouble(); // mcc
        double meanAuroc = objectInputStream.readDouble(); // meanauroc
        double fOne = objectInputStream.readDouble(); // f1
        double precision = objectInputStream.readDouble(); // prec
        double recall = objectInputStream.readDouble(); // reca
        double sensitivity = objectInputStream.readDouble(); // sens
        double specificity = objectInputStream.readDouble(); // spec
        long testTime = objectInputStream.readLong(); // test time
        long trainTime = objectInputStream.readLong(); // train time
        long memory = objectInputStream.readLong(); // mem
        results.acc = acc;
        results.balancedAcc = balacc;
        results.nll = nll;
        results.mcc = mcc;
        results.meanAUROC = meanAuroc;
        results.f1 = fOne;
        results.precision = precision;
        results.specificity = specificity;
        results.recall = recall;
        results.setTestTime(testTime);
        results.setTrainTime(trainTime);
        results.sensitivity = sensitivity;
        results.memory = memory;
        return results;
    }

    private List<ClassifierResults> readResultsSequence(InputStream inputStream, int size) throws IOException {
        ObjectInputStream objectInputStream = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(inputStream)));
        List<ClassifierResults> list = new ArrayList<>();
        long benchmark = objectInputStream.readLong();
        for(int i = 0; i < size; i++) {
            ClassifierResults results = readResults(objectInputStream);
            results.setBenchmark(benchmark);
            list.add(results);
        }
        return list;
    }

    public void experiment(boolean skip) throws Exception {
        if(benchmark < 0) {
            System.out.println("benchmarking");
            benchmark = ClassifierResults.benchmark();
        }
        System.out.println("experimenting");
        List<ParameterSpace<? extends DistanceMeasure>> parameterSpaces = new ArrayList<>();
        parameterSpaces.add(new DtwParameterSpace());
        parameterSpaces.add(new DdtwParameterSpace());
        parameterSpaces.add(new WdtwParameterSpace());
        parameterSpaces.add(new WddtwParameterSpace());
        parameterSpaces.add(new LcssParameterSpace());
        parameterSpaces.add(new MsmParameterSpace());
        parameterSpaces.add(new TweParameterSpace());
        parameterSpaces.add(new ErpParameterSpace());
        Random random = new Random();
        Collections.shuffle(datasets,random);
        for(Integer foldIndex : foldIndices) {
            for(File datasetFile : datasets) {
                String datasetName = datasetFile.getName();
                Instances trainInstances = ClassifierTools.loadData(datasetFile + "/" + datasetName + "_TRAIN.arff");
                Instances testInstances = ClassifierTools.loadData(datasetFile + "/" + datasetName + "_TEST.arff");
                Instances[] splitInstances = InstanceTools.resampleTrainAndTestInstances(trainInstances, testInstances, foldIndex);
                trainInstances = splitInstances[0];
                testInstances = splitInstances[1];
                for(ParameterSpace<? extends DistanceMeasure> parameterSpace : parameterSpaces) {
                    parameterSpace.useInstances(trainInstances);
                    int numCombinations = parameterSpace.size();
                    List<Integer> combinations = new ArrayList<>();
                    for(int i = 0; i < numCombinations; i++) {
                        combinations.add(i);
                    }
                    Collections.shuffle(combinations, random);
                    parameterSpace.setCombination(0);
                    System.out.println(datasetName + " " + foldIndex
                        + " " + parameterSpace.build());
                    for(Integer combination : combinations) {
                        parameterSpace.setCombination(combination);
                        DistanceMeasure distanceMeasure = parameterSpace.build();
                        nn = new Nn();
                        nn.setDistanceMeasure(distanceMeasure);
                        nn.setSeed(foldIndex);
                        nn.setCvTrain(true);
                        nn.setUseEarlyAbandon(false);
                        nn.setKPercentage(0);
                        nn.setUseRandomTieBreak(false); // todo version with rand tie break
                        int numTrainInstances = trainInstances.numInstances();
                        String path = globalResultsDir
                            + "/" + datasetName
                            + "/" + foldIndex
                            + "/" + nn.getDistanceMeasure()
                            + "/" + nn.getDistanceMeasure().getParameters() + ".gzip";
                        File file = new File(path);
                        Utilities.mkdir(file.getParentFile());
                        if(file.createNewFile()) {
                            ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(file))));
                            out.writeLong(benchmark);
                            for(int i = 0; i <= numTrainInstances; i++) {
//                                System.out.println(i + " of " + numTrainInstances);
                                if(i == numTrainInstances) {
                                    System.out.println();
                                }
                                nn.setSampleSizePercentage((double) i / numTrainInstances);
                                ClassifierResults trainResults = nn.getTrainPrediction(trainInstances);
                                ClassifierResults testResults = nn.getTestPrediction(trainInstances, testInstances);
                                writeResults(out, trainResults);
                                writeResults(out, testResults);
                            }
                            out.close();
                        }
                    }
                }
            }
        }
    }
}
