package development.go;

import utilities.ClassifierResults;

import java.io.*;
import java.util.zip.GZIPInputStream;

public class Test {
    public static void main(String[] args) throws IOException {
        File file = new File("/run/user/33190/gvfs/sftp:host=hpc.uea.ac.uk/gpfs/home/vte14wgu/experiments/sample-train/results/nn3/Beef/0/erp/penalty=0.37737567252309867,warpingWindow=0.25.gzip");
        ObjectInputStream objectInputStream = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(file))));
        System.out.println("benchmark " + objectInputStream.readLong());
        for(int i = 0; i < 60; i++) {
            ClassifierResults results = readResults(objectInputStream);
            System.out.println(results.acc);
//            System.out.println(results.balancedAcc);
//            System.out.println(results.nll);
//            System.out.println(results.mcc);
//            System.out.println(results.meanAUROC);
//            System.out.println(results.f1);
//            System.out.println(results.precision);
//            System.out.println(results.recall);
//            System.out.println(results.sensitivity);
//            System.out.println(results.specificity);
//            System.out.println(results.getTestTime());
//            System.out.println(results.getTrainTime());
//            System.out.println(results.memory);
        }
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
