package development.go;

import net.sourceforge.sizeof.SizeOf;
import utilities.ClassifierResults;
import utilities.ClassifierStats;
import utilities.Utilities;
import weka.core.Instances;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

public class Playground {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
//        String dirPath = System.getProperty("user.dir") + "/resources";
//        String zipPath = System.getProperty("user.dir") + "/test.zip";
//        String internalPath = "abc";
//        zip(dirPath, zipPath, internalPath, 9);
//        GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream("/run/user/33190/gvfs/sftp:host=hpc.uea.ac.uk/gpfs/home/vte14wgu/experiments/sample-train/results/Coffee/m=dtw,n=0,f=0,s=0,p=0.4,d=false.gzip"));
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        byte[] buffer = new byte[1024];
//        int length;
//        while ((length = gzipInputStream.read(buffer)) >= 0) {
//            byteArrayOutputStream.write(buffer);
//        }
//        gzipInputStream.close();
//        System.out.println(byteArrayOutputStream.toString());
//        int numClasses = 30;
//        Random random = new Random();
//        double[][] distances = new double[numClasses - 1][];
//        Map<Integer, Map<Integer, Double>> map = new TreeMap<>();
//        for(int i = 0; i < distances.length; i++) {
//            double distance = random.nextDouble();
//            distances[i] = new double[i + 1];
//            map.computeIfAbsent(i, key -> new TreeMap<>());
//            for(int j = 0; j < distances[i].length; j++) {
//                distances[i][j] = distance;
//                map.get(i).put(j, distance);
//            }
//        }
//        System.out.println(SizeOf.deepSizeOf(distances));
//        System.out.println(SizeOf.deepSizeOf(map));

//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                double[] a = new double[10000000];
//                try {
//                    writeObjectToFile(a, new File("abc.xyz"));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//        Thread t1 = new Thread(runnable);
//        Thread t2 = new Thread(runnable);
//        t1.start();
//        t2.start();

        File datasetList = new File("/run/user/33190/gvfs/sftp:host=hpc.uea.ac.uk/gpfs/home/vte14wgu/experiments/sample-train/datasetList.txt");
        File datasetDir = new File("/run/user/33190/gvfs/sftp:host=hpc.uea.ac.uk/gpfs/home/ajb/TSCProblems2019");
        BufferedReader reader = new BufferedReader(new FileReader(datasetList));
        String dataset;
        while ((dataset = reader.readLine()) != null) {
            System.out.println(dataset);
            Instances instances = Utilities.loadDataset(new File(datasetDir, dataset));
        }
    }

    private static void writeObjectToFile(Object object, File file) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(
//            new GZIPOutputStream(
//                new BufferedOutputStream(
                    new FileOutputStream(file));//));
        out.writeObject(object);
        out.close();
    }

    private static void zip(String externalPath, String zipPath, String internalPath, int compressionLevel) throws IOException {
        if(!internalPath.endsWith("/")) {
            internalPath += "/";
        }
        ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipPath)));
        zipOutputStream.setMethod(ZipOutputStream.DEFLATED);
        zipOutputStream.setLevel(compressionLevel);
        LinkedList<File> files = new LinkedList<>();
        files.add(new File(externalPath));
        while (!files.isEmpty()) {
            File file = files.pollFirst();
            if(file.isDirectory()) {
                File[] children = file.listFiles();
                if(children != null) {
                    files.addAll(Arrays.asList(children));
                }
                zipOutputStream.putNextEntry(new ZipEntry(internalPath + file.getName() + "/"));
                zipOutputStream.closeEntry();
            } else {
                // is file
                ZipEntry zipEntry = new ZipEntry(new File(internalPath, file.getName()).getPath());
                zipOutputStream.putNextEntry(zipEntry);
                InputStream fileInputStream = new BufferedInputStream(new FileInputStream(file));
                byte[] bytes = new byte[1024];
                int length;
                while ((length = fileInputStream.read(bytes)) >= 0) {
                    zipOutputStream.write(bytes, 0, length);
                }
                fileInputStream.close();
            }
        }
        zipOutputStream.close();
    }
}
