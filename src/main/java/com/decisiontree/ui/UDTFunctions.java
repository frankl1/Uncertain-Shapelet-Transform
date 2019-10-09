/**
 * Decision Tree Classification With Uncertain Data (UDT)
 * Copyright (C) 2009, The Database Group,
 * Department of Computer Science, The University of Hong Kong
 *
 * This file is part of UDT.
 *
 * UDT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UDT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.decisiontree.ui;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.decisiontree.datagen.RangeDataGen;
import com.decisiontree.datagen.SampleDataCleaner;
import com.decisiontree.datagen.SampleDataGen;
import com.decisiontree.eval.DispersionMeasure;
import com.decisiontree.file.FileUtil;
import com.decisiontree.function.DecisionTree;
import com.decisiontree.function.DecisionTreeFactory;
import com.decisiontree.measure.Times;
import com.decisiontree.operation.SplitSearch;
import com.decisiontree.operation.SplitSearchFactory;
import com.decisiontree.param.GlobalParam;

/**
 *
 * UDTFunctions - as an interface to assist UDTApp to run the programs
 *
 * @author Smith Tsang
 * @since 0.9
 *
 */
public class UDTFunctions {

	public static Logger log = Logger.getLogger(UDTFunctions.class);

	/**
	 * Generate interval-valued data from point data in training dataset
	 * @param training the training dataset file
	 * @param width the interval width (relative to domain)
	 * @param varies whether the interval width varies
	 */
	private void generateData(String training, String nameFile,
			double width, boolean varies) {
		generateData(training, null, nameFile, width, varies);
	}

	/**
	 * Generate interval-valued data from point data in training and testing dataset
	 * @param training the training dataset file
	 * @param testing the testing dataset file
	 * @param width the interval width (relative to domain)
	 * @param varies whether the interval width varies
	 */
	private void generateData(String training, String testing, String nameFile,
			double width, boolean varies) {

		log.info("Generating interval uncertain data");

		RangeDataGen gen = new RangeDataGen(training, nameFile, varies);
		double widths[] = new double[gen.getNoAttr()];

		for (int i = 0; i < gen.getNoAttr(); i++)
			widths[i] = width;

		if(testing == null)
			gen.storeGeneratedData(training, widths);
		else
			gen.storeGeneratedDataWithTest(training, testing, widths);
	}

	/**
	 * Generate interval-valued sample-distributed data from point data in training dataset
	 * @param training the training dataset file
	 * @param noSamples the number of samples
	 * @param width the interval width (relative to domain)
	 * @param seed the random-generate seed number
	 * @param varies whether the interval width varies
	 */
	private void generateData(String training, String nameFile, int noSamples,
			double width, long seed,  boolean varies) {
		generateData(training, null, nameFile, noSamples, width, seed,  varies);
	}

	/**
	 * Generate interval-valued sample-distributed data from point data in training dataset
	 * @param training the training dataset file
	 * @param testing the testing dataset file
	 * @param noSamples the number of samples
	 * @param width the interval width (relative to domain)
	 * @param seed the random-generate seed number
	 * @param varies whether the interval width varies
	 */
	private void generateData(String training, String testing, String nameFile,
			int noSamples, double width, long seed, boolean varies) {

		log.info("Generating sampled interval uncertain data");

		SampleDataGen gen = new SampleDataGen(training, nameFile, noSamples, seed, varies);
		double widths[] = new double[gen.getNoAttr()];

		for (int i = 0; i < gen.getNoAttr(); i++)
			widths[i] = width;

		if(testing == null)
			gen.storeGeneratedData(training, widths);
		else
			gen.storeGeneratedDataWithTest(training, testing, widths);
	}



	/**
	 * Generate data mode with different algorithms and settings
	 * @param training the training dataset file
	 * @param testing the testing dataset file
	 * @param nameFile the property file
	 * @param algorithm the algorithm
	 * @param noSamples the number of samples
	 * @param width the width of the interval
	 * @param seed the seed of the random guassian samples
	 * @param varies whether the interval width varies
	 */
    public void generateMode(String training, String testing, String nameFile, String algorithm, int noSamples, double width, long seed, boolean varies){

		log.info("Generating Uncertain Data...");
		if(algorithm.equals(SplitSearch.POINT))
			log.info("No Uncertain Data Generation Required.");
		if(algorithm.equals(SplitSearch.UDTUD) || algorithm.equals(SplitSearch.AVGUD)){
			if(testing == null)
				generateData(training, nameFile, width, varies);
			else
			generateData(training, testing, nameFile, width, varies);
		}
		else{
			if(testing == null)
				generateData(training, nameFile, noSamples, width, seed, varies);
			else
			generateData(training, testing, nameFile, noSamples, width, seed, varies);
		}
    }


    public boolean buildAndSaveMode(String training, String nameFile, String algorithm, double nodeSize, double purityThreshold, String treeFile){
    	// Currently using entropy
    	SplitSearch splitSearch = SplitSearchFactory.createSplitSearch(algorithm, DispersionMeasure.ENTROPY);
    	if(splitSearch == null){
    		log.error("Incorrect algorithm specified.");
    		return false;
    	}

		DecisionTree decisionTree = DecisionTreeFactory.createDecisionTree(algorithm, splitSearch, nodeSize, purityThreshold);

		return decisionTree.buildAndSaveTree(training, nameFile, treeFile);

    }


    public String buildMode(String training, String testing, String nameFile, String algorithm, String type,  double nodeSize, double purityThreshold){

    	// Currently using entropy
    	SplitSearch splitSearch = SplitSearchFactory.createSplitSearch(algorithm, DispersionMeasure.ENTROPY);
    	if(splitSearch == null){
    		log.error("Incorrect algorithm specified.");
    		return null;
    	}

		final Times start = new Times();

		DecisionTree decisionTree = DecisionTreeFactory.createDecisionTree(algorithm, splitSearch, nodeSize, purityThreshold);

		String result = "";

		if(type.equals(DecisionTree.TIME)){
			log.info("Timing...");

			decisionTree.buildTree(training, nameFile);
			final Times end = new Times();

			System.out.println("Building Time: " );
			final Times sub = end.difference(start);
			sub.printTime();

			result = GlobalParam.getNoEntCal() +"," + sub.getUserTimeInSeconds() + "," + sub.getSystemTimeInSeconds();

		}else if(type.equals(DecisionTree.ACCUR)){
			log.info("Finding Accuracy...");
			double accuracy = 0.0;

			if(testing == null)
				accuracy = decisionTree.findAccuracy(training, nameFile);
			else accuracy = decisionTree.findAccuracy(training, testing, nameFile);

			accuracy = Math.rint(accuracy *10000)/10000;
			result = GlobalParam.getNoEntCal() + "," +accuracy;

		}else if(type.equals(DecisionTree.XFOLD)){
			log.info("Finding Accuracy by crossfold");
			double accuracy = decisionTree.crossFold(training, nameFile);

			accuracy = Math.rint(accuracy *10000)/10000;
			result = GlobalParam.getNoEntCal() + "," + accuracy;

		}
		return result;

    }

    public String testingMode(String testing, String nameFile, String algorithm, String treeFile){
    	// Currently using entropy
    	SplitSearch splitSearch = SplitSearchFactory.createSplitSearch(algorithm, DispersionMeasure.ENTROPY);
    	if(splitSearch == null){
    		log.error("Incorrect algorithm specified.");
    		return null;
    	}

		DecisionTree decisionTree = DecisionTreeFactory.createDecisionTree(algorithm, splitSearch);

		log.info("Finding Accuracy...");
		double accuracy = decisionTree.findAccuracyByTree(treeFile, testing, nameFile);

		accuracy = Math.rint(accuracy *10000)/10000;
		return GlobalParam.getNoEntCal() + "," +accuracy;
    }

    public String classifyMode(String dataFile, String nameFile, String treeFile){
    	// TODO: to be implemented in the next version
    	return null;
    }

    public void cleanMode(String training, String testing){

		SampleDataCleaner cleaner = new SampleDataCleaner();
		if(testing == null)
			cleaner.cleanGeneratedData(training);
		else cleaner.cleanGeneratedDataWithTest(training, testing);


    }

    public String overallMode(String training, String testing, String nameFile,String algorithm, String type, int noSamples, double width, long seed, boolean varies,
    		double nodeSize, double purityThreshold){

		generateMode(training,testing,nameFile,algorithm,noSamples,width,seed,varies);
		String result = buildMode(training, testing, nameFile, algorithm, type, nodeSize, purityThreshold);
		GlobalParam.clearStoredValues();

		return result;

    }

    public List<String> overallMode(String training, String testing, String nameFile, String algorithm, String type, int noSamples, double width, long seed, boolean varies,
    		double nodeSize, double purityThreshold, int noTrials){
    	List<String> resultList = new ArrayList<String>(noTrials);
    	for(int i =0; i < noTrials; i++)
    		resultList.add(algorithm + ","+ i + "," +
    				overallMode(training,testing,nameFile,algorithm,type,noSamples,width,seed+i,varies,nodeSize,purityThreshold));

    	return resultList;

    }


    public void overallMode(String training, String testing, String nameFile, String algorithm, String type, int noSamples, double width, long seed, boolean varies,
    		double nodeSize, double purityThreshold, int noTrials, String resultFileName) throws IOException{
    	BufferedWriter writer = null;
		FileUtil.createFileWithDirectory(resultFileName);
    	try{
			writer = new BufferedWriter(new FileWriter(resultFileName));
			writer.write("Decision Tree Build Type = " + type );
			writer.newLine();
			if(type.equals(DecisionTree.TIME))
				writer.write("Algorithm,Trail,NoEntropyCal,UserTime,SystemTime");
			else writer.write("Algorithm,Trial,NoEntropyCal,Accuracy");
			writer.newLine();

			for(int i = 0 ; i < noTrials ; i++){
				writer.write(algorithm + "," + i + "," +
						overallMode(training,testing, nameFile, algorithm,type,noSamples,width,seed+i,varies,nodeSize,purityThreshold));
				writer.newLine();
			}
		}
		finally{
			if(writer != null)
				writer.close();
		}
    }

}
