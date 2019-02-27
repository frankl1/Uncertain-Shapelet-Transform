//package development.go;
//
//import com.beust.jcommander.JCommander;
//import com.beust.jcommander.Parameter;
//import com.beust.jcommander.converters.FileConverter;
//import net.sourceforge.sizeof.SizeOf;
//import timeseriesweka.classifiers.nearest_neighbour.NearestNeighbour;
//import timeseriesweka.classifiers.ee.constituents.generators.*;
//import timeseriesweka.classifiers.ee.iteration.ElementIterator;
//import timeseriesweka.classifiers.ee.iteration.RandomIndexIterator;
//import timeseriesweka.measures.DistanceMeasure;
//import utilities.ClassifierResults;
//import utilities.InstanceTools;
//import utilities.Utilities;
//import utilities.range.Range;
//import weka.core.Instances;
//
//import java.io.*;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//import java.util.zip.GZIPInputStream;
//import java.util.zip.GZIPOutputStream;
//
//public class SamplingExperiment {
//
//    private SamplingExperiment() {}
//
//    // todo param validation
//    @Parameter(names={"-r"}, description="results globalResultsDir", converter= FileConverter.class, required=true)
//    private File globalResultsDir;
//    @Parameter(names={"-f"}, description="dataset fold index", required=true)
//    private List<Integer> foldIndices;
//    @Parameter(names={"-d"}, description="datasets", required=true)
//    private List<File> datasets;
//    @Parameter(names={"-k"}, description="killswitch")
//    private String killSwitchPath;
//
//    public static void main(String[] args) {
//        SamplingExperiment samplingExperiment = new SamplingExperiment();
//        new JCommander(samplingExperiment).parse(args);
//        samplingExperiment.run();
//    }
//
//    private ClassifierResults getResults(double[][] predictions) {
//        ClassifierResults results = new ClassifierResults();
//        for(int i = 0; i < predictions.length; i++) {
//            results.storeSingleResult(testInstances.get(i).classValue(), predictions[i]);
//        }
//        results.setNumClasses(testInstances.numClasses());
//        results.setNumInstances(predictions.length);
//        results.findAllStatsOnce();
//        results.setBenchmark(benchmark);
//        results.setTrainTime(nearestNeighbour.getTrainDuration());
//        results.setTestTime(nearestNeighbour.getTestDuration());
//        results.memory = SizeOf.deepSizeOf(nearestNeighbour);
//        return results;
//    }
//
//    private long benchmark = -1;
//    private NearestNeighbour nearestNeighbour;
//    private Instances testInstances;
//
//    private static void setPermissions(File file) {
//        file.setReadable(true, false);
//        file.setWritable(true, false);
//        file.setExecutable(true, false);
//    }
//
//    private static void mkdir(File dir) {
//        File parentFile = dir.getParentFile();
//        if (parentFile != null) {
//            mkdir(parentFile);
//        }
//        if(dir.mkdirs()) {
//            setPermissions(dir);
//        }
//    }
//
//    public void run() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                File killswitch = new File(killSwitchPath);
//                boolean stop = false;
//                while (!stop) {
//                    stop = !killswitch.exists();
//                    try {
//                        Thread.sleep(TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES));
//                    } catch (InterruptedException e) {
//                        stop = true;
//                    }
//                }
//                System.out.println("killing");
//                System.exit(2);
//            }
//        }).start();
//        try {
//            experiment(true);
////            System.out.println("verification");
////            experiment(false);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.exit(0);
//    }
//
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
////    private ClassifierResults readResults(InputStream inputStream) throws IOException {
////        ObjectInputStream objectInputStream = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(inputStream)));
////        ClassifierResults results = new ClassifierResults();
////        results.acc = objectInputStream.readDouble();
////        results.balancedAcc = objectInputStream.readDouble();
////        results.nll = objectInputStream.readDouble();
////        results.mcc = objectInputStream.readDouble();
////        results.meanAUROC = objectInputStream.readDouble();
////        results.f1 = objectInputStream.readDouble();
////        results.precision = objectInputStream.readDouble();
////        results.recall = objectInputStream.readDouble();
////        results.sensitivity = objectInputStream.readDouble();
////        results.specificity = objectInputStream.readDouble();
////        results.setTestTime(objectInputStream.readLong());
////        results.setTrainTime(objectInputStream.readLong());
////        results.setBenchmark(objectInputStream.readLong());
////        results.memory = objectInputStream.readLong();
////        return results;
////    }
////
////    private boolean write(String path, ClassifierResults results) throws IOException {
////        File file = new File(path);
////        if(!file.createNewFile()) {
////            return false;
////        }
////        File parentFile = file.getParentFile();
////        if(parentFile != null) {
////            mkdir(parentFile);
////        }
////        FileOutputStream outputStream = new FileOutputStream(path);
////        writeResults(outputStream, results);
////        setPermissions(new File(path));
////        return true;
////    }
//
//    public void experiment(boolean skip) throws IOException {
//        if(benchmark < 0) {
//            System.out.println("benchmarking");
//            benchmark = ClassifierResults.benchmark(); //todo change
//        }
//        System.out.println("experimenting");
//        List<ParameterisedSupplier<? extends DistanceMeasure>> parameterisedSuppliers = new ArrayList<>();
//        parameterisedSuppliers.add(new DtwParameterisedSupplier());
//        parameterisedSuppliers.add(new DdtwParameterisedSupplier());
//        parameterisedSuppliers.add(new WdtwParameterisedSupplier());
//        parameterisedSuppliers.add(new WddtwParameterisedSupplier());
//        parameterisedSuppliers.add(new LcssParameterisedSupplier());
//        parameterisedSuppliers.add(new MsmParameterisedSupplier());
//        parameterisedSuppliers.add(new TweParameterisedSupplier());
//        parameterisedSuppliers.add(new ErpParameterisedSupplier());
////        parameterisedSuppliers.add(new EuclideanParameterisedSupplier());
//        ElementIterator<File> datasetIterator = new ElementIterator<>();
//        datasetIterator.setIndexIterator(new RandomIndexIterator());
//        datasetIterator.setList(datasets);
//        for(Integer foldIndex : foldIndices) {
//            while (datasetIterator.hasNext()) {
//                File datasetFile = datasetIterator.next();
//                datasetIterator.remove();
//                String datasetName = datasetFile.getName();
//                Instances dataset = Utilities.loadDataset(datasetFile);
//                Instances[] splitInstances = InstanceTools.resampleInstances(dataset, foldIndex, 0.5);
//                Instances trainInstances = splitInstances[0];
//                ElementIterator<ParameterisedSupplier<? extends DistanceMeasure>> distanceMeasureIterator = new ElementIterator<>(parameterisedSuppliers, new RandomIndexIterator());
//                while (distanceMeasureIterator.hasNext()) {
//                    ParameterisedSupplier<? extends DistanceMeasure> parameterisedSupplier = distanceMeasureIterator.next();
//                    distanceMeasureIterator.remove();
//                    parameterisedSupplier.setParameterRanges(trainInstances);
//                    testInstances = splitInstances[1];
//                    RandomIndexIterator distanceMeasureParameterIterator = new RandomIndexIterator();
//                    distanceMeasureParameterIterator.setRange(new Range(0, parameterisedSupplier.size() - 1));
//                    while(distanceMeasureParameterIterator.hasNext()) {
//                        int distanceMeasureParameter = distanceMeasureParameterIterator.next();
//                        distanceMeasureParameterIterator.remove();
//                        DistanceMeasure distanceMeasure = parameterisedSupplier.get(distanceMeasureParameter);
//                        nearestNeighbour = new NearestNeighbour();
//                        nearestNeighbour.setDistanceMeasure(distanceMeasure);
//                        nearestNeighbour.setSeed(foldIndex);
//                        nearestNeighbour.setTrain(trainInstances);
//                        nearestNeighbour.setTest(testInstances);
//                        int numTrainInstances = trainInstances.numInstances();
//                        String path = globalResultsDir
//                            + "/" + datasetName
//                            + "/" + foldIndex
//                            + "/" + nearestNeighbour.getDistanceMeasure()
//                            + "/" + nearestNeighbour.getDistanceMeasure().getParameters() + ".gzip";
//                        System.out.println(datasetName + " " + foldIndex
//                            + " " + nearestNeighbour.getDistanceMeasure()
//                            + " " + nearestNeighbour.getDistanceMeasure().getParameters());
//                        File file = new File(path);
//                        mkdir(file.getParentFile());
//                        if(file.createNewFile()) {
//                            ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(file))));
//                            out.writeLong(benchmark);
//                            for(int i = 0; i <= numTrainInstances; i++) {
////                                System.out.println(i + " of " + numTrainInstances);
//                                while (!nearestNeighbour.willSampleTrain()) {
//                                    nearestNeighbour.trainTick();
//                                }
//                                nearestNeighbour.test();
//                                writeResults(out, getResults(nearestNeighbour.predictTrain()));
//                                writeResults(out, getResults(nearestNeighbour.predictTest()));
//                                if(nearestNeighbour.hasNextTrainTick()) {
//                                    nearestNeighbour.trainTick();
//                                }
//                            }
//                            out.close();
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
