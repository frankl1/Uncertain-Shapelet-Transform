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
package com.decisiontree.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.decisiontree.function.DecisionTree;
import com.decisiontree.operation.SplitSearch;
import com.decisiontree.param.GlobalParam;
import com.decisiontree.ui.UDTFunctions;

/**
 *
 * UDTApp - runs the main function of the UDT - Decision Tree for Uncertain Data.
 *
 * @author Smith Tsang
 * @since 0.85
 *
 */
class UDTApp {

	private static Logger log = Logger.getLogger(UDTApp.class);

	// MODE parameters
	private static final String GEN = "GEN";
	private static final String BUILD = "BUILD";
	private static final String BUILDSAVE = "BUILDSAVE";
	private static final String OVERALL = "OVERALL";
	private static final String CLEAN = "CLEAN";
	private static final String TESTING = "TESTING";
	private static final String CLASSIFY = "CLASSIFY";

	private static final int NOTRIALS = 1;

	// default values
	private static String training = null;
	private static String testing = null;
	private static String nameFile = null;
	private static int noSamples = GlobalParam.DEFAULT_NO_SAMPLES;
	private static double nodeSize = GlobalParam.DEFAULT_NODESIZE;
	private static double purityThreshold = GlobalParam.DEFAULT_PURITY_THRESHOLD;

	private static String mode = BUILD;
	private static String type = DecisionTree.TIME;
	private static String algorithm = SplitSearch.AVG;

	private static double width = GlobalParam.DEFAULT_WIDTH;

	private static long seed = GlobalParam.DEFAULT_SEED;

	private static boolean varies = false; //NOT SUPPORT IN THIS VERSION

	// For saving tree and classify by tree
	private static String treeFile = GlobalParam.TREE_FILE;

	// for overall mode only
	private static int noTrials = NOTRIALS;
	private static boolean saveToFile = false;
	private static String resultFileName = GlobalParam.RESULT_FILE;


	/**
	 * Loading parameter from command line arguments
	 * @param args
	 * @throws IOException
	 */
	public static void loadCommandArg(String args []) throws IOException{
		log.info("Configure...");
		// default values

		for(int i = 0; i < args.length; i++){
			String param = args[i];
			if( i+1 < args.length ){
				String value = args[i+1];
				if(param.equals("-mode") || param.equals("-m")){
					if(value.equalsIgnoreCase(GEN))
						mode = GEN;
					else if(value.equalsIgnoreCase(OVERALL))
						mode = OVERALL;
					else if(value.equalsIgnoreCase(BUILD))
						mode = BUILD;
					else if(value.equalsIgnoreCase(BUILDSAVE))
						mode = BUILDSAVE;
					else if(value.equalsIgnoreCase(TESTING))
						mode = TESTING;
					else if(value.equalsIgnoreCase(CLASSIFY))
						mode = CLASSIFY;
					else if(value.equalsIgnoreCase(CLEAN))
						mode = CLEAN;
				}
				else if(param.equals("-dataset") || param.equals("-d")){
					training = value;
					continue;
				}

				else if(param.equals("-test") || param.equals("-t")){
					testing = value;
					continue;
				}

				else if(param.equals("-property") || param.equals("-f")){
					nameFile = value;
					continue;
				}

				else if(param.equals("-sample") || param.equals("-s")){
					noSamples = Integer.parseInt(value);
				}

				else if(param.equals("-algorithm") || param.equals("-a")){
					if(value.equalsIgnoreCase("udt"))
						algorithm = SplitSearch.UDT;
					else if(value.equalsIgnoreCase("udtbp"))
						algorithm = SplitSearch.UDTBP;
					else if(value.equalsIgnoreCase("udtlp"))
						algorithm = SplitSearch.UDTLP;
					else if(value.equalsIgnoreCase("udtgp"))
						algorithm = SplitSearch.UDTGP;
					else if(value.equalsIgnoreCase("udtes"))
						algorithm = SplitSearch.UDTES;
					else if(value.equalsIgnoreCase("avg"))
						algorithm = SplitSearch.AVG;
					else if(value.equalsIgnoreCase("udtud"))
						algorithm = SplitSearch.UDTUD;
					else if(value.equalsIgnoreCase("avgud"))
						algorithm = SplitSearch.AVGUD;
					else if(value.equalsIgnoreCase("point"))
						algorithm = SplitSearch.POINT;
				}

				if(mode.equals(BUILDSAVE) || mode.equals(TESTING)){

					if(param.equals("-tree") || mode.equals("-r")){
						treeFile = value;
					}
				}

				if(mode.equals(BUILD) || mode.equals(OVERALL)){

					if(param.equals("-type") || param.equals("-y")){

						if(value.equalsIgnoreCase("xfold"))
							type = DecisionTree.XFOLD;
						else if (value.equalsIgnoreCase("accuracy"))
							type = DecisionTree.ACCUR;
						else type = DecisionTree.TIME;
					}
				}
				if(mode.equals(BUILD) || mode.equals(OVERALL) || mode.equals(BUILDSAVE)){

					if(param.equals("-nodesize") || param.equals("-n")){
						nodeSize = Double.parseDouble(value);
						if(nodeSize < GlobalParam.DEFAULT_NODESIZE) nodeSize = GlobalParam.DEFAULT_NODESIZE;
					}
					else if(param.equals("-purity") || param.equals("-h")){
						purityThreshold = Double.parseDouble(value);
						if(purityThreshold > GlobalParam.DEFAULT_PURITY_THRESHOLD) purityThreshold = GlobalParam.DEFAULT_PURITY_THRESHOLD;
					}


				}
				if(mode.equals(GEN) || mode.equals(OVERALL)){
					if (param.equals("-width") || param.equals("-p")) {
						width = Double.parseDouble(value);
					}
					else if(param.equals("-seed") || param.equals("-e")){
						seed = Integer.parseInt(value);
					}
				}

				if(mode.equals(OVERALL)){
					if (param.equals("-trial") || param.equals("-l"))
						noTrials = Integer.parseInt(value);
					else if (param.equals("-save") || param.equals("-v")){
						saveToFile = true;
						resultFileName = value;
					}
				}

			}

		}


	}

	/**
	 * Loading parameters from properties files
	 * @param propFile
	 * @throws IOException
	 */
	public static void loadProp(File propFile) throws IOException{
		Properties prop = new Properties();
		prop.load(new FileInputStream(propFile));

		mode = prop.getProperty(GlobalProp.MODE, GEN);

		training = prop.getProperty(GlobalProp.DATASET);

		testing = prop.getProperty(GlobalProp.TESTSET);

		nameFile = prop.getProperty(GlobalProp.PROPERTY);

		try{
			noSamples = Integer.parseInt(prop.getProperty(GlobalProp.NOSAMPLES));
		}catch(NumberFormatException e){
			log.warn(e.getMessage(),e);
			noSamples = GlobalParam.DEFAULT_NO_SAMPLES;
		}

		algorithm = prop.getProperty(GlobalProp.ALGORITHM);

		if(mode.equals(BUILDSAVE) || mode.equals(TESTING)){
			treeFile = prop.getProperty(GlobalProp.TREE);
		}

		if(mode.equals(BUILD) || mode.equals(OVERALL)){
			type = prop.getProperty(GlobalProp.TYPE, DecisionTree.TIME);

		}
		if(mode.equals(BUILD) || mode.equals(OVERALL) || mode.equals(BUILDSAVE)){

			try{
				nodeSize = Double.parseDouble(prop.getProperty(GlobalProp.NODESIZE));
			}catch(NumberFormatException e){
				log.warn(e.getMessage(),e);
				nodeSize = GlobalParam.DEFAULT_NODESIZE;
			}

			try{
				purityThreshold = Double.parseDouble(prop.getProperty(GlobalProp.PURITY));
			}catch(NumberFormatException e){
				log.warn(e.getMessage(),e);
				nodeSize = GlobalParam.DEFAULT_PURITY_THRESHOLD;
			}

		}
		if(mode.equals(GEN) || mode.equals(OVERALL)){
			try{
				width = Double.parseDouble(prop.getProperty(GlobalProp.WIDTH));
			}catch(NumberFormatException e){
				log.warn(e.getMessage(),e);
				width = GlobalParam.DEFAULT_WIDTH;
			}
		}

		if(mode.equals(OVERALL)){
			try{
				noTrials = Integer.parseInt(prop.getProperty(GlobalProp.TRIAL));
			}catch (NumberFormatException e){
				log.warn(e.getMessage(),e);
				noTrials = NOTRIALS;
			}

			resultFileName = prop.getProperty(GlobalProp.SAVE, GlobalParam.RESULT_FILE);
			if(!"".equals(resultFileName)) saveToFile = true;
		}

	}

	/**
	 * Tree building based on the console input or properties files
	 * @throws IOException
	 */
	public static void start() throws IOException{
		// if training data file is not specified
		if(training == null || training.equals("")){
			log.error("No training set is specified.");
			System.err.println("Please input training set using -d or -dataset option.");
			System.exit(1);
		}

		if("".equals(testing))
			testing = null;

		// if name file is not specified
		if(nameFile == null || nameFile.equals("")){
			nameFile = training;
		}

		UDTFunctions functions = new UDTFunctions();

		if(mode.equals(GEN) ){
			log.info("Running Generate Mode.");
			System.out.println("You are running generate mode.");
			System.out.println("The generate data would be stored in the same folder of the source data file.");
			functions.generateMode(training, testing, nameFile, algorithm, noSamples, width, seed, varies);
		}

		else if(mode.equals(BUILD) ){
			log.info("Running Build Mode.");
			System.out.println("You are running build mode.");

			String result = functions.buildMode(training, testing, nameFile, algorithm, type, nodeSize, purityThreshold);
			String [] splitResult = result.split(GlobalParam.SEPERATOR);
			if(type.equals(DecisionTree.TIME)){
				log.info("Timing...");

				System.out.println("Building Time: " );
				System.out.println( "\tUserTime: " + splitResult[3]);
				System.out.println( "\tSystemTime: " + splitResult[4]);
				System.out.println("No of Entropy Calculation: " + splitResult[2]);

			}else if(type.equals(DecisionTree.ACCUR)){
				log.info("Finding Accuracy...");

				System.out.println("Testing Accuracy: " + splitResult[1]);
			}else if(type.equals(DecisionTree.XFOLD)){
				log.info("Finding Accuracy by crossfold...");
				System.out.println("Cross-Fold Accuracy: " + splitResult[1]);
			}

		}
		else if(mode.equals(BUILDSAVE)){
			// TODO : implement
			log.info("Running Build And Save Mode.");
			System.out.println("You are running build and save mode.");
			if( functions.buildAndSaveMode(training, nameFile, algorithm,nodeSize, purityThreshold, treeFile))
				System.out.println("The file is successfully saved in " + treeFile + ".");

		}
		else if(mode.equals(TESTING)){
			// TODO: implement
			log.info("Running Testing Mode.");
			System.out.println("You are running testing mode.");
			String result = functions.testingMode(training, nameFile, algorithm, treeFile);
			String [] splitResult = result.split(GlobalParam.SEPERATOR);
			System.out.println("Testing Accuracy: " + splitResult[1]);
		}
		else if(mode.equals(CLASSIFY)){
			// TODO: to be implement in next release
			log.info("Runing Classify Mode");
			System.out.println("You are running classify mode.");
			log.warn("Classify function has not implemented yet. ");
		}

		else if(mode.equals(CLEAN)){
			log.info("Runing Clean Mode");
			System.out.println("You are running clean mode. The operation cannot be reverted.");

			functions.cleanMode(training, testing);

			System.out.println("Data is cleaned successfully.");

		}

		else if( mode.equals(OVERALL)){ // OVERALL Allows multiple trials and file save for data.
			log.info("Running Overall Mode" );
			System.out.println("You are running overall mode. Reminded that the generated data would NOT be cleaned.");
			if(saveToFile){
				functions.overallMode(training, testing, nameFile, algorithm, type, noSamples, width, seed, varies,
						nodeSize, purityThreshold,noTrials,resultFileName);
				System.out.println("The result data is saved in " + resultFileName +".");

			}else{
				System.out.println("The results are as follows:");
				List<String> resultList = functions.overallMode(training, testing, nameFile,
						algorithm, type, noSamples, width, seed, varies, nodeSize, purityThreshold, noTrials);
				for(int i = 0; i < resultList.size(); i++){
					System.out.println(resultList.get(i));
				}
			}
		}

		log.info("The program terminates successfully.");
		System.out.println("The job has finished successfully.");

	}

	/**
	 * Running the Console program
	 * @param args console program arguments
	 */
	public static void main(String args []){

		try{

			PropertyConfigurator.configure(GlobalParam.LOG_FILE);
			String [] infoMessage =
					{
					"UDT Version 0.9  Copyright (C) 2009 Database Group, ",
					"Department of COmputer Science, The University of Hong Kong ",
					"Welcome to UDT Version 0.9! Please wait until the program finished."
					};

			for(int i = 0; i < infoMessage.length; i++){
				log.info(infoMessage[i]);
				System.out.println(infoMessage[i]);
			}

			log.info("Starting...");

			boolean help = false;

			for(int i = 0; i < args.length;i++)
				if(args[i].equals("-h") || args[i].equals("-help"))
					help = true;

			if(help || args.length < 1){
				System.out.println("Usage: Please refer to the README for instructions.");
				System.exit(1);
			}

			boolean useProp = false;
			String propertyFileName = null;

			for(int i = 0; i < args.length;i++)
				if(args[i].equals("-file") || args[i].equals("-P"))
					if(i+1 < args.length ){
						useProp = true;
						propertyFileName = args[i+1];
					}


			if(useProp){
				log.info("Using Properties File: " + propertyFileName);
				File propFile = new File(propertyFileName);
				if(propFile.exists())
					loadProp(propFile);
			}else{
				log.info("Command Arguments");
				loadCommandArg(args);
			}

			printProp(); // for debugging

			start(); // start the building
		}catch(Exception e){
			log.error("Internal errors occur. The program terminates.", e);
			System.err.println("Internal Errors. Please check if your input is incorrect.");
			System.exit(1);
		}


	}


	/** For testing only*/
	static void printProp(){

		log.debug("Training: " +training);
		log.debug("Testing: " + testing);
		log.debug("nameFile: " + nameFile);
		log.debug("noSamples: " + noSamples);
		log.debug("nodeSize: " + nodeSize);
		log.debug("Purity: " + purityThreshold);
		log.debug("Mode: " + mode);
		log.debug("Type: " + type);
		log.debug("Algorithm: " + algorithm);
		log.debug("Width: " + width);
		log.debug("Seed: " + seed);
		log.debug("treeFile: " + treeFile);
		log.debug("noTrials: " + noTrials);
		log.debug("resultFileName: " + resultFileName);

//		System.out.println("Training: " +training);
//		System.out.println("Testing: " + testing);
//		System.out.println("nameFile: " + nameFile);
//		System.out.println("noSamples: " + noSamples);
//		System.out.println("nodeSize: " + nodeSize);
//		System.out.println("Purity: " + purityThreshold);
//		System.out.println("Mode: " + mode);
//		System.out.println("Type: " + type);
//		System.out.println("Algorithm: " + algorithm);
//		System.out.println("Width: " + width);
//		System.out.println("Seed: " + seed);
//		System.out.println("treeFile: " + treeFile);
//		System.out.println("noTrials: " + noTrials);
//		System.out.println("resultFileName: " + resultFileName);
//		System.out.println();
	}

}
