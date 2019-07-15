package u_shapelet_transform;

import java.io.Serializable;

public class UShapeletCandidate implements Serializable {
	double[][] content;
    double [][] contentErr;
    int numChannels;
    
    //if no dimension, assume univariate
    public UShapeletCandidate(){
        numChannels = 1;
        content = new double[numChannels][];
        contentErr = new double[numChannels][];
    }
    
    public UShapeletCandidate(int numChans){
        numChannels = numChans;
        content = new double[numChannels][];
        contentErr = new double[numChannels][];
    }
    
    public UShapeletCandidate(double[] cont, double[] contErr){
        numChannels = 1;
        content = new double[numChannels][];
        contentErr = new double[numChannels][];
        content[0] = cont;
        contentErr[0] = contErr;
    }
    
    //if no dimension, assume univariate
    public void setShapeletContent(double[] cont, double[] err){
        content[0] = cont;
        contentErr[0] = err;
    }
    
    //if no dimension, assume univariate
    public void setShapeletContent(int channel, double[] cont, double[] err){
        content[channel] = cont;
        contentErr[channel] = err;
    }

    public double[] getShapeletContent(int channel){
        return content[channel]; 
    }
    
    public double[] getShapeletContentErr(int channel){
        return contentErr[channel]; 
    }
    
    public double[] getShapeletContent(){
        return content[0];
    }
    
    public double[] getShapeletContentErr(){
        return contentErr[0];
    }
    
    public int getLength(){
        return content[0].length;
    }
    
    public int getNumChannels(){
        return numChannels;
    }
}
