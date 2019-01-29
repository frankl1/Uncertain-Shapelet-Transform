package development.go;

import net.sourceforge.sizeof.SizeOf;
import utilities.ClassifierResults;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

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

        ObjectInputStream objectInputStream = new ObjectInputStream(new GZIPInputStream(new FileInputStream("/run/user/33190/gvfs/sftp:host=hpc.uea.ac.uk/gpfs/home/vte14wgu/experiments/sample-train/results/ElectricDevices/m=msm,n=30,f=0,s=0,p=0.25.gzip")));
        ClassifierResults classifierResults = (ClassifierResults) objectInputStream.readObject();
        objectInputStream.close();
        System.out.println(classifierResults);

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
