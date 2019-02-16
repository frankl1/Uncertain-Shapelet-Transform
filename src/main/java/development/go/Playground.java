package development.go;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import net.sourceforge.sizeof.SizeOf;
import timeseriesweka.classifiers.NeighbourClassifier;
import utilities.ClassifierResults;
import utilities.ClassifierStats;
import utilities.InstanceTools;
import utilities.Utilities;
import utilities.instances.Folds;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

public class Playground {

    @Parameter(names={"-r"}, description="results globalResultsDir", converter= FileConverter.class, required=true)
    private File globalResultsDir;
    @Parameter(names={"-d"}, description="datasets", required=true, converter = FileConverter.class)
    private List<File> datasets;
    @Parameter(names={"-s"}, description="seed")
    private List<Long> seeds;
    @Parameter(names={"-sm"}, description="seed max")
    private Long seedMax;

    public static void main(String[] args) throws Exception {
        Playground playground = new Playground();
        new JCommander(playground).parse(args);
        playground.run();
    }

    public void run() throws Exception {
        globalResultsDir.mkdirs();
        if(seedMax != null) {
            seeds = new ArrayList<>();
            for(long i = 0; i < seedMax; i++) {
                seeds.add(i);
            }
        }
        int numFolds = 10;
        for(File dataset : datasets) {
            for(Long seed : seeds) {
                System.out.println(dataset.getName() + " " + seed);
                Instances instances = Utilities.loadDataset(dataset);
                Instances[] split = InstanceTools.resampleInstances(instances, seed, 0.5);
                Instances train = split[0];
                Instances test = split[1];
                Folds folds = new Folds.Builder(train, numFolds).setSeed(seed).stratify(true).build();
                NeighbourClassifier classifier = new NeighbourClassifier();
                classifier.setSeed(seed);
//                classifier.setUseDistancesInPrediction(true);
                ClassifierResults results = new ClassifierResults();
                for(int i = 0; i < folds.size(); i++) {
                    Instances trainFold = folds.getTrain(i);
                    Instances testFold = folds.getTest(i);
                    test(classifier, trainFold, testFold, results);
                }
                File file = new File(globalResultsDir, instances.relationName() + "/train" + seed + ".csv");
                file.getParentFile().mkdirs();
                writeFile(file, results);
                file = new File(globalResultsDir, instances.relationName() + "/test" + seed + ".csv");
                file.getParentFile().mkdirs();
                writeFile(file, test(classifier, train, test));
            }
        }
    }

    public static void writeFile(File file, ClassifierResults results) throws IOException {
        Writer writer = new BufferedWriter(new FileWriter(file));
        writer.write(results.toString());
        writer.close();
    }

    public static ClassifierResults test(Classifier classifier, Instances train, Instances test) throws Exception {
        classifier.buildClassifier(train);
        ClassifierResults results = test(classifier, test);
        return results;
    }

    public static ClassifierResults test(Classifier classifier, Instances test) throws Exception {
        ClassifierResults results = new ClassifierResults();
        for(Instance testInstance : test) {
            results.storeSingleResult(testInstance.classValue(), classifier.distributionForInstance(testInstance));
        }
        results.setNumClasses(test.numClasses());
        results.setNumInstances(test.numInstances());
        results.findAllStatsOnce();
        return results;
    }

    public static ClassifierResults test(Classifier classifier, Instances train, Instances test, ClassifierResults results) throws Exception {
        classifier.buildClassifier(train);
        for(Instance testInstance : test) {
            results.storeSingleResult(testInstance.classValue(), classifier.distributionForInstance(testInstance));
        }
        results.setNumClasses(test.numClasses());
        results.setNumInstances(test.numInstances());
        results.findAllStatsOnce();
        return results;
    }
}
