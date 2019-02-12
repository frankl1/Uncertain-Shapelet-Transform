package development.go;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static development.go.SamplingExperimentAnalysis.parseVariable;

public class MoveResults {
    public static void main(String[] args) throws IOException {
        File datasetList = new File("/gpfs/home/vte14wgu/experiments/sample-train/datasetList.txt");
        File globalResultsDir = new File("/gpfs/home/vte14wgu/experiments/sample-train/results/c");
        File tmpResultsDir = new File("/gpfs/home/vte14wgu/experiments/sample-train/results/f");

        BufferedReader reader = new BufferedReader(new FileReader(datasetList));
        String dataset;
        while ((dataset = reader.readLine()) != null) {
            System.out.println(dataset);
            String resultsDir = globalResultsDir.getPath() + "/" + dataset;
            File resultsDirFile = new File(resultsDir);
            File tmpResultsDirFile = new File(tmpResultsDir + "/" + dataset);
            File[] files = resultsDirFile.listFiles();
            for(File file : files) {
                File orig = file;
                String fileName = file.getName();
                file = new File(tmpResultsDirFile, parseVariable(fileName, "f"));
                file = new File(file, parseVariable(fileName, "d"));
                file = new File(file, parseVariable(fileName, "q"));
                file = new File(file, parseVariable(fileName, "p") + ".gzip");
                file.getParentFile().mkdirs();
                Files.copy(orig.toPath(), file.toPath());
            }
        }
    }
}
