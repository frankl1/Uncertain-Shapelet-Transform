/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package evaluation;

import evaluation.storage.ClassifierResults;
import ResultsProcessing.MatlabController;
import ResultsProcessing.ResultColumn;
import ResultsProcessing.ResultTable;
import evaluation.MultipleClassifiersPairwiseTest;
import fileIO.OutFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import statistics.tests.OneSampleTests;
import statistics.tests.TwoSampleTests;
import utilities.GenericTools;
import utilities.InstanceTools;
import utilities.StatisticalUtilities;
import utilities.generic_storage.Pair;
import weka.clusterers.XMeans;
import weka.core.Instances;



/**
 *
 * This is a monster of a class, with some bad code and not enough documentation. It's improving over time however.
 If there are any questions about it, best bet would be to email me (see below).

 This class is given a much better front end/'api' in MultipleClassifierEvaluation.java. Users should almost always use
 that class for their comparative summaries of different classifiers. 
 
 The two functions from this class in particular a user would actually use in their code might be: 
 performFullEvaluation(...) and performTestAccEvalOnly(...), the former of which is the the 
 function wrapped by MultipleClassifierEvaluation
 
 Basically, this is a collection of static functions to analyse/handle COMPLETED (i.e no folds missing out 
 of those expected of the specified classifierXdatasetXfoldXsplit set) sets of results in ClassifierResults format
 
 For some reason, the excel workbook writer library i found/used makes xls files (instead of xlsx) and doesn't 
 support recent excel default fonts. Just open it and saveas xlsx if you want to

 Future work when wanted/needed would be to handle incomplete results (e.g random folds missing), more matlab figures over time, 
 and a MASSIVE refactor to remove the crap code
 * 
 * @author James Large james.large@uea.ac.uk
 */


public class ClassifierResultsAnalysis {
    
    //actual parameters
    public static String expRootDirectory;
    public static boolean buildMatlabDiagrams = false;
    public static boolean testResultsOnly = false;
    
    
    //final id's and path suffixes
    protected static final String matlabFilePath = "src/main/matlab/";
    protected static final String pairwiseScatterDiaPath = "PairwiseScatterDias/";
    protected static final String cdDiaPath = "cddias/";
    protected static final String pairwiseCDDiaDirectoryName = "pairwise/";
    protected static final String friedmanCDDiaDirectoryName = "friedman/";
    public static final double FRIEDMANCDDIA_PVAL = 0.05;
    private static final String testLabel = "TEST";
    private static final String trainLabel = "TRAIN";
    private static final String trainTestDiffLabel = "TRAINTESTDIFFS";
    public static final String clusterGroupingIdentifier = "PostHocXmeansClustering";
        
    
    protected static boolean performDeepAnalysis = true; //the ultimate in hacks and spaghetti 
    //brought in with buildtime compilation, turns off stat code while 
    //making those files, thisll be cleaned up with time
    //timings were too often 0's while measuring in long millis, and the pairwisestats
    //would throw errors because everything tied. todo should be ok to remove this now
   
    
    public static class ClassifierEvaluation  {
        public String classifierName;
        public ClassifierResults[][] testResults; //[dataset][fold]
        public ClassifierResults[][] trainResults; //[dataset][fold]
        
        public ClassifierEvaluation(String name, ClassifierResults[][] testResults, ClassifierResults[][] trainResults) {
            this.classifierName = name;
            this.testResults = testResults;
            this.trainResults = trainResults;
        }
    }
    
    //THIS IS THE METHODS YOU'D ACTUALLY USE, the public 'actually do stuff' method    
    public static void performFullEvaluation(
            String outPath, 
            String expname, 
            ArrayList<Pair<String,Function<ClassifierResults,Double>>> statistics, 
            ArrayList<ClassifierEvaluation> results, 
            String[] dsets, 
            Map<String, Map<String, String[]>> dsetGroupings) 
    {
        //hacky housekeeping
        MultipleClassifiersPairwiseTest.beQuiet = true;
        OneSampleTests.beQuiet = true;
        
        outPath = outPath.replace("\\", "/"); 
        if (!outPath.endsWith("/"))
            outPath+="/";
        outPath += expname + "/";
        new File(outPath).mkdirs();
        
        expRootDirectory = outPath;
        
        OutFile bigSummary = new OutFile(outPath + expname + "_BIGglobalSummary.csv");
        OutFile smallSummary = new OutFile(outPath + expname + "_SMALLglobalSummary.csv");
        
        String[] cnames = getNames(results);
        ArrayList<String> statCliquesForCDDias = new ArrayList<>();
        ArrayList<String> statNamesForDias = new ArrayList<>();
        
        // START USER DEFINED STATS
        for (Pair<String, Function<ClassifierResults, Double>> stat : statistics) {
            
            String[] summary = null;
            try { 
                summary = eval_metric(outPath, expname, results, stat, cnames, dsets, dsetGroupings);
            } catch (FileNotFoundException fnf) {
                System.out.println("Something went wrong while writing " + stat + "files, likely later stages of analysis could "
                        + "not find files that should have been made "
                        + "internally in earlier stages of the pipeline, FATAL");
                fnf.printStackTrace();
                System.exit(0);
            }
            
            bigSummary.writeString(stat.var1+":");
            bigSummary.writeLine(summary[0]);
            
            smallSummary.writeString(stat.var1+":");
            smallSummary.writeLine(summary[1]);
            
            if (summary[2] != null) {
                statNamesForDias.add(stat.var1);
                statCliquesForCDDias.add(summary[2]);
            }
        }
        // END USER DEFINED STATS
        
        // START TIMINGS 
        String[][] trainTestTimingSummary = null;
        try { 
            trainTestTimingSummary = eval_timings(outPath, expname, results, cnames, dsets, null); //dont bother with groupings for timings
        } catch (FileNotFoundException fnf) {
            System.out.println("Something went wrong while writing timing files, likely "
                    + "later stages of analysis could not find files that should have been made"
                    + "internally in earlier stages of the pipeline, FATAL");
            fnf.printStackTrace();
            System.exit(0);
        }
        
        String[] timeLabels = { "TRAINTIMES", "TESTTIMES" };
        for (int j = 0; j < timeLabels.length; j++) {
            if (trainTestTimingSummary[0] != null) {
                bigSummary.writeString(timeLabels[j] + ":");
                bigSummary.writeLine(trainTestTimingSummary[j][0]);

                smallSummary.writeString(timeLabels[j] + ":");
                smallSummary.writeLine(trainTestTimingSummary[j][1]);

                statNamesForDias.add(timeLabels[j] + ":");
                statCliquesForCDDias.add(trainTestTimingSummary[j][2]);
            } 
            else {
                bigSummary.writeString(timeLabels[j] + ":  MISSING\n\n");
                smallSummary.writeString(timeLabels[j] + ": MISSING\n\n");
            }
        }
        //END TIMINGS
        
        
        bigSummary.closeFile();
        smallSummary.closeFile();
        
        jxl_buildResultsSpreadsheet(outPath, expname, statistics);
     
        String[] statNamesForDiasArr = statNamesForDias.toArray(new String[] { });
        String[] statCliquesForCDDiasArr = statCliquesForCDDias.toArray(new String[] { });
        
        if(buildMatlabDiagrams) {
            MatlabController proxy = MatlabController.getInstance();
            proxy.eval("addpath(genpath('"+matlabFilePath+"'))");
            matlab_buildCDDias(expname, statNamesForDiasArr, statCliquesForCDDiasArr);
            matlab_buildPairwiseScatterDiagrams(outPath, expname, statNamesForDiasArr, dsets);
        }
    }
    
    /**
     * Essentially just a wrapper for what eval_metricOnSplit does, in the simple case that we just have a 3d array of test accs and want summaries for it
 Mostly for legacy results not in the classifier results file format 
     */
    public static void performTestAccEvalOnly(String outPath, String filename, double[][][] testFolds, String[] cnames, String[] dsets, Map<String, Map<String, String[]>> dsetGroupings) throws FileNotFoundException {
        eval_metricOnSplit(outPath, filename, null, testLabel, "ACC", testFolds, cnames, dsets, dsetGroupings);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    protected static void writeTableFile(String filename, String tableName, double[][] accs, String[] cnames, String[] dsets) {
        OutFile out=new OutFile(filename);
        out.writeLine(tableName + ":" + fileHelper_tabulate(accs, cnames, dsets));
//        out.writeLine("\navg:" + util_mean(accs));
        out.closeFile();
    }
    
    protected static void writeTableFileRaw(String filename, double[][] accs, String[] cnames) {
        OutFile out=new OutFile(filename);
        out.writeLine(fileHelper_tabulateRaw(accs, cnames));
        out.closeFile();
    }
    
    /**
     * also writes separate win/draw/loss files now
     */
    protected static String[] eval_metricOnSplitStatsFile(String outPath, String filename, String statName, double[][][] statPerFold, double[][] statPerDset, double[][] ranks, double[][] stddevsFoldAccs, String[] cnames, String[] dsets) {   
        StringBuilder shortSummaryStats = new StringBuilder();
        shortSummaryStats.append(fileHelper_header(cnames)).append("\n");
        shortSummaryStats.append("Avg"+statName+":").append(util_mean(statPerDset)).append("\n");
        shortSummaryStats.append("Avg"+statName+"_RANK:").append(util_mean(ranks)).append("\n");
        
        StringBuilder longSummaryStats = new StringBuilder();
        
        longSummaryStats.append(statName).append(fileHelper_header(cnames)).append("\n");
        longSummaryStats.append("Avg"+statName+"OverDsets:").append(util_mean(statPerDset)).append("\n");
        longSummaryStats.append("Avg"+statName+"RankOverDsets:").append(util_mean(ranks)).append("\n");
        longSummaryStats.append("StddevOf"+statName+"OverDsets:").append(util_stddev(statPerDset)).append("\n");
        longSummaryStats.append("AvgOfStddevsOf"+statName+"OverDsetFolds:").append(util_mean(stddevsFoldAccs)).append("\n");
        longSummaryStats.append("StddevsOf"+statName+"RanksOverDsets:").append(util_stddev(ranks)).append("\n");

        String[] wdl =      eval_winsDrawsLosses(statPerDset, cnames, dsets);
        String[] sig01wdl = eval_sigWinsDrawsLosses(0.01, statPerDset, statPerFold, cnames, dsets);
        String[] sig05wdl = eval_sigWinsDrawsLosses(0.05, statPerDset, statPerFold, cnames, dsets);
        
        (new File(outPath+"/WinsDrawsLosses/")).mkdir();
        OutFile outwdl = new OutFile(outPath+"/WinsDrawsLosses/" + filename + "_listWDLFLAT_"+statName+".csv");
        outwdl.writeLine(wdl[1]);
        outwdl.closeFile();
        outwdl = new OutFile(outPath+"/WinsDrawsLosses/" + filename + "_listWDLSig01_"+statName+".csv");
        outwdl.writeLine(sig01wdl[1]);
        outwdl.closeFile();
        outwdl = new OutFile(outPath+"/WinsDrawsLosses/" + filename + "_listWDLSig05_"+statName+".csv");
        outwdl.writeLine(sig05wdl[1]);
        outwdl.closeFile();
        
        outwdl = new OutFile(outPath+"/WinsDrawsLosses/" + filename + "_tableWDLFLAT_"+statName+".csv");
        outwdl.writeLine(wdl[2]);
        outwdl.closeFile();
        outwdl = new OutFile(outPath+"/WinsDrawsLosses/" + filename + "_tableWDLSig01_"+statName+".csv");
        outwdl.writeLine(sig01wdl[2]);
        outwdl.closeFile();
        outwdl = new OutFile(outPath+"/WinsDrawsLosses/" + filename + "_tableWDLSig05_"+statName+".csv");
        outwdl.writeLine(sig05wdl[2]);
        outwdl.closeFile();
        
        OutFile out=new OutFile(outPath+filename+"_"+statName+"_SUMMARY.csv");
        
        out.writeLine(longSummaryStats.toString());
        
        out.writeLine(wdl[0]);
        out.writeLine("\n");
        out.writeLine(sig01wdl[0]);
        out.writeLine("\n");
        out.writeLine(sig05wdl[0]);
        out.writeLine("\n");
        
        String cliques = "";
        try {
            out.writeLine(MultipleClassifiersPairwiseTest.runTests(outPath+filename+"_"+statName+".csv").toString());       
            cliques = MultipleClassifiersPairwiseTest.printCliques();
            out.writeLine("\n\n" + cliques);
        } catch (Exception e) {
            System.err.println("\n\n");
            System.err.println("*****".replace("*", "*****"));
            
            System.err.println("MultipleClassifiersPairwiseTest.runTests() failed. Almost certainly this is because there were"
                    + "too many ties/duplicates within one of the pairwise tests and then an index out of bounds error was thrown. "
                    + "This will be fixed at some point. The analysis will CARRY ON, and everything that is successfully printed out "
                    + "IS CORRECT, however whatever particular table that test would have been summarised as is missing from your files.");
            System.err.println("filename="+outPath+filename+"_"+statName+".csv");
            e.printStackTrace();
            
            System.err.println("*****".replace("*", "*****"));
            System.err.println("\n\n");
        }
        
        out.closeFile();
        
        return new String[] { longSummaryStats.toString(), shortSummaryStats.toString(), cliques };
    }
    
    protected static String fileNameBuild_cd(String filename, String statistic) {
        return "cd_"+filename+"_"+statistic+"S";
    }
    
    protected static String fileNameBuild_pws(String filename, String statistic) {
        return "pws_"+filename+"_"+statistic+"S";
    }
    
    protected static String fileNameBuild_pwsInd(String c1, String c2, String statistic) {
        return "pws_"+c1+"VS"+c2+"_"+statistic+"S";
    }
    
    protected static String[] eval_metricOnSplit(String outPath, String filename, String groupingName, String evalSet, String statName, double[][][] foldVals, String[] cnames, String[] dsets, Map<String, Map<String, String[]>> dsetGroupings) throws FileNotFoundException {
        outPath += evalSet + "/";
        if (groupingName != null && !groupingName.equals(""))
            outPath += groupingName + "/";
        
        //BEFORE ordering, write the individual folds files
        eval_perFoldFiles(outPath+evalSet+"FOLD"+statName+"S/", foldVals, cnames, dsets, evalSet);
        
        double[][] dsetVals = findAvgsOverFolds(foldVals);
        double[][] stddevsFoldVals = findStddevsOverFolds(foldVals);
        double[][] ranks = null;
        
        //todo, refactor such that the stat, statname pair becomes a triple that contains 
        //a comparator too. will be easier if/when the metrics are separated out of 
        //classifierreuslts into their own package
        if (       statName.toLowerCase().contains("nll") 
                || statName.toLowerCase().contains("timings") 
                || statName.toLowerCase().contains("time")
                ) {
            ranks = findRanks(dsetVals, false); //we want to minimise
        }
        else {
            ranks = findRanks(dsetVals, true); //we want to maximise
        }
        
        int[] ordering = ordering = findOrdering(ranks); 
        //ordering is now an array of value referring to the rank-order of the element at each index
        //e.g [1, 4, 2, 3] means that the first (in index 0) classifier is best, third is next, then fourth, then second
        
        //now order all the info (essentially in parallel arrays) we've collected by the classifier's ranks
        //such that e.g the data referring to the first classifier is still in index 0, the data referring to
        //the second classifier is moved to index 1, etc
        ranks = util_order(ranks, ordering);
        cnames = util_order(cnames, ordering);
        foldVals = util_order(foldVals, ordering);
        dsetVals = util_order(dsetVals, ordering);
        stddevsFoldVals = util_order(stddevsFoldVals, ordering);
        
        if (performDeepAnalysis) {
            if (evalSet.equalsIgnoreCase("TEST")) {
                //qol for cd dia creation, make a copy of all the raw test stat files in a common folder, one for pairwise, one for freidman
                String cdFolder = expRootDirectory + cdDiaPath;
                (new File(cdFolder)).mkdirs();
                OutFile out = new OutFile(cdFolder+"readme.txt");
                out.writeLine("remember that nlls are auto-negated now for cd dia ordering\n");
                out.writeLine("and that basic notepad wont show the line breaks properly, view (cliques especially) in notepad++");
                out.closeFile();
                for (String subFolder : new String[] { pairwiseCDDiaDirectoryName, friedmanCDDiaDirectoryName }) {
                    (new File(cdFolder+subFolder+"/")).mkdirs();
                    String cdName = cdFolder+subFolder+"/"+fileNameBuild_cd(filename,statName)+".csv";
                    
                    //meta hack for qol, negate the nll (sigh...) for correct ordering on dia
                    //ALSO now negating the timings, smaller = better
                    if (statName.toLowerCase().contains("nll") 
                            || statName.toLowerCase().contains("buildtime") 
                            || statName.toLowerCase().contains("testtime")) {
                        
                        double[][] negatedDsetVals = new double[dsetVals.length][dsetVals[0].length];
                        for (int i = 0; i < dsetVals.length; i++) {
                            for (int j = 0; j < dsetVals[i].length; j++) {
                                negatedDsetVals[i][j] = dsetVals[i][j] * -1;
                            }
                        }
                        writeTableFileRaw(cdName, negatedDsetVals, cnames);
                    } else {
                        writeTableFileRaw(cdName, dsetVals, cnames);
                    } 
                } //end cd dia qol

                //qol for pairwisescatter dia creation, make a copy of the test stat files 
                String pwsFolder = expRootDirectory + pairwiseScatterDiaPath;
                (new File(pwsFolder)).mkdirs();
                String pwsName = pwsFolder+fileNameBuild_pws(filename,statName)+".csv";
                writeTableFileRaw(pwsName, dsetVals, cnames);
                //end pairwisescatter qol
            }
        }
        
        writeTableFile(outPath+filename+"_"+evalSet+statName+"RANKS.csv", evalSet+statName+"RANKS", ranks, cnames, dsets);
        writeTableFile(outPath+filename+"_"+evalSet+statName+".csv", evalSet+statName, dsetVals, cnames, dsets);
        writeTableFileRaw(outPath+filename+"_"+evalSet+statName+"RAW.csv", dsetVals, cnames); //for matlab stuff
        writeTableFile(outPath+filename+"_"+evalSet+statName+"STDDEVS.csv", evalSet+statName+"STDDEVS", stddevsFoldVals, cnames, dsets);
        
        String[] groupingSummary = { "" };
        if (performDeepAnalysis)
            if (dsetGroupings != null && dsetGroupings.size() != 0)
                groupingSummary = eval_metricDsetGroups(outPath, filename, evalSet, statName, foldVals, cnames, dsets, dsetGroupings);
        
        
        String[] summaryStrings = {};
        if (performDeepAnalysis) {
            summaryStrings = eval_metricOnSplitStatsFile(outPath, filename, evalSet+statName, foldVals, dsetVals, ranks, stddevsFoldVals, cnames, dsets); 

            //write these even if not actually making the dias this execution
            writeCliqueHelperFiles(expRootDirectory + cdDiaPath + pairwiseCDDiaDirectoryName, filename, statName, summaryStrings[2]); 
        }
        
        //this really needs cleaning up at some point... jsut make it a list and stop circlejerking to arrays
        String[] summaryStrings2 = new String[summaryStrings.length+groupingSummary.length];
        int i = 0;
        for ( ; i < summaryStrings.length; i++) 
            summaryStrings2[i] = summaryStrings[i];
        for (int j = 0; j < groupingSummary.length; j++) 
            summaryStrings2[i] = groupingSummary[j];
               
        return summaryStrings2;
    }
    
    public static String[] eval_metricDsetGroups(String outPathBase, String filename, String evalSet, String statName, double[][][] foldVals, String[] cnames, String[] dsets, Map<String, Map<String, String[]>> dsetGroupings) throws FileNotFoundException {
        String outPath = expRootDirectory + "DatasetGroupings/";
//        String outPath = outPathBase + "DatasetGroupings/";
        (new File(outPath)).mkdir();
        
        //for each grouping method 
        for (Map.Entry<String, Map<String, String[]>> dsetGroupingMethodEntry : dsetGroupings.entrySet()) {
            String groupingMethodName = dsetGroupingMethodEntry.getKey();
            String groupingMethodPath = outPath + groupingMethodName + "/";
            (new File(groupingMethodPath+statName+"/"+evalSet+"/")).mkdirs();
            
            Map<String, String[]> dsetGroupingMethod = dsetGroupingMethodEntry.getValue();
            
            if (groupingMethodName.equals(clusterGroupingIdentifier)) {
                //if clustering is to be done, build the groups now.
                //can't 'put' these groups back into the dsetGroupings map
                //since we'd be editing a map that we're currently iterating over
                //EDIT: actually, jsut move this process outside the for loop as
                //a preprocess step, if the need ever arises 
                
                assert(dsetGroupingMethod == null);
                dsetGroupingMethod = new HashMap<>();
                
                int[] assignments = dsetGroups_clusterDsetResults(StatisticalUtilities.averageFinalDimension(foldVals));
                
                //puts numClusters as final element
                assert(assignments.length == dsets.length+1);
                int numClusters = assignments[dsets.length];
                
                String[] clusterNames = new String[numClusters];
                String[][] clusterDsets = new String[numClusters][];
                
                //would generally prefer to jsut loop once over the assignments array, but that would
                //require we already know the size of each cluster and/or wankery with array lists
                for (int cluster = 0; cluster < numClusters; cluster++) {
                    ArrayList<String> dsetAlist = new ArrayList<>();
                    for (int dset = 0; dset < dsets.length; dset++)
                        if (assignments[dset] == cluster)
                            dsetAlist.add(dsets[dset]); 
                    
                    clusterNames[cluster] = "Cluster " + (cluster+1);
                    clusterDsets[cluster] = dsetAlist.toArray(new String[] { });
                    dsetGroupingMethod.put(clusterNames[cluster], clusterDsets[cluster]);
                }
            
                //writing all the clusters to one file start here
                OutFile allDsetsOut = new OutFile(groupingMethodPath+statName+"/"+evalSet+"/" + "clusters.csv");
                
                for (int cluster = 0; cluster < numClusters; cluster++)
                    allDsetsOut.writeString(clusterNames[cluster] + ",");
                allDsetsOut.writeLine("");
                
                //printing variable length 2d array in table form, columns = clusters, rows = dsets
                int dsetInd = 0;
                boolean allDone = false;
                while (!allDone) {
                    allDone = true;
                    for (int cluster = 0; cluster < numClusters; cluster++) {
                        if (dsetInd < clusterDsets[cluster].length) {
                            allDsetsOut.writeString(clusterDsets[cluster][dsetInd]);
                            allDone = false;
                        }
                        allDsetsOut.writeString(",");
                    }
                    allDsetsOut.writeLine("");
                    dsetInd++;
                }
                allDsetsOut.closeFile();
                //writing all the clusters to one file end here
            
                String clusterGroupsPath = groupingMethodPath+statName+"/"+evalSet+"/" + "DsetClustersTxtFiles/";
                (new File(clusterGroupsPath)).mkdir();
            
                //writing each individual clsuter file start here
                for (int cluster = 0; cluster < numClusters; cluster++) {
                    OutFile clusterFile = new OutFile(clusterGroupsPath + clusterNames[cluster] + ".txt");
                    for (String dset : clusterDsets[cluster])
                        clusterFile.writeLine(dset);
                    clusterFile.closeFile();
                }
            }
            
            int numGroups = dsetGroupingMethod.size();
            String[] groupNames = new String[numGroups];
            
            //using maps for this because classifiernames could be in different ordering based on rankings 
            //within each group. ordering of dataset groups temselves is constant though. jsut skips 
            //annoying/bug inducing housekeeping of indices
            Map<String, double[]> groupWins = new HashMap<>(); //will reflect ties, e.g if 2 classifiers tie for first rank, each will get 'half' a win
            Map<String, double[]> groupAccs = new HashMap<>();
            for (int i = 0; i < cnames.length; i++) {
                groupWins.put(cnames[i], new double[numGroups]);
                groupAccs.put(cnames[i], new double[numGroups]);
            }
            
            //for each group in this grouping method 
            StringBuilder [] groupSummaryStringBuilders = new StringBuilder[numGroups];
            int groupIndex = 0;
            
            for (Map.Entry<String, String[]> dsetGroup : dsetGroupingMethod.entrySet()) {
                String groupName = dsetGroup.getKey();
                groupNames[groupIndex] = groupName;
//                String groupPath = groupingMethodPath + groupName + "/";
//                (new File(groupPath)).mkdir();
                
                //perform group analysis
                String[] groupDsets = dsetGroup.getValue();
                double[][][] groupFoldVals = dsetGroups_collectDsetVals(foldVals, dsets, groupDsets);
                String groupFileName = filename + "-" + groupName + "-";
//                String[] groupSummaryFileStrings = eval_metricOnSplit(groupPath+statName+"/", groupFileName, groupName, evalSet, statName, groupFoldVals, cnames, groupDsets, null);
                String[] groupSummaryFileStrings = eval_metricOnSplit(groupingMethodPath+statName+"/", groupFileName, groupName, evalSet, statName, groupFoldVals, cnames, groupDsets, null);
                
                //collect the accuracies for the dataset group 
                String[] classifierNamesLine = groupSummaryFileStrings[1].split("\n")[0].split(",");
                assert(classifierNamesLine.length-1 == cnames.length);
                String[] accLineParts = groupSummaryFileStrings[1].split("\n")[1].split(",");
                for (int i = 1; i < accLineParts.length; i++) { //i=1 => skip the row fileHelper_header
                    double[] accs = groupAccs.get(classifierNamesLine[i]);
                    accs[groupIndex] = Double.parseDouble(accLineParts[i]);
                    groupAccs.put(classifierNamesLine[i], accs);
                }
                
                //collect the wins for the group
                Scanner ranksFileIn = new Scanner(new File(groupingMethodPath+statName+"/"+evalSet+"/"+groupName+"/"+groupFileName+"_"+evalSet+statName+"RANKS.csv"));      
                classifierNamesLine = ranksFileIn.nextLine().split(",");
                double[] winCounts = new double[classifierNamesLine.length];
                while (ranksFileIn.hasNextLine()) {
                    //read the ranks on this dataset
                    String[] ranksStr = ranksFileIn.nextLine().split(",");
                    double[] ranks = new double[ranksStr.length];
                    ranks[0] = Double.MAX_VALUE;
                    for (int i = 1; i < ranks.length; i++)
                        ranks[i] = Double.parseDouble(ranksStr[i]);
                    
                    //there might be ties, so cant just look for the rank "1"
                    List<Integer> minRanks = util_min(ranks);
                    for (Integer minRank : minRanks)
                        winCounts[minRank] += 1.0 / minRanks.size();
                }
                ranksFileIn.close();
                
                for (int i = 1; i < winCounts.length; i++) {
                    double[] wins = groupWins.get(classifierNamesLine[i]);
                    wins[groupIndex] = winCounts[i];
                    groupWins.put(classifierNamesLine[i], wins);
                }
                
                //build the summary string
                StringBuilder sb = new StringBuilder("Group: " +groupName + "\n");
                sb.append(groupSummaryFileStrings[1]); 
                
                //when will the hacks ever end? 
                String cliques = groupSummaryFileStrings[2];
                cliques = cliques.replace("cliques = [", "cliques=,").replace("]", ""); //remove spaces in 'title' before next step
                cliques = cliques.replace(" ", ",").replace("\n", "\n,"); //make vals comma separated, to line up in csv file
                sb.append("\n"+cliques);
                
                groupSummaryStringBuilders[groupIndex] = sb;
                groupIndex++;
            }
            
            String groupMethodSummaryFilename = groupingMethodPath + filename + "_" + groupingMethodName + "_" + evalSet + statName + ".csv";
            dsetGroups_writeGroupingMethodSummaryFile(groupMethodSummaryFilename, groupSummaryStringBuilders, cnames, groupNames, groupWins, groupAccs);
        }
        
        return new String[] { };
    }
    
    public static void dsetGroups_writeGroupingMethodSummaryFile(String filename, StringBuilder [] groupSummaryStringBuilders, String[] cnames, String[] groupNames, 
            Map<String, double[]> groupWins, Map<String, double[]> groupAccs) {
        
        OutFile groupingMethodSummaryFile = new OutFile(filename);
        for (StringBuilder groupSummary : groupSummaryStringBuilders) {
            groupingMethodSummaryFile.writeLine(groupSummary.toString());
            groupingMethodSummaryFile.writeLine("\n\n");
        }

        groupingMethodSummaryFile.writeString(dsetGroups_buildAccsTableString(groupAccs, cnames, groupNames));
        groupingMethodSummaryFile.writeLine("\n\n");
        groupingMethodSummaryFile.writeString(dsetGroups_buildWinsTableString(groupWins, cnames, groupNames));

        groupingMethodSummaryFile.closeFile();
    }
    
    public static String dsetGroups_buildWinsTableString(Map<String, double[]> groupWins, String[] cnames, String[] groupNames) {
        int numGroups = groupNames.length;
        StringBuilder sb = new StringBuilder();

        sb.append("This table accounts for ties on a dset e.g if 2 classifiers share best accuracy "
        + "that will count as half a win for each").append("\n");

        //header row
        sb.append("NumWinsInGroups:");
        for (String cname : cnames) 
            sb.append(","+cname);
        sb.append(",TotalNumDsetsInGroup").append("\n");

        //calc the avgs too
        double[] groupSums = new double[numGroups], clsfrSums = new double[cnames.length];
        for (int i = 0; i < numGroups; i++) {
            sb.append(groupNames[i]);
            for (int j = 0; j < cnames.length; j++) {
                double val = groupWins.get(cnames[j])[i];
                groupSums[i] += val;
                clsfrSums[j] += val;
                sb.append(","+val);
            }
            sb.append(","+(groupSums[i])).append("\n");
        }

        //print final row, avg of classifiers
        double globalSum = 0;
        sb.append("TotalNumWinsForClassifier");
        for (int j = 0; j < cnames.length; j++) {
            globalSum += clsfrSums[j];
            sb.append(","+clsfrSums[j]);
        }

        sb.append(","+globalSum).append("\n");
        
        return sb.toString();
    }
    
    public static String dsetGroups_buildAccsTableString(Map<String, double[]> groupAccs, String[] cnames, String[] groupNames) {
        int numGroups = groupNames.length;
        StringBuilder sb = new StringBuilder();

        //header row
        sb.append("AvgAccsOnGroups:");
        for (String cname : cnames) 
            sb.append(","+cname);
        sb.append(",Averages").append("\n");

        //calc the avgs too
        double[] groupAvgs = new double[numGroups], clsfrAvgs = new double[cnames.length];
        for (int i = 0; i < numGroups; i++) {
            sb.append(groupNames[i]);
            for (int j = 0; j < cnames.length; j++) {
                double val = groupAccs.get(cnames[j])[i];
                groupAvgs[i] += val;
                clsfrAvgs[j] += val;
                sb.append(","+val);
            }
            sb.append(","+(groupAvgs[i]/cnames.length)).append("\n");
        }

        //print final row, avg of classifiers
        double globalAvg = 0;
        sb.append("Averages");
        for (int j = 0; j < cnames.length; j++) {
            double avg = clsfrAvgs[j]/numGroups;
            globalAvg += avg;
            sb.append(","+avg);
        }
        globalAvg /= cnames.length;
        sb.append(","+globalAvg).append("\n");
        
        return sb.toString();
    }
    
    public static double[][][] dsetGroups_collectDsetVals(double[][][] foldVals, String[] dsets, String[] groupDsets) {
        //cloning arrays to avoid any potential referencing issues considering we're recursing + doing more stuff after all this grouping shite
        double[][][] groupFoldVals = new double[foldVals.length][groupDsets.length][foldVals[0][0].length];
        
        for (int groupDsetInd = 0; groupDsetInd < groupDsets.length; ++groupDsetInd) {
            String dset = groupDsets[groupDsetInd];
            int globalDsetInd = Arrays.asList(dsets).indexOf(dset);
            
            for (int classifier = 0; classifier < foldVals.length; classifier++) {
                for (int fold = 0; fold < foldVals[classifier][globalDsetInd].length; fold++) {
                    groupFoldVals[classifier][groupDsetInd][fold] = foldVals[classifier][globalDsetInd][fold];
                }
            }
        }
        
        return groupFoldVals;
    }

    
    protected static String[] eval_metric(String outPath, String filename, ArrayList<ClassifierEvaluation> results, Pair<String, Function<ClassifierResults, Double>> evalStatistic, String[] cnames, String[] dsets, Map<String, Map<String, String[]>> dsetGroupings) throws FileNotFoundException {
        String statName = evalStatistic.var1;
        outPath += statName + "/";
        new File(outPath).mkdirs();        
        
        double[][][] testFolds = getInfo(results, evalStatistic.var2, testLabel);
        
        if (!testResultsOnly) {
            double[][][] trainFolds = getInfo(results, evalStatistic.var2, trainLabel);
            double[][][] trainTestDiffsFolds = findTrainTestDiffs(trainFolds, testFolds);
            eval_metricOnSplit(outPath, filename, null, trainLabel, statName, trainFolds, cnames, dsets, dsetGroupings); 
            eval_metricOnSplit(outPath, filename, null, trainTestDiffLabel, statName, trainTestDiffsFolds, cnames, dsets, dsetGroupings);
        }
        
        return eval_metricOnSplit(outPath, filename, null, testLabel, statName, testFolds, cnames, dsets, dsetGroupings);
    }

    protected static String[/*{train,test}*/][] eval_timings(String outPath, String filename, ArrayList<ClassifierEvaluation> results, String[] cnames, String[] dsets, Map<String, Map<String, String[]>> dsetGroupings) throws FileNotFoundException {
        if (results.get(0).testResults[0][0].getBuildTime() <= 0) { //is not present. TODO god forbid naive bayes on balloons takes less than a millisecond...
            System.out.println("Warning: No buildTimes found, or buildtimes == 0");
            return null;
        }
        
        //future proofing the spaghetti, if dostats was already false becuase of something else
        //we can leave it as it was.
        boolean prevStateOfDoStats = performDeepAnalysis;
//        performDeepAnalysis = false; 
        //since timings are now handled semi-reasonably, there shouldnt be too many problems
        //with perform pairwise tests and making dias of timings now. 
        
        Function<ClassifierResults, Double> stat = ClassifierResults.GETTER_buildTimeDoubleMillis;
        String statName = "Timings";
        outPath += statName + "/";
        new File(outPath).mkdirs();        
        
        double[][][] trainTimes = getTimingsIfAllArePresent(results, ClassifierResults.GETTER_buildTimeDoubleMillis);
        String[] trainResStr = null;
        if (trainTimes != null)
            trainResStr = eval_metricOnSplit(outPath, filename, null, trainLabel, statName, trainTimes, cnames, dsets, dsetGroupings); 
           
        double[][][] testTimes = getTimingsIfAllArePresent(results, ClassifierResults.GETTER_testTimeDoubleMillis);
        String[] testResStr = null;
        if (testTimes != null)
            testResStr = eval_metricOnSplit(outPath, filename, null, testLabel, statName, testTimes, cnames, dsets, dsetGroupings);
  
        performDeepAnalysis = prevStateOfDoStats;
        return new String[][] { trainResStr, testResStr };
    }

    protected static void writeCliqueHelperFiles(String cdCSVpath, String expname, String stat, String cliques) {
        (new File(cdCSVpath)).mkdirs();
        
        //temp workaround, just write the cliques and readin again from matlab for ease of checking/editing for pairwise edge cases
        OutFile out = new OutFile (cdCSVpath + fileNameBuild_cd(expname, stat) + "_cliques.txt");
        out.writeString(cliques);
        out.closeFile();
    }
    
    protected static void matlab_buildCDDias(String expname, String[] stats, String[] cliques) {        
        MatlabController proxy = MatlabController.getInstance();
        proxy.eval("buildDiasInDirectory('"+expRootDirectory+"/cdDias/"+friedmanCDDiaDirectoryName+"', 0, "+FRIEDMANCDDIA_PVAL+")"); //friedman 
        proxy.eval("clear");
        proxy.eval("buildDiasInDirectory('"+expRootDirectory+"/cdDias/"+pairwiseCDDiaDirectoryName+"', 1)");  //pairwise
        proxy.eval("clear"); 
    }
        
    protected static void eval_perFoldFiles(String outPath, double[][][] folds, String[] cnames, String[] dsets, String splitLabel) {
        new File(outPath).mkdirs();
        
        StringBuilder headers = new StringBuilder("folds:");
        for (int f = 0; f < folds[0][0].length; f++)
            headers.append(","+f);
        
        for (int c = 0; c < folds.length; c++) {
            OutFile out=new OutFile(outPath + cnames[c]+"_"+splitLabel+"FOLDS.csv");
            out.writeLine(headers.toString());
            
            for (int d = 0; d < folds[c].length; d++) {
                out.writeString(dsets[d]);
                for (int f = 0; f < folds[c][d].length; f++)
                    out.writeString("," + folds[c][d][f]);
                out.writeLine("");
            }
            
            out.closeFile();
        }
        
        
        OutFile out = new OutFile(outPath + "TEXASPLOT_"+splitLabel+".csv");
        out.writeString(cnames[0]);
        for (int c = 1; c < cnames.length; c++)
            out.writeString("," + cnames[c]);
        out.writeLine("");
        
        for (int d = 0; d < dsets.length; d++) {
            for (int f = 0; f < folds[0][0].length; f++) {
                out.writeDouble(folds[0][d][f]);
                for (int c = 1; c < cnames.length; c++)
                    out.writeString("," + folds[c][d][f]);
                out.writeLine("");
            }
        }
        out.closeFile();
    }
    
    protected static String fileHelper_tabulate(double[][] res, String[] cnames, String[] dsets) {
        StringBuilder sb = new StringBuilder();
        sb.append(fileHelper_header(cnames));
        
        for (int i = 0; i < res[0].length; ++i) {
            sb.append("\n").append(dsets[i]);

            for (int j = 0; j < res.length; j++)
                sb.append("," + res[j][i]);
        }      
        return sb.toString();
    }
    
    protected static String fileHelper_tabulateRaw(double[][] res, String[] cnames) {
        StringBuilder sb = new StringBuilder();
        sb.append(fileHelper_header(cnames).substring(1));
        
        for (int i = 0; i < res[0].length; ++i) {
            sb.append("\n").append(res[0][i]);
            for (int j = 1; j < res.length; j++)
                sb.append("," + res[j][i]);
        }      
        return sb.toString();
    }
    
    protected static String fileHelper_header(String[] names) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < names.length; i++)
            sb.append(",").append(names[i]);
        return sb.toString();
    }
    
    protected static String util_mean(double[][] res) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < res.length; i++) 
            sb.append(",").append(StatisticalUtilities.mean(res[i], false));
        
        return sb.toString();
    }
    
    protected static String util_stddev(double[][] res) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < res.length; i++) 
            sb.append(",").append(StatisticalUtilities.standardDeviation(res[i], false, StatisticalUtilities.mean(res[i], false)));
        
        return sb.toString();
    }
    
    protected static double[][][] findTrainTestDiffs(double[][][] trainFoldAccs, double[][][] testFoldAccs) {
        double[][][] diffs = new double[trainFoldAccs.length][trainFoldAccs[0].length][trainFoldAccs[0][0].length];
        
        for (int c = 0; c < diffs.length; c++) 
            for (int d = 0; d < diffs[c].length; d++) 
                for (int f = 0; f < diffs[c][d].length; f++) 
                    diffs[c][d][f] =  trainFoldAccs[c][d][f] - testFoldAccs[c][d][f];
        
        return diffs;
    }
        
    protected static double[][] findAvgsOverFolds(double[][][] foldaccs) {
        double[][] accs = new double[foldaccs.length][foldaccs[0].length];
        for (int i = 0; i < accs.length; i++)
            for (int j = 0; j < accs[i].length; j++)
                accs[i][j] = StatisticalUtilities.mean(foldaccs[i][j], false);
        
        return accs;
    }
    
    protected static double[][] findStddevsOverFolds(double[][][] foldaccs) {
        double[][] devs = new double[foldaccs.length][foldaccs[0].length];
        for (int i = 0; i < devs.length; i++)
            for (int j = 0; j < devs[i].length; j++)
                devs[i][j] = StatisticalUtilities.standardDeviation(foldaccs[i][j], false, StatisticalUtilities.mean(foldaccs[i][j], false));
        
        return devs;
    }
    
    protected static int[] findOrdering(double[][] r) {
        double[] avgranks = new double[r.length];
        for (int i = 0; i < r.length; i++) 
            avgranks[i] = StatisticalUtilities.mean(r[i], false);
        
        int[] res = new int[avgranks.length];
        
        int i = 0;
        while (i < res.length) {
            ArrayList<Integer> mins = util_min(avgranks);
            
            for (int j = 0; j < mins.size(); j++) {
                res[mins.get(j)] = i++;
                avgranks[mins.get(j)] = Double.MAX_VALUE;
            }
        }
        
        return res;
    }
    
    protected static int[] findReverseOrdering(double[][] r) {
        double[] avgranks = new double[r.length];
        for (int i = 0; i < r.length; i++) 
            avgranks[i] = StatisticalUtilities.mean(r[i], false);
        
        int[] res = new int[avgranks.length];
        
        int i = 0;
        while (i < res.length) {
            ArrayList<Integer> maxs = util_max(avgranks);
            
            for (int j = 0; j < maxs.size(); j++) {
                res[maxs.get(j)] = i++;
                avgranks[maxs.get(j)] = -Double.MAX_VALUE;
            }
        }
        
        return res;
    }
    
    protected static ArrayList<Integer> util_min(double[] d) {
        double min = d.length+1;
        ArrayList<Integer> minIndices = null;
        
        for (int c = 0; c < d.length; c++) {
            if(d[c] < min){
                min = d[c];
                minIndices = new ArrayList<>();
                minIndices.add(c);
            }else if(d[c] == min){
                minIndices.add(c);
            }
        }
        
        return minIndices;
    }
    
    protected static ArrayList<Integer> util_max(double[] d) {
        double max = -1;
        ArrayList<Integer> maxIndices = null;
        
        for (int c = 0; c < d.length; c++) {
            if(d[c] > max){
                max = d[c];
                maxIndices = new ArrayList<>();
                maxIndices.add(c);
            }else if(d[c] == max){
                maxIndices.add(c);
            }
        }
        
        return maxIndices;
    }
    
    protected static String[] util_order(String[] s, int[] ordering) {
        String[] res = new String[s.length];
        
        for (int i = 0; i < ordering.length; i++) 
            res[ordering[i]] = s[i];
        
        return res;
    }
    
    protected static double[][] util_order(double[][] s, int[] ordering) {
        double[][] res = new double[s.length][];
        
        for (int i = 0; i < ordering.length; i++) 
            res[ordering[i]] = s[i];
        
        return res;
    }
    
    protected static double[][][] util_order(double[][][] s, int[] ordering) {
        double[][][] res = new double[s.length][][];
        
        for (int i = 0; i < ordering.length; i++) 
            res[ordering[i]] = s[i];
        
        return res;
    }
    
    /**
     * @param accs [classifiers][acc on datasets]
     * @param higherIsBetter if true, larger values will receive a better (i.e. lower) rank, false vice versa. e.g want to maximise acc, but want to minimise time
     * @return [classifiers][rank on dataset]
     */
    protected static double[][] findRanks(double[][] accs, boolean higherIsBetter) {
        double[][] ranks = new double[accs.length][accs[0].length];
        
        for (int d = 0; d < accs[0].length; d++) {
            Double[] orderedAccs = new Double[accs.length];
            for (int c = 0; c < accs.length; c++) 
                orderedAccs[c] = accs[c][d];
            
            if (higherIsBetter)
                Arrays.sort(orderedAccs, Collections.reverseOrder());
            else 
                Arrays.sort(orderedAccs);
            
//            //README - REDACTED, this problem is currently just being ignored, since it makes so many headaches and is so insignificant anyway
//            //to create parity between this and the matlab critical difference diagram code,
//            //rounding the *accuracies used to calculate ranks* to 15 digits (after the decimal) 
//            //this affects the average rank summary statistic, but not e.g the average accuracy statistic
//            //matlab has a max default precision of 16. in a tiny number of cases, there are differences 
//            //in accuracy that are smaller than this maximum precision, which were being taken into
//            //acount here (by declaring one as havign a higher rank than the other), but not being 
//            //taken into account in matlab (which considered them a tie). 
//            //one could argue the importance of a difference less than 1x10^-15 when comparing classifiers,
//            //so for ranks only, will round to matlab's precision. rounding the accuracies everywhere
//            //creates a number of headaches, therefore the tiny inconsistency as a result of this
//            //will jsut have to be lived with
//            final int DEFAULT_MATLAB_PRECISION = 15;
//            for (int c = 0; c < accs.length; c++) {
//                MathContext mc = new MathContext(DEFAULT_MATLAB_PRECISION, RoundingMode.DOWN);
//                BigDecimal bd = new BigDecimal(orderedAccs[c],mc);
//                orderedAccs[c] = bd.doubleValue();
//            }
            
            
            for (int rank = 0; rank < accs.length; rank++) {
                for (int c = 0; c < accs.length; c++) {
//                    if (orderedAccs[rank] == new BigDecimal(accs[c][d], new MathContext(DEFAULT_MATLAB_PRECISION, RoundingMode.DOWN)).doubleValue()) {
                    if (orderedAccs[rank] == accs[c][d]) {
                        ranks[c][d] = rank; //count from one
                    }
                }
            }
            
            //correcting ties
            int[] hist = new int[accs.length];
            for (int c = 0; c < accs.length; c++)
                ++hist[(int)ranks[c][d]];
            
            for (int r = 0; r < hist.length; r++) {
                if (hist[r] > 1) {//ties
                    double newRank = 0;
                    for (int i = 0; i < hist[r]; i++)
                        newRank += r-i;
                    newRank/=hist[r];
                    for (int c = 0; c < ranks.length; c++)
                        if (ranks[c][d] == r) 
                            ranks[c][d] = newRank;
                }
            }
            
            //correcting for index from 1
            for (int c = 0; c < accs.length; c++)
                ++ranks[c][d];
        }
        
        return ranks;
    }
    
    protected static String[] eval_winsDrawsLosses(double[][] accs, String[] cnames, String[] dsets) {
        StringBuilder table = new StringBuilder();
        ArrayList<ArrayList<ArrayList<String>>> wdlList = new ArrayList<>(); //[classifierPairing][win/draw/loss][dsetNames]
        ArrayList<String> wdlListNames = new ArrayList<>();
        
        String[][] wdlPlusMinus = new String[cnames.length*cnames.length][dsets.length];
        
        table.append("flat" + fileHelper_header(cnames)).append("\n");
        
        int count = 0;
        for (int c1 = 0; c1 < accs.length; c1++) {
            table.append(cnames[c1]);
            for (int c2 = 0; c2 < accs.length; c2++) {
                wdlListNames.add(cnames[c1] + "_VS_" + cnames[c2]);
                wdlList.add(new ArrayList<>());
                wdlList.get(count).add(new ArrayList<>());
                wdlList.get(count).add(new ArrayList<>());
                wdlList.get(count).add(new ArrayList<>());                
                
                int wins=0, draws=0, losses=0;
                for (int d = 0; d < dsets.length; d++) {
                    if (accs[c1][d] > accs[c2][d]) {
                        wins++;
                        wdlList.get(count).get(0).add(dsets[d]);
                        wdlPlusMinus[count][d] = "1";
                    }
                    else if ((accs[c1][d] == accs[c2][d])) {
                        draws++;
                        wdlList.get(count).get(1).add(dsets[d]);
                        wdlPlusMinus[count][d] = "0";
                    }
                    else { 
                        losses++;
                        wdlList.get(count).get(2).add(dsets[d]);
                        wdlPlusMinus[count][d] = "-1";
                    }
                }
                table.append(","+wins+"|"+draws+"|"+losses);
                count++;
            }
            table.append("\n");
        }
        
        StringBuilder list = new StringBuilder();
        for (int i = 0; i < wdlListNames.size(); ++i) {
            list.append(wdlListNames.get(i));
            list.append("\n");
            list.append("Wins("+wdlList.get(i).get(0).size()+"):");
            for (String dset : wdlList.get(i).get(0)) 
                list.append(",").append(dset);
            list.append("\n");
            list.append("Draws("+wdlList.get(i).get(1).size()+"):");
            for (String dset : wdlList.get(i).get(1)) 
                list.append(",").append(dset);
            list.append("\n");
            list.append("Losses("+wdlList.get(i).get(2).size()+"):");
            for (String dset : wdlList.get(i).get(2)) 
                list.append(",").append(dset);
            list.append("\n\n");
        }
        
        StringBuilder plusMinuses = new StringBuilder();
        for (int j = 0; j < wdlPlusMinus.length; j++) 
            plusMinuses.append(",").append(wdlListNames.get(j));
        
        for (int i = 0; i < dsets.length; i++) {
            plusMinuses.append("\n").append(dsets[i]);
            for (int j = 0; j < wdlPlusMinus.length; j++) 
                plusMinuses.append(",").append(wdlPlusMinus[j][i]);
        }
        
        return new String[] { table.toString(), list.toString(), plusMinuses.toString() };
    }
    
    protected static String[] eval_sigWinsDrawsLosses(double pval, double[][] accs, double[][][] foldAccs, String[] cnames, String[] dsets) {
        StringBuilder table = new StringBuilder();
        ArrayList<ArrayList<ArrayList<String>>> wdlList = new ArrayList<>(); //[classifierPairing][win/draw/loss][dsetNames]
        ArrayList<String> wdlListNames = new ArrayList<>();
        
        String[][] wdlPlusMinus = new String[cnames.length*cnames.length][dsets.length];
        
        table.append("p=" + pval + fileHelper_header(cnames)).append("\n");
        
        int count = 0;
        for (int c1 = 0; c1 < foldAccs.length; c1++) {
            table.append(cnames[c1]);
            for (int c2 = 0; c2 < foldAccs.length; c2++) {
                wdlListNames.add(cnames[c1] + "_VS_" + cnames[c2]);
                wdlList.add(new ArrayList<>());
                wdlList.get(count).add(new ArrayList<>());
                wdlList.get(count).add(new ArrayList<>());
                wdlList.get(count).add(new ArrayList<>());    
                
                int wins=0, draws=0, losses=0;
                for (int d = 0; d < dsets.length; d++) {
                    if (accs[c1][d] == accs[c2][d]) {
                        //when the accuracies are identical, p == NaN. 
                        //because NaN < 0.05 apparently it wont be counted as a draw, but a loss
                        //so handle it here                        
                        draws++;
                        wdlList.get(count).get(1).add(dsets[d]);
                        wdlPlusMinus[count][d] = "0";
                        continue;
                    }
                    
                    double p = TwoSampleTests.studentT_PValue(foldAccs[c1][d], foldAccs[c2][d]);
                    
                    if (p > pval) {
                        draws++;
                        wdlList.get(count).get(1).add(dsets[d]);
                        wdlPlusMinus[count][d] = "0";
                    }
                    else { //is sig
                        if (accs[c1][d] > accs[c2][d]) {
                            wins++;
                            wdlList.get(count).get(0).add(dsets[d]);
                            wdlPlusMinus[count][d] = "1";
                        }
                        else  {
                            losses++;
                            wdlList.get(count).get(2).add(dsets[d]);
                            wdlPlusMinus[count][d] = "-1";
                        }
                    }
                }
                table.append(","+wins+"|"+draws+"|"+losses);
                count++;
            }
            table.append("\n");
        }
        
        StringBuilder list = new StringBuilder();
        for (int i = 0; i < wdlListNames.size(); ++i) {
            list.append(wdlListNames.get(i));
            list.append("\n");
            list.append("Wins("+wdlList.get(i).get(0).size()+"):");
            for (String dset : wdlList.get(i).get(0)) 
                list.append(",").append(dset);
            list.append("\n");
            list.append("Draws("+wdlList.get(i).get(1).size()+"):");
            for (String dset : wdlList.get(i).get(1)) 
                list.append(",").append(dset);
            list.append("\n");
            list.append("Losses("+wdlList.get(i).get(2).size()+"):");
            for (String dset : wdlList.get(i).get(2)) 
                list.append(",").append(dset);
            list.append("\n\n");
        }
        
        StringBuilder plusMinuses = new StringBuilder();
        for (int j = 0; j < wdlPlusMinus.length; j++) 
            plusMinuses.append(",").append(wdlListNames.get(j));
        
        for (int i = 0; i < dsets.length; i++) {
            plusMinuses.append("\n").append(dsets[i]);
            for (int j = 0; j < wdlPlusMinus.length; j++) 
                plusMinuses.append(",").append(wdlPlusMinus[j][i]);
        }
        
        return new String[] { table.toString(), list.toString(), plusMinuses.toString() };
    }
    
        
    /**
     * Intended for potentially new stats that are introduced over time (at time of writing this function,
     * build and especially test times), where maybe some older files in the intended analysis 
     * dont have the stat but newer ones do, or some classifiers that write their own files 
     * (via e.g TrainAccuracyEstimate) aren't properly writing them.
     *  
     * Missing for timings is defined as -1. why cant i hold all this spaghetti?
     * 
     * Looking ONLY at the test files, a) because they should all be here anyway else 
     * wouldnt have got as far as needing to call this, b) because the 'testtime' stored 
     * in the testfold files are the test timing we're generally actually interested in,
     * i.e. the total prediction time of the fully trained classifier on the test set, 
     * as opposed to the test time of the classifier on (e.g) corssvalidation folds in training
     * that is stored in the train file
     * 
     * @returns null if any of the wanted info is missing, else the score described by the stat for each results
     */
    private static double[][][] getTimingsIfAllArePresent(ArrayList<ClassifierEvaluation> res, Function<ClassifierResults, Double> getter) { 
        double[][][] info = new double[res.size()][res.get(0).testResults.length][res.get(0).testResults[0].length];
        
        for (int i = 0; i < res.size(); i++) {
            for (int j = 0; j < res.get(i).testResults.length; j++) {
                for (int k = 0; k < res.get(i).testResults[j].length; k++) {
                    info[i][j][k] = getter.apply(res.get(i).testResults[j][k]);

                    if (info[i][j][k] == -1)
                        return null;
                }
            }
        }
        
        return info;
    }
    
    protected static double[][][] getInfo(ArrayList<ClassifierEvaluation> res, Function<ClassifierResults, Double> getter, String trainortest) {
        double[][][] info = new double[res.size()][res.get(0).testResults.length][res.get(0).testResults[0].length];
        for (int i = 0; i < res.size(); i++) {
            if (trainortest.equalsIgnoreCase(trainLabel))
                for (int j = 0; j < res.get(i).trainResults.length; j++)
                    for (int k = 0; k < res.get(i).trainResults[j].length; k++)
                        info[i][j][k] = getter.apply(res.get(i).trainResults[j][k]);
            else if (trainortest.equalsIgnoreCase(testLabel))
                for (int j = 0; j < res.get(i).testResults.length; j++)
                    for (int k = 0; k < res.get(i).testResults[j].length; k++)
                        info[i][j][k] = getter.apply(res.get(i).testResults[j][k]);
            else {
                System.out.println("getInfo(), trainortest="+trainortest);
                System.exit(0);
            }
        }
        return info;
    }
        
    protected static String[] getNames(ArrayList<ClassifierEvaluation> res) {
        String[] names = new String[res.size()];
        for (int i = 0; i < res.size(); i++)
            names[i] = res.get(i).classifierName;
        return names;
    }
    
    protected static void jxl_buildResultsSpreadsheet(String basePath, String expName, ArrayList<Pair<String,Function<ClassifierResults,Double>>> statistics) {        
        WritableWorkbook wb = null;
        WorkbookSettings wbs = new WorkbookSettings();
        wbs.setLocale(new Locale("en", "EN"));
        
        try {
            wb = Workbook.createWorkbook(new File(basePath + expName + "ResultsSheet.xls"), wbs);        
        } catch (Exception e) { 
            System.out.println("ERROR CREATING RESULTS SPREADSHEET");
            System.out.println(e);
            System.exit(0);
        }
        
        WritableSheet summarySheet = wb.createSheet("GlobalSummary", 0);
        String summaryCSV = basePath + expName + "_SMALLglobalSummary.csv";
        jxl_copyCSVIntoSheet(summarySheet, summaryCSV);
        
        for (int i = 0; i < statistics.size(); i++) {
            String statName = statistics.get(i).var1;
            String path = basePath + statName + "/";
            jxl_buildStatSheets(wb, expName, path, statName, i);
        }
        
        try {
            wb.write();
            wb.close();      
        } catch (Exception e) { 
            System.out.println("ERROR WRITING AND CLOSING RESULTS SPREADSHEET");
            System.out.println(e);
            System.exit(0);
        }
    }
    
    protected static void jxl_buildStatSheets(WritableWorkbook wb, String expName, String filenameprefix, String statName, int statIndex) {
        final int initialSummarySheetOffset = 1;
        int numSubStats = 3;    
        int testOffset = 0;
        
        if (!testResultsOnly) {
            numSubStats = 5; 
            testOffset = 2;
            
            WritableSheet trainSheet = wb.createSheet(statName+"Train", initialSummarySheetOffset+statIndex*numSubStats+0);
            String trainCSV = filenameprefix + trainLabel + "/" + expName + "_" +trainLabel+statName+".csv";
            jxl_copyCSVIntoSheet(trainSheet, trainCSV);

            WritableSheet trainTestDiffSheet = wb.createSheet(statName+"TrainTestDiffs", initialSummarySheetOffset+statIndex*numSubStats+1);
            String trainTestDiffCSV = filenameprefix + trainTestDiffLabel + "/" + expName + "_" +trainTestDiffLabel+statName+".csv";
            jxl_copyCSVIntoSheet(trainTestDiffSheet, trainTestDiffCSV);
        }
        
        WritableSheet testSheet = wb.createSheet(statName+"Test", initialSummarySheetOffset+statIndex*numSubStats+0+testOffset);
        String testCSV = filenameprefix + testLabel + "/" + expName + "_" +testLabel+statName+".csv";
        jxl_copyCSVIntoSheet(testSheet, testCSV);
        
        WritableSheet rankSheet = wb.createSheet(statName+"TestRanks", initialSummarySheetOffset+statIndex*numSubStats+1+testOffset);
        String rankCSV = filenameprefix + testLabel + "/" + expName + "_" +testLabel+statName+"RANKS.csv";
        jxl_copyCSVIntoSheet(rankSheet, rankCSV);
        
        WritableSheet summarySheet = wb.createSheet(statName+"TestSigDiffs", initialSummarySheetOffset+statIndex*numSubStats+2+testOffset);
        String summaryCSV = filenameprefix + testLabel + "/" + expName + "_" +testLabel+statName+"_SUMMARY.csv";
        jxl_copyCSVIntoSheet(summarySheet, summaryCSV);
        
        
    }
    
    protected static void jxl_copyCSVIntoSheet(WritableSheet sheet, String csvFile) {
        try { 
            Scanner fileIn = new Scanner(new File(csvFile));

            int rowInd = 0;
            while (fileIn.hasNextLine()) {
                Scanner lineIn = new Scanner(fileIn.nextLine());
                lineIn.useDelimiter(",");

                int colInd = -1;
                while (lineIn.hasNext()) {
                    colInd++; //may not reach end of block, so incing first and initialising at -1
                    
                    String cellContents = lineIn.next();
                    WritableFont font = new WritableFont(WritableFont.ARIAL, 10); 	
                    WritableCellFormat format = new WritableCellFormat(font);
                    
                    try {
                        int iCellContents = Integer.parseInt(cellContents);
                        sheet.addCell(new jxl.write.Number(colInd, rowInd, iCellContents, format));
                        continue; //if successful, val was int, has been written, move on
                    } catch (NumberFormatException nfm) { }
                        
                    try {
                        double dCellContents = Double.parseDouble(cellContents);
                        sheet.addCell(new jxl.write.Number(colInd, rowInd, dCellContents, format));
                        continue; //if successful, val was int, has been written, move on
                    } catch (NumberFormatException nfm) { }
                    
                    
                    sheet.addCell(new jxl.write.Label(colInd, rowInd, cellContents, format));
                }
                rowInd++;
            }
        } catch (Exception e) {
            System.out.println("ERROR BUILDING RESULTS SPREADSHEET, COPYING CSV");
            System.out.println(e);
            System.exit(0);
        }
    }
    
    public static Pair<String[], double[][]> matlab_readRawFile(String file, int numDsets) throws FileNotFoundException {
        ArrayList<String> cnames = new ArrayList<>();
        
        Scanner in = new Scanner(new File(file));
        
        Scanner linein = new Scanner(in.nextLine());
        linein.useDelimiter(",");
        
        while (linein.hasNext())
            cnames.add(linein.next());
        
        double[][] vals = new double[cnames.size()][numDsets];
        
        for (int d = 0; d < numDsets; d++) {
            linein = new Scanner(in.nextLine());
            linein.useDelimiter(",");
            for (int c = 0; c < cnames.size(); c++)
                vals[c][d] = linein.nextDouble();
        }
        return new Pair<>(cnames.toArray(new String[] { }), vals);
    }

    public static void matlab_buildPairwiseScatterDiagrams(String outPath, String expName, String[] statNames, String[] dsets) {      
        outPath += pairwiseScatterDiaPath;
        
        for (String statName : statNames) {
            try {
                Pair<String[], double[][]> asd = matlab_readRawFile(outPath + fileNameBuild_pws(expName, statName) + ".csv", dsets.length);
                ResultTable rt = new ResultTable(ResultTable.createColumns(asd.var1, dsets, asd.var2));

                int numClassiifers = rt.getColumns().size();

                MatlabController proxy = MatlabController.getInstance();
                
                for (int c1 = 0; c1 < numClassiifers-1; c1++) {
                    for (int c2 = c1+1; c2 < numClassiifers; c2++) {
                        String c1name = rt.getColumns().get(c1).getName();
                        String c2name = rt.getColumns().get(c2).getName();
                        
                        if (c1name.compareTo(c2name) > 0) {
                            String t = c1name;
                            c1name = c2name;
                            c2name = t;
                        }
                        
                        String pwFolderName = outPath + c1name + "vs" + c2name + "/";
                        (new File(pwFolderName)).mkdir();
                        
                        List<ResultColumn> pwrl = new ArrayList<>(2);
                        pwrl.add(rt.getColumn(c1name).get());
                        pwrl.add(rt.getColumn(c2name).get());
                        ResultTable pwrt = new ResultTable(pwrl);

                        proxy.eval("array = ["+ pwrt.toStringValues(false) + "];");

                        final StringBuilder concat = new StringBuilder();
                        concat.append("'");
                        concat.append(c1name.replaceAll("_", "\\\\_"));
                        concat.append("',");
                        concat.append("'");
                        concat.append(c2name.replaceAll("_", "\\\\_"));
                        concat.append("'");
                        proxy.eval("labels = {" + concat.toString() + "}");
                        
//                        System.out.println("array = ["+ pwrt.toStringValues(false) + "];");
//                        System.out.println("labels = {" + concat.toString() + "}");
//                        System.out.println("pairedscatter('" + pwFolderName + fileNameBuild_pwsInd(c1name, c2name, statName).replaceAll("\\.", "") + "',array(:,1),array(:,2),labels,'"+statName+"')");
                        
                        proxy.eval("pairedscatter('" + pwFolderName + fileNameBuild_pwsInd(c1name, c2name, statName).replaceAll("\\.", "") + "',array(:,1),array(:,2),labels,'"+statName+"')");
                        proxy.eval("clear");
                    }
                }
            } catch (Exception io) {
                System.out.println("buildPairwiseScatterDiagrams("+outPath+") failed loading " + statName + " file\n" + io);
            }
        }
    }
    
    
    public static int[] dsetGroups_clusterDsetResults(double[/*classifier*/][/*dataset*/] results) { 
        double[/*dataset*/][/*classifier*/] dsetScores = GenericTools.cloneAndTranspose(results);
        int numDsets = dsetScores.length;
        
        for (int dset = 0; dset < dsetScores.length; dset++) {
            double dsetAvg = StatisticalUtilities.mean(dsetScores[dset], false);
            for (int clsfr = 0; clsfr < dsetScores[dset].length; clsfr++)
                dsetScores[dset][clsfr] -= dsetAvg;
        }
        
        Instances clusterData = InstanceTools.toWekaInstances(dsetScores);
        
        XMeans xmeans = new XMeans();
        xmeans.setMaxNumClusters(Math.min((int)Math.sqrt(numDsets), 5));
        xmeans.setSeed(0);
        
        try {
            xmeans.buildClusterer(new Instances(clusterData)); 
            //pass copy, just in case xmeans does any kind of reordering of 
            //instances. we want to maintain order of dsets/instances for indexing purposes
        } catch (Exception e) {
            System.out.println("Problem building clusterer for post hoc dataset groupings\n" + e);
        }
        
        int numClusters = xmeans.numberOfClusters();
        
        int[] assignments = new int[numDsets+1];
        assignments[numDsets] = numClusters;
        
        for (int i = 0; i < numDsets; i++) {
            try {
                assignments[i] = xmeans.clusterInstance(clusterData.instance(i));
            } catch (Exception e) {
                System.out.println("Problem assigning clusters in post hoc dataset groupings, dataset " + i + "\n" + e);
            }
        }
        
        return assignments;
    }
}