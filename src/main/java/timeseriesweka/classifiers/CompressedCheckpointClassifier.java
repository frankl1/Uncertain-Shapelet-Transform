package timeseriesweka.classifiers;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public interface CompressedCheckpointClassifier extends CheckpointClassifier { // todo integrate this into checkpointClassifier
    @Override
    default void saveToFile(String path) throws IOException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(path))));
        objectOutputStream.writeObject(this);
        objectOutputStream.close();
    }

    @Override
    default void loadFromFile(String path) throws Exception {
        ObjectInputStream objectInputStream = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(path))));
        Object object = objectInputStream.readObject();
        objectInputStream.close();
        copyFromSerObject(object);
    }
}
