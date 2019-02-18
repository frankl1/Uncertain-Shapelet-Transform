package development.go;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import net.sourceforge.sizeof.SizeOf;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.scp.ScpClient;
import org.apache.sshd.client.scp.ScpClientCreator;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.compression.CompressionZlib;
import org.apache.sshd.common.kex.KexProposalOption;
import org.apache.sshd.common.scp.ScpTimestamp;
import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.classifiers.ee.constituents.generators.*;
import timeseriesweka.classifiers.ee.iteration.RandomIndexIterator;
import timeseriesweka.measures.DistanceMeasure;
import utilities.ClassifierResults;
import utilities.ClassifierStats;
import utilities.InstanceTools;
import utilities.Utilities;
import utilities.range.Range;
import weka.core.Instances;

import java.io.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
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
    @Parameter(names={"-l"}, description="if running locally")
    private boolean local = false;
    @Parameter(names={"-k"}, description="killswitch")
    private String killSwitchPath;

    public static void main(String[] args) {
        SamplingExperiment samplingExperiment = new SamplingExperiment();
        new JCommander(samplingExperiment).parse(args);
        samplingExperiment.run();
    }

    private ClassifierResults getResults(double[][] predictions) {
        ClassifierResults results = new ClassifierResults();
        for(int i = 0; i < predictions.length; i++) {
            results.storeSingleResult(testInstances.get(i).classValue(), predictions[i]);
        }
        results.setNumClasses(testInstances.numClasses());
        results.setNumInstances(predictions.length);
        results.findAllStatsOnce();
        results.setBenchmark(benchmark);
        results.memory = SizeOf.deepSizeOf(nearestNeighbour);
        return results;
    }

    private long benchmark = -1;
    private NearestNeighbour nearestNeighbour;
    private Instances testInstances;
    private ScpClient scpClient;
    private ClientSession clientSession;

    private static void setPermissions(File file) {
        file.setReadable(true, false);
        file.setWritable(true, false);
        file.setExecutable(true, false);
    }

    private static void mkdir(File dir) {
        File parentFile = dir.getParentFile();
        if (parentFile != null) {
            mkdir(parentFile);
        }
        if(dir.mkdirs()) {
            setPermissions(dir);
        }
    }

    public void run() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File killswitch = new File(killSwitchPath);
                boolean stop = false;
                while (!stop) {
                    stop = !killswitch.exists();
                    try {
                        Thread.sleep(TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES));
                    } catch (InterruptedException e) {
                        stop = true;
                    }
                }
                System.out.println("killing");
                System.exit(2);
            }
        }).start();
        if(!local) {
            String user = "vte14wgu";
            int port = 22;
            String host = "cmp-18gopc.uea.ac.uk";
            SshClient sshClient = SshClient.setUpDefaultClient();
            sshClient.setServerKeyVerifier((clientSession, socketAddress, publicKey) -> true);
            System.out.println("starting ssh client");
            sshClient.start();
            try {
                System.out.println("connecting");
                clientSession = sshClient.connect(user, host, port).verify().getSession();
                BufferedReader reader = new BufferedReader(new FileReader("password"));
                String password = reader.readLine().trim();
                clientSession.addPasswordIdentity(password); // for password-based authentication
                clientSession.auth().verify();
                clientSession.setCompressionFactoriesNameList(new CompressionZlib().getName());
                System.out.println("connected");
                scpClient = ScpClientCreator.instance().createScpClient(clientSession);
                System.out.println("obtained scp client");
                experiment(true);
                experiment(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
            sshClient.stop();
        } else {
            try {
                experiment(true);
                experiment(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean resultsExist(String path) {
        try {
            ClassifierResults results = (ClassifierResults) readResults(path);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private ClassifierResults readResults(String path) throws IOException {
        InputStream inputStream;
        if(local) {
            inputStream = new FileInputStream(path);
        } else {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            OutputStream outputStream = new BufferedOutputStream(byteArrayOutputStream);
            path = path.replaceAll(" ", "\\\\ ");
            scpClient.download(path, outputStream);
            outputStream.close();
            byte[] bytes = byteArrayOutputStream.toByteArray();
            inputStream = new ByteArrayInputStream(bytes);
        }
        return readResults(inputStream);
    }

    private void writeObject(OutputStream outputStream, Object object) throws IOException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new GZIPOutputStream(new BufferedOutputStream(outputStream)));
        objectOutputStream.writeObject(object);
        objectOutputStream.close();
    }

    private void writeResults(OutputStream objectOutputStream, ClassifierResults results) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(new GZIPOutputStream(new BufferedOutputStream(objectOutputStream)));
        results.findAllStatsOnce();
        outputStream.writeDouble(results.acc);
        outputStream.writeDouble(results.balancedAcc);
        outputStream.writeDouble(results.nll);
        outputStream.writeDouble(results.mcc);
        outputStream.writeDouble(results.meanAUROC);
        outputStream.writeDouble(results.f1);
        outputStream.writeDouble(results.precision);
        outputStream.writeDouble(results.recall);
        outputStream.writeDouble(results.sensitivity);
        outputStream.writeDouble(results.specificity);
        outputStream.writeLong(results.getTestTime());
        outputStream.writeLong(results.getTrainTime());
        outputStream.writeLong(results.getBenchmark());
        outputStream.writeLong(results.memory);
        outputStream.close();
    }

    private ClassifierResults readResults(InputStream inputStream) throws IOException {
        ObjectInputStream objectInputStream = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(inputStream)));
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
        results.setBenchmark(objectInputStream.readLong());
        results.memory = objectInputStream.readLong();
        objectInputStream.close();
        return results;
    }

    private void write(String path, ClassifierResults results) throws IOException {
        if(local) {
            File parentFile = new File(path).getParentFile();
            if(parentFile != null) {
                mkdir(parentFile);
            }
            FileOutputStream outputStream = new FileOutputStream(path);
            writeResults(outputStream, results);
            setPermissions(new File(path));
        } else {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            writeResults(outputStream, results);
            byte[] bytes = outputStream.toByteArray();
//            path = "/scratch/" + path;
            String cmd = "mkdir -p \"" + new File(path).getParent() + "\"";
            clientSession.executeRemoteCommand(cmd);
            long time = System.currentTimeMillis();
            scpClient.upload(bytes, "\"" + path + "\"", PosixFilePermissions.fromString("rwxrwxr-x"), new ScpTimestamp(time, time));
        }
    }

    public void experiment(boolean skip) throws IOException {
        if(benchmark < 0) {
            System.out.println("benchmarking");
            benchmark = ClassifierResults.benchmark(); //todo change
        }
        System.out.println("experimenting");
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
        final int[] parameterBins = new int[] {
            parameterisedSuppliers.size(),
            datasets.size(),
        };
        final int numCombinations = Utilities.numCombinations(parameterBins);
        for(Integer foldIndex : foldIndices) {
            RandomIndexIterator combinationIndexIterator = new RandomIndexIterator();
            combinationIndexIterator.setRange(new Range(0, numCombinations - 1));
//            combinationIndexIterator.setSeed(0); // todo change
            while (combinationIndexIterator.hasNext()) {
                int combination = combinationIndexIterator.next();
                combinationIndexIterator.remove();
                int[] parameters = Utilities.fromCombination(combination, parameterBins);
                int parameterIndex = 0;
                ParameterisedSupplier<? extends DistanceMeasure> parameterisedSupplier = parameterisedSuppliers.get(parameters[parameterIndex++]);
                File datasetFile = datasets.get(parameters[parameterIndex++]);
                String datasetName = datasetFile.getName();
                Instances dataset = Utilities.loadDataset(datasetFile);
                Instances[] splitInstances = InstanceTools.resampleInstances(dataset, foldIndex, 0.5);
                Instances trainInstances = splitInstances[0];
                parameterisedSupplier.setParameterRanges(trainInstances);
                testInstances = splitInstances[1];
                RandomIndexIterator distanceMeasureParameterIterator = new RandomIndexIterator();
                distanceMeasureParameterIterator.setRange(new Range(0, parameterisedSupplier.size() - 1));
//                distanceMeasureParameterIterator.setSeed(0); // todo change
                while(distanceMeasureParameterIterator.hasNext()) {
                    int distanceMeasureParameter = distanceMeasureParameterIterator.next();
                    distanceMeasureParameterIterator.remove();
                    DistanceMeasure distanceMeasure = parameterisedSupplier.get(distanceMeasureParameter);
                    nearestNeighbour = new NearestNeighbour();
                    nearestNeighbour.setDistanceMeasure(distanceMeasure);
                    nearestNeighbour.setSeed(foldIndex);
                    nearestNeighbour.setTrainInstances(trainInstances);
                    nearestNeighbour.setTestInstances(testInstances);
                    int numTrainInstances = trainInstances.numInstances();
                    double nextPercentage = 0;
                    int numTestTickInstances = 0;
                    boolean printed = false;
                    for(int i = 0, j = 0; i <= numTrainInstances; i++) {
                        double percentage = (double) i / numTrainInstances;
                        if(percentage >= nextPercentage) {
                            j++;
                            nextPercentage = (double) j / 100;
                            String path = globalResultsDir
                                + "/" + datasetName + "/" + foldIndex
                                + "/" + nearestNeighbour.getDistanceMeasure()
                                + "/" + nearestNeighbour.getDistanceMeasure().getParameters()
                                + "/" + percentage + "/";
                            String trainPath = path + "train.gzip";
                            String testPath = path + "test.gzip";
                            if(!resultsExist(testPath) || !resultsExist(trainPath)) {
                                if(!printed) {
                                    printed = true;
                                    System.out.println(datasetName + "/" + foldIndex
                                        + "/" + nearestNeighbour.getDistanceMeasure()
                                        + "/" + nearestNeighbour.getDistanceMeasure().getParameters());
                                }
                                while (numTestTickInstances < i && nearestNeighbour.remainingTrainTicks()) {
                                    nearestNeighbour.trainTick();
                                    if(nearestNeighbour.willSampleTrain()) {
                                        numTestTickInstances++;
                                    }
                                }
                                nearestNeighbour.test();
                                if(!resultsExist(trainPath)) write(trainPath, getResults(nearestNeighbour.predictTrain()));
                                if(!resultsExist(testPath)) write(testPath, getResults(nearestNeighbour.predictTest()));
                            } else if(skip) {
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}
