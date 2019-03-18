/*
  Interface that allows the user to allow a classifier to checkpoint, i.e. 
save its current state and then load it again to continue building the model on 
a separate run.

By default this involves simply saving and loading a serialised the object 

known classifiers: none

Requires two methods 
number 

*/
package timeseriesweka.classifiers;

import utilities.Utilities;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author Tony Bagnall 2018
 */
public interface CheckpointClassifier extends Serializable{

    // fixme when saving a checkpoint, if the program is killed part way through the checkpoint file is corrupt. Need something to avoid this - perhaps a tmp file to save to then rename to the checkpoint file?

    //Set the path where checkpointed versions will be stored
    public void setSavePath(String path); // todo get save path
    //Define how to copy from a loaded object to this object
    public void copyFromSerObject(Object obj) throws Exception;

    //Override both if not using Java serialisation    
    public default void saveToFile(String filename) throws IOException{
        File file = new File(filename);
        Utilities.mkdir(file);
        FileOutputStream fos =
        new FileOutputStream(filename);
        try (ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new BufferedOutputStream(fos)))) {
            out.writeObject(this);
            fos.close();
            out.close();
        }
    }
    public default void loadFromFile(String filename) throws Exception{
        FileInputStream fis = new FileInputStream(filename);
        try (ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(fis)))) {
            Object obj=in.readObject();
            copyFromSerObject(obj);
        }
    }
    
    
}
