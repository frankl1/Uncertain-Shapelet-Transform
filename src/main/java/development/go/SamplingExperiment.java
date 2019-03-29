package development.go;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import development.go.Ee.ConstituentBuilders.ConstituentBuilder;
import development.go.Ee.ConstituentBuilders.DistanceMeasureBuilders.*;
import evaluation.storage.ClassifierResults;
import timeseriesweka.classifiers.Nn.AbstractNn;
import timeseriesweka.classifiers.Nn.Nn;
import utilities.ClassifierTools;
import utilities.InstanceTools;
import utilities.Utilities;
import weka.core.Instances;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

public class SamplingExperiment {

    // todo param validation
    @Parameter(names={"-r"}, description="results globalResultsDir", converter= FileConverter.class, required=true)
    private File globalResultsDir;
    @Parameter(names={"-s"}, description="dataset fold index", required=true)
    private List<Integer> seeds;
    @Parameter(names={"-d"}, description="datasets", required=true)
    private String datasetNamesFilePath;
    @Parameter(names={"-dd"}, description="datasets dir", required=true)
    private File datasetsDir;
    @Parameter(names={"-k"}, description="killswitch", required=true)
    private String killSwitchPath;

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
        System.out.println("benchmarking");
        long benchmark = benchmark();
        System.out.println("experimenting");
        Utilities.mkdir(globalResultsDir);
        List<String> datasetNames = Utilities.readDatasetNameList(datasetNamesFilePath);
        List<ConstituentBuilder> constituentBuilders = new ArrayList<>();
        constituentBuilders.add(new DtwBuilder());
        constituentBuilders.add(new DdtwBuilder());
        constituentBuilders.add(new WddtwBuilder());
        constituentBuilders.add(new WdtwBuilder());
        constituentBuilders.add(new LcssBuilder());
        constituentBuilders.add(new MsmBuilder());
        constituentBuilders.add(new TweBuilder());
        constituentBuilders.add(new ErpBuilder());
        List<Double> samplingPercentages = new ArrayList<>();
        for(Integer seed : seeds) {
            for(String datasetName : datasetNames) {
                System.out.println(datasetName + " " + seed);
                Instances trainInstances = ClassifierTools.loadData(datasetsDir + "/" + datasetName + "/" + datasetName + "_TRAIN.arff");
                Instances testInstances = ClassifierTools.loadData(datasetsDir + "/" + datasetName + "/" + datasetName + "_TEST.arff");
                Instances[] splitInstances = InstanceTools.resampleTrainAndTestInstances(trainInstances, testInstances, seed);
                trainInstances = splitInstances[0];
                testInstances = splitInstances[1];
                samplingPercentages.clear();
                for(int i = 0; i <= trainInstances.size(); i++) {
                    samplingPercentages.add((double) i / trainInstances.size());
                }
                for(ConstituentBuilder constituentBuilder : constituentBuilders) {
                    constituentBuilder.setUpParameters(trainInstances);
                }
                for(ConstituentBuilder constituentBuilder : constituentBuilders) {
                    for(int combination = 0; combination < constituentBuilder.size(); combination++) {
                        constituentBuilder.setParameterPermutation(combination);
                        Nn nn = constituentBuilder.build();
                        File file = new File(globalResultsDir
                            + "/" + datasetName
                            + "/" + nn.getDistanceMeasure()
                            + "/" + combination
                            + "/fold" + seed + ".csv.gzip");
                        try {
                            Utilities.mkdir(file.getParentFile());
                            if (file.createNewFile()) {
                                System.out.println(nn.toString() + " " + nn.getDistanceMeasure().getParameters());
                                nn.setSeed(seed);
                                nn.setCvTrain(true);
                                nn.setUseEarlyAbandon(false);
                                nn.setKPercentage(0);
                                nn.setUseRandomTieBreak(false);
                                ObjectOutputStream objectOutputStream = new ObjectOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(file))));
                                for (Double samplingPercentage : samplingPercentages) {
                                    nn.setSampleSizePercentage(samplingPercentage);
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
//                            else {
//                                System.out.println(nn.toString() + " " + nn.getDistanceMeasure().getParameters() + " already exists");
//                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
