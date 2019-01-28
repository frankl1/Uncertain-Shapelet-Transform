package development.go;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import timeseriesweka.classifiers.ee.iteration.ElementIterator;
import timeseriesweka.classifiers.ee.iteration.RandomIndexIterator;
import utilities.ClassifierResults;

import java.io.File;
import java.util.List;

public class BenchmarkedDistanceCalculator {

    @Parameter(names={"-d"}, description="dataset list", converter= FileConverter.class, required=true)
    private List<File> datasets;
    @Parameter(names={"-r"}, description="results dir", required=true)
    private String resultsDir;

    public static void main(String[] args) {
        BenchmarkedDistanceCalculator benchmarkedDistanceCalculator = new BenchmarkedDistanceCalculator();
        new JCommander(benchmarkedDistanceCalculator).parse(args);
        try {
            benchmarkedDistanceCalculator.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void run() {
        System.out.print("benchmarking: ");
        long benchmark = ClassifierResults.benchmark();
        System.out.println(benchmark);

        ElementIterator<File> datasetIterator = new ElementIterator<>();
        datasetIterator.setIndexIterator(new RandomIndexIterator());
        datasetIterator.setList(datasets);
        datasetIterator.reset();
        while (datasetIterator.hasNext()) {
            File dataset = datasetIterator.next();
            RandomIndexIterator distanceMeasureParameterIndexIterator = new RandomIndexIterator();
            distanceMeasureParameterIndexIterator.getRange().add(0, 802);
            distanceMeasureParameterIndexIterator.reset();
            while (distanceMeasureParameterIndexIterator.hasNext()) {
                int distanceMeasureParameterIndex = distanceMeasureParameterIndexIterator.next();
                System.out.println(dataset.getName() + " " + distanceMeasureParameterIndex);
                DistanceCalculator.main(new String[] {
                        "-d", dataset.getPath(),
                        "-c", String.valueOf(distanceMeasureParameterIndex),
                        "-r", resultsDir,
                        "-b", String.valueOf(benchmark)}
                );
            }
        }
    }
}
