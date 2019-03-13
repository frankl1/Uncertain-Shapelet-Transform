// checked April l16

package timeseriesweka.classifiers.ensembles.elastic_ensemble;


import development.go.Ee.Constituents.ParameterSpaces.DtwParameterSpace;
import development.go.Ee.Constituents.ParameterSpaces.LcssParameterSpace;
import development.go.Ee.Constituents.ParameterSpaces.ParameterSpace;
import timeseriesweka.classifiers.nn.Nn;
import timeseriesweka.measures.dtw.Dtw;
import timeseriesweka.measures.lcss.Lcss;
import utilities.ClassifierResults;
import utilities.ClassifierTools;
import utilities.Utilities;
import weka.classifiers.lazy.kNN;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import timeseriesweka.elastic_distance_measures.LCSSDistance;
import weka.core.neighboursearch.NearestNeighbourSearch;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author sjx07ngu
 */
public class LCSS1NN extends Efficient1NN{

    private int delta;
    private double epsilon;
    
    boolean epsilonsAndDeltasRefreshed;
    double[] epsilons;
    int[] deltas;
    
    public LCSS1NN(int delta, double epsilon){
        this.delta = delta;
        this.epsilon = epsilon;
        epsilonsAndDeltasRefreshed = false;
        this.classifierIdentifier = "LCSS_1NN";
        this.allowLoocv = false;
    }

    public LCSS1NN(){
        // note: these default params may be garbage for most datasets, should set them through CV
        this.delta = 3;
        this.epsilon = 1;
        epsilonsAndDeltasRefreshed = false;
        this.classifierIdentifier = "LCSS_1NN";
    }

    @Override
    public void buildClassifier(Instances train) throws Exception {
        super.buildClassifier(train); 
        
        // used for setting params with the paramId method
        epsilonsAndDeltasRefreshed = false;
    }
    
    
    
    public double distance(Instance first, Instance second) {
        
        // need to remove class index/ignore
        // simple check - if its last, ignore it. If it's not last, copy the instances, remove that attribue, and then call again 
        //  edit: can't do a simple copy with Instance objs by the looks of things. Fail-safe: fall back to the original measure
        
        int m, n;
        if(first.classIndex()==first.numAttributes()-1 && second.classIndex()==second.numAttributes()-1){
            m = first.numAttributes()-1;
            n = second.numAttributes()-1;
        }else{
            // default case, use the original MSM class (horrible efficiency, but just in as a fail safe for edge-cases)
            System.err.println("Warning: class designed to use problems with class index as last attribute. Defaulting to original MSM distance");
            return new LCSSDistance(this.delta, this.epsilon).distance(first, second);
        }
        
        int[][] lcss = new int[m+1][n+1];

        for(int i = 0; i < m; i++){
            for(int j = i-delta; j <= i+delta; j++){
                if(j < 0){
                    j = -1;
                }else if(j >= n){
                    j = i+delta;
                }else if(second.value(j)+this.epsilon >= first.value(i) && second.value(j)-epsilon <=first.value(i)){
                    lcss[i+1][j+1] = lcss[i][j]+1;
                }else if(lcss[i][j+1] > lcss[i+1][j]){
                    lcss[i+1][j+1] = lcss[i][j+1];
                }else{
                    lcss[i+1][j+1] = lcss[i+1][j];
                }
                
                // could maybe do an early abandon here? Not sure, investigate further 
            }
        }

        int max = -1;
        for(int i = 1; i < lcss[lcss.length-1].length; i++){
            if(lcss[lcss.length-1][i] > max){
                max = lcss[lcss.length-1][i];
            }
        }
        return 1-((double)max/m);
        
    }


    @Override
    public Capabilities getCapabilities() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
//    public static void main(String[] args) throws Exception{
//        for (int i = 0; i < 10; i++) {
//            runComparison();
//        }
//    }

    public static void main(String[] args) throws Exception {
        LCSS1NN orig = new LCSS1NN();
        LcssParameterSpace parameterSpace = new LcssParameterSpace();
        Instances instances = ClassifierTools.loadData(new File("/scratch/Datasets/TSCProblems2019/GunPoint/GunPoint_TRAIN.arff"));
        parameterSpace.useInstances(instances);
//        for(int i = 0; i < parameterSpace.size(); i++) {
//            orig.setParamsFromParamId(instances, i);
//            parameterSpace.setCombination(i);
//            Lcss n = parameterSpace.build();
//            System.out.println(i);
//            System.out.println(orig.epsilon + " " + orig.delta);
//            int w = (int) Math.round(n.getWarpingWindow() * (instances.numAttributes() - 1));
//            System.out.println(n.getTolerance() + " " + w);
//            if(orig.delta != w || orig.epsilon != n.getTolerance()) {
//                throw new IllegalArgumentException();
//            }
//        }
        int param = 21;
        orig.setParamsFromParamId(instances, 20);
//        orig.buildClassifier(instances);
        parameterSpace.setCombination(20);
        Lcss lcss = parameterSpace.build();
        orig.delta = (instances.numAttributes() - 1) / 2;
        lcss.setWarpingWindow(0.5);
        for(Instance instance : instances) {
            for(Instance other : instances) {
                double a = lcss.distance(instance, other);
                double b = orig.distance(instance, other);
                System.out.print(a);
                System.out.print(", ");
                System.out.print(b);
                System.out.println();
                if(a != b) {
                    throw new Exception();
                }
            }
        }
        Nn nn = new Nn();
        nn.setUseRandomTieBreak(false);
        nn.setCvTrain(true);
        nn.setDistanceMeasure(lcss);
        nn.buildClassifier(instances);
        double o = orig.loocvAccAndPreds(instances, 20)[0];
        System.out.println(nn.getTrainPrediction().acc);
        System.out.println(o);
//        ClassifierResults results = new ClassifierResults();
//        ClassifierResults origResult = new ClassifierResults();
//        Instances test = ClassifierTools.loadData(new File("/scratch/Datasets/TSCProblems2019/GunPoint/GunPoint_TEST.arff"));
//        for(Instance instance : test) {
//            results.storeSingleResult(instance.classValue(), nn.distributionForInstance(instance));
//            origResult.storeSingleResult(instance.classValue(), orig.distributionForInstance(instance));
//        }
//        results.setNumInstances(test.numInstances());
//        results.setNumClasses(test.numClasses());
//        origResult.setNumInstances(test.numInstances());
//        origResult.setNumClasses(test.numClasses());
//        results.findAllStatsOnce();
//        origResult.findAllStatsOnce();
//        System.out.println(results.acc);
//        System.out.println(origResult.acc);
    }

    public static void runComparison() throws Exception{
        String tscProbDir = "C:/users/sjx07ngu/Dropbox/TSC Problems/";
        
//        String datasetName = "ItalyPowerDemand";
        String datasetName = "GunPoint";
//        String datasetName = "Beef";
//        String datasetName = "Coffee";
//        String datasetName = "SonyAiboRobotSurface1";

        
        Instances train = ClassifierTools.loadData(tscProbDir+datasetName+"/"+datasetName+"_TRAIN");
        Instances test = ClassifierTools.loadData(tscProbDir+datasetName+"/"+datasetName+"_TEST");
        
        int delta = 10;
        double epsilon = 0.5;       
        
        
        // old version
        kNN knn = new kNN(); //efaults to k = 1 without any normalisation
        LCSSDistance lcssOld = new LCSSDistance(delta, epsilon);
        knn.setDistanceFunction(lcssOld);
        knn.buildClassifier(train);
        
        // new version
        LCSS1NN lcssNew = new LCSS1NN(delta, epsilon);
        lcssNew.buildClassifier(train);
        
        int correctOld = 0;
        int correctNew = 0;
        
        long start, end, oldTime, newTime;
        double pred;
        
          
        
        // classification with old MSM class and kNN
        start = System.nanoTime();
        
        correctOld = 0;
        for(int i = 0; i < test.numInstances(); i++){
            pred = knn.classifyInstance(test.instance(i));
            if(pred==test.instance(i).classValue()){
                correctOld++;
            }
        }
        end = System.nanoTime();
        oldTime = end-start;
        
        // classification with new MSM and in-build 1NN
        start = System.nanoTime();
        correctNew = 0;
        for(int i = 0; i < test.numInstances(); i++){
            pred = lcssNew.classifyInstance(test.instance(i));
            if(pred==test.instance(i).classValue()){
                correctNew++;
            }
        }
        end = System.nanoTime();
        newTime = end-start;
        
        
        System.out.println("Comparison of MSM: "+datasetName);
        System.out.println("==========================================");
        System.out.println("Old acc:    "+((double)correctOld/test.numInstances()));
        System.out.println("New acc:    "+((double)correctNew/test.numInstances()));
        System.out.println("Old timing: "+oldTime);
        System.out.println("New timing: "+newTime);
        System.out.println("Relative Performance: " + ((double)newTime/oldTime));
    }

    @Override
    public double distance(Instance first, Instance second, double cutOffValue) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return this.distance(first, second);
    }

    @Override
    public void setParamsFromParamId(Instances train, int paramId) {
        // more efficient to only calculate these when the training data has been changed, so could call in build classifier
        // however, these values are only needed in this method, so calculate here. 
        // If the training data hasn't changed (i.e. no calls to buildClassifier, then they don't need recalculated 
        if(!epsilonsAndDeltasRefreshed){
            double stdTrain = LCSSDistance.stdv_p(train);
            double stdFloor = stdTrain*0.2;
            epsilons = LCSSDistance.getInclusive10(stdFloor, stdTrain);
            deltas = LCSSDistance.getInclusive10(0, (train.numAttributes()-1)/4);
            epsilonsAndDeltasRefreshed = true;
        }
        this.delta = deltas[paramId/10];
        this.epsilon = epsilons[paramId%10];
    }

    @Override
    public String getParamInformationString() {
        return this.delta+","+this.epsilon;
    }
    
    
    
    
    
}
