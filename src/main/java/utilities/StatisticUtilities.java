package utilities;

import weka.core.Instances;

public class StatisticUtilities {
    public static double populationStandardDeviation(Instances instances){

        double sumx = 0;
        double sumx2 = 0;
        double[] ins2array;
        for(int i = 0; i < instances.numInstances(); i++){
            ins2array = instances.instance(i).toDoubleArray(); // todo use extract time series rather than assumption of class val index
            for(int j = 0; j < ins2array.length-1; j++){//-1 to avoid classVal
                if(!Double.isNaN(ins2array[j])) {
                    sumx+=ins2array[j];
                    sumx2+=ins2array[j]*ins2array[j];
                }
            }
        }
        int n = instances.numInstances()*(instances.numAttributes()-1);
        double mean = sumx/n;
        return Math.sqrt(sumx2/(n)-mean*mean);

    }
}
