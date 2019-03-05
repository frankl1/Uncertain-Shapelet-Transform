package development.go;

import utilities.ClassifierResults;

import java.io.*;
import java.util.zip.GZIPInputStream;

public class Test {
    public static void main(String[] args) throws IOException {
        File file = new File("/scratch/results/GunPoint/0/dtw/-w 0.3.gzip");
//        File file = new File("/run/user/33190/gvfs/sftp:host=hpc.uea.ac.uk/gpfs/home/vte14wgu/experiments/sample-train/results/snn/GunPoint/0/dtw/-w 0.1.gzip");
        ObjectInputStream objectInputStream = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(file))));
        System.out.println("benchmark " + objectInputStream.readLong());
        StringBuilder test = new StringBuilder();
        StringBuilder train = new StringBuilder();
        for(int i = 0; i <= 50 * 2; i++) {
            ClassifierResults results = readResults(objectInputStream);
            if(i % 2 == 0) {
                train.append(results.acc);
                train.append(System.lineSeparator());
            } else {
                test.append(results.acc);
                test.append(System.lineSeparator());
            }
        }
        System.out.println(train.toString());
        System.out.println("---");
        System.out.println(test.toString());
    }

    private static ClassifierResults readResults(ObjectInputStream objectInputStream) throws IOException {
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
        results.memory = objectInputStream.readLong();
        return results;
    }
}
