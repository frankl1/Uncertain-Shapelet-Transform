package development.go;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import net.sourceforge.sizeof.SizeOf;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.scp.ScpClient;
import org.apache.sshd.client.scp.ScpClientCreator;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.scp.ScpTimestamp;
import org.slf4j.LoggerFactory;
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

    public static void main(String[] args) {
        SamplingExperiment samplingExperiment = new SamplingExperiment();
        new JCommander(samplingExperiment).parse(args);
        samplingExperiment.run();
    }

    private ClassifierStats getStats() {
        double[][] predictions = nearestNeighbour.predict();
        ClassifierResults results = new ClassifierResults();
        for(int i = 0; i < testInstances.numInstances(); i++) {
            results.storeSingleResult(testInstances.get(i).classValue(), predictions[i]);
        }
        results.setNumInstances(testInstances.numInstances());
        results.setNumClasses(testInstances.numClasses());
        results.findAllStatsOnce();
        results.setTrainTime(nearestNeighbour.getTrainTime());
        results.setTestTime(nearestNeighbour.getTestTime());
        results.setBenchmark(benchmark);
        results.memory = SizeOf.deepSizeOf(nearestNeighbour);
        ClassifierStats stats = new ClassifierStats(results);
        return stats;
    }

    private long benchmark;
    private NearestNeighbour nearestNeighbour;
    private Instances testInstances;
    private ScpClient scpClient;
    private ClientSession clientSession;

    public void run() {
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
                System.out.println("connected");
                scpClient = ScpClientCreator.instance().createScpClient(clientSession);
                System.out.println("obtained scp client");
                experiment();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sshClient.stop();
        } else {
            try {
                experiment();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean resultsExist(String path) {
        try {
            ClassifierStats stats = (ClassifierStats) read(path);
            return true;
        } catch (IOException | ClassNotFoundException e) {
            return false;
        }
    }

    private Object read(String path) throws IOException, ClassNotFoundException {
        InputStream inputStream;
        if(local) {
            inputStream = new FileInputStream(path);
        } else {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            OutputStream outputStream = new BufferedOutputStream(byteArrayOutputStream);
            scpClient.download(path, outputStream);
            outputStream.close();
            byte[] bytes = byteArrayOutputStream.toByteArray();
            inputStream = new ByteArrayInputStream(bytes);
        }
        ObjectInputStream objectInputStream = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(inputStream)));
        Object object = objectInputStream.readObject();
        objectInputStream.close();
        return object;
    }

    private void writeObject(OutputStream outputStream, Object object) throws IOException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new GZIPOutputStream(new BufferedOutputStream(outputStream)));
        objectOutputStream.writeObject(object);
        objectOutputStream.close();
    }

    private void write(String path, Object object) throws IOException {
        if(local) {
            File parentFile = new File(path).getParentFile();
            if(parentFile != null) {
                parentFile.mkdirs();
            }
            writeObject(new FileOutputStream(path), object);
        } else {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            writeObject(outputStream, object);
            byte[] bytes = outputStream.toByteArray();
            path = "/scratch/" + path;
            String cmd = "mkdir -p \"" + new File(path).getParent() + "\"";
            clientSession.executeRemoteCommand(cmd);
           long time = System.currentTimeMillis();
            scpClient.upload(bytes, "\"" + path + "\"", PosixFilePermissions.fromString("rwxrwxr-x"), new ScpTimestamp(time, time));
        }
    }

    public void experiment() throws IOException {
        System.out.println("benchmarking");
        benchmark = ClassifierResults.benchmark(); //todo change
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
                final File resultsDir = new File(globalResultsDir, datasetName);
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
                    nearestNeighbour.train();
                    int numTrainInstances = trainInstances.numInstances();
                    double nextPercentage = 0;
                    int numTestTickInstances = 0;
                    for(int i = 0, j = 0; i <= numTrainInstances; i++) {
                        double percentage = (double) i / numTrainInstances;
                        if(percentage >= nextPercentage) {
                            j++;
                            nextPercentage = (double) j / 100;
                            String path = globalResultsDir
                                + "/" + datasetName + "/" + foldIndex
                                + "/" + nearestNeighbour.getDistanceMeasure()
                                + "/" + nearestNeighbour.getDistanceMeasure().getParameters()
                                + "/" + percentage + ".gzip";
                            if(!resultsExist(path)) {
                                while (numTestTickInstances < i) {
                                    nearestNeighbour.testTick();
                                    if(nearestNeighbour.hasSelectedNewTrainInstance() || !nearestNeighbour.remainingTestTicks()) {
                                        numTestTickInstances++;
                                    }
                                }
                                ClassifierStats stats = getStats();
                                write(path, stats);
                            }
                        }
                    }
                }
            }
        }
    }
}
