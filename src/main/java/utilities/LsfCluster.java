package utilities;

import java.io.*;
import java.util.function.Consumer;

import static utilities.Utilities.deserialise;
import static utilities.Utilities.serialize;

public class LsfCluster {

    private LsfCluster() {}

    private static final String busersCommand = "busers %s | grep --invert-match USER | awk '{print $%d}'";

    public static int getJobLimit() throws IOException {
        return getJobLimit("");
    }

    public static int getJobLimit(String username) throws IOException {
        return busersCommand(3, username);
    }

    public static int getNumJobs() throws IOException {
        return getNumJobs("");
    }

    public static int getNumJobs(String username) throws IOException {
        return busersCommand(4, username);
    }

    public static int getNumPendingJobs() throws IOException {
        return getNumPendingJobs("");
    }

    public static int getNumPendingJobs(String username) throws IOException {
        return busersCommand(5, username);
    }

    public static int getNumRunningJobs() throws IOException {
        return getNumRunningJobs("");
    }

    public static int getNumRunningJobs(String username) throws IOException {
        return busersCommand(6, username);
    }

    public static int getNumUnusedJobSlots() throws IOException {
        return getJobLimit() - getNumJobs();
    }

    public static String submitBsub(String bsub) throws IOException {
//        System.out.println("Submitting bsub: ");
//        System.out.println(bsub);
        Process process = new ProcessBuilder("bsub").start();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        writer.write(bsub);
        writer.close();
        return readOutput(process);
    }

    public static int busersCommand(int column) throws IOException {
        return busersCommand(column, "");
    }

    public enum Queue {
        LONG_ETH,
        SHORT_ETH;

        @Override
        public String toString() {
            return name().toLowerCase().replace('_', '-');
        }
    }

    private static final int extraMemoryInMb = 128;

    public static String compileBsubJavaJar(String jarFilePath, String mainClass, String jarArgs, Queue queue, String outputLogFilePath,
                                             String errorLogFilePath, int heapMemoryInMb) {
        return compileBsubJava("-cp " + jarFilePath + " " + mainClass,jarArgs, queue, outputLogFilePath, errorLogFilePath, heapMemoryInMb);
    }

    public static String compileBsubJavaJar(String jarFilePath, String jarArgs, Queue queue, String outputLogFilePath,
                                             String errorLogFilePath, int heapMemoryInMb) {
        return compileBsubJava("-jar " + jarFilePath, jarArgs, queue, outputLogFilePath, errorLogFilePath, heapMemoryInMb);
    }

    public static String compileBsubJava(String javaCommand, String jarArgs, Queue queue, String outputLogFilePath,
                                          String errorLogFilePath, int heapMemoryInMb) {
        return compileBsub("java " + javaCommand + " -Xmx" + heapMemoryInMb + "m" + " " + jarArgs, queue, outputLogFilePath, errorLogFilePath, heapMemoryInMb);
    }

    public static String compileBsub(String command, Queue queue, String outputLogFilePath,
                                      String errorLogFilePath, int heapMemoryInMb) {
        File outputLogFile = new File(outputLogFilePath);
        File errorLogFile = new File(errorLogFilePath);
        File outputLogFileParent = outputLogFile.getParentFile();
        if(outputLogFileParent != null) {
            outputLogFileParent.mkdirs();
        }
        File errorLogFileParent = errorLogFile.getParentFile();
        if(errorLogFileParent != null) {
            errorLogFileParent.mkdirs();
        }
        int totalMemoryInMb = heapMemoryInMb + extraMemoryInMb;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("#! /bin/sh").append(System.lineSeparator());
        stringBuilder.append("#BSUB -q ").append(queue.toString()).append(System.lineSeparator());
        stringBuilder.append("#BSUB -M ").append(totalMemoryInMb).append(System.lineSeparator());
        stringBuilder.append("#BSUB -R \"rusage[mem=").append(totalMemoryInMb).append("]\"").append(System.lineSeparator());
        stringBuilder.append("#BSUB -eo ").append(errorLogFile.getAbsolutePath()).append(System.lineSeparator());
        stringBuilder.append("#BSUB -oo ").append(outputLogFile.getAbsolutePath()).append(System.lineSeparator());
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(". /etc/profile").append(System.lineSeparator());
        stringBuilder.append("module add java/jdk1.8.0_51").append(System.lineSeparator());
        stringBuilder.append(command).append(System.lineSeparator());
        return stringBuilder.toString();
    }

    private static int busersCommand(int column, String username) throws IOException {
        return Integer.parseInt(runCommand(String.format(busersCommand, username, column)).trim());
    }

    private static String runCommand(String command) throws IOException {
        return readOutput(new ProcessBuilder("sh", "-c", command, System.lineSeparator()).start()); // todo windoze version
    }

    private static String readOutput(Process process) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        reader.lines().forEach(new Consumer<String>() {
            @Override
            public void accept(String line) {
                output.append(line);
                output.append("\n");
            }
        });
        return output.toString();
    }

}
