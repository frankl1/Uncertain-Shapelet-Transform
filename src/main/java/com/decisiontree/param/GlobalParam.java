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
package com.decisiontree.param;

import org.apache.log4j.Logger;

/**
 * 
 * GlobalParam - stores the global parameters for the program
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class GlobalParam{
	
	public static Logger log = Logger.getLogger(GlobalParam.class);

	public static final String LOG_FILE = "properties/log.properties"; 
	
	public static final double DOUBLE_PRECISION = 1E-12;
	
	public static final int SAMPLING = 10;
	public static final int NOFOLD = 10;
	
	public static final int DEFAULT_PARTITION = 2;
	
	public static final int DEFAULT_NO_SAMPLES = 100;
	public static final long DEFAULT_SEED = 0;
	public static final double DEFAULT_WIDTH = 0.1;
	
	public static final double DEFAULT_NODESIZE = 1;
	public static final double DEFAULT_PURITY_THRESHOLD = 0.99;

	public static String SAMPLE_PATH = "_PDF/"; 
	public static final String SAMPLE_TUPLE = "T"; 
	public static final String SAMPLE_ATTR = "A"; 
	
	public static final String NAME_FILE = ".names"; 
	public static final String POINT_FILE = ".data"; 
	public static final String RANGE_FILE =".range"; 
	public static final String SAMPLE_FILE = ".error"; 
	
	public static final String TO = "->"; 
	public static final String SEPERATOR = ",";

	public static final String RESULT_FILE = "result/result.rst";

	public static final String TREE_FILE = "tree/tree.udt";
	
	private static int noNode = 0;
	
	private static int noEndPtIntervals = 0;   
	private static int noHeterIntervals = 0;
	private static int noUnpIntervals = 0;
	
	// For End-Point Sampling
	private static int noEndPtSampLBs = 0;
	private static int noUnpEndPtSampLBs = 0;
	private static int noEndPtSampIntervals = 0;
	private static int noEntOnSamples = 0;

	public static int getNoEntCal(){
		return getNoEndPtIntervals() + getNoHeterIntervals() 
		+ getNoEndPtSampIntervals() + getNoEndPtSampLBs() +
		getNoEntOnSamples();
	}

	public static void incrNoNode(){
		noNode++;
	}
	
	public static void addNoNode(int noNode){
		noNode += noNode;
	}
	
	public static int getNoNode(){
		return noNode;
	}

	public static int getNoEndPtIntervals() {
		return noEndPtIntervals;
	}

	public static void setNoEndPtIntervals(int noEndPtIntervals) {
		GlobalParam.noEndPtIntervals = noEndPtIntervals;
	}

	public static void addNoEndPtIntervals(int noEndPtIntervals) {
		GlobalParam.noEndPtIntervals += noEndPtIntervals;
	}
	
	public static void incrNoEndPtIntervals() {
		GlobalParam.noEndPtIntervals++;
	}
	
	public static int getNoEndPtSampIntervals() {
		return noEndPtSampIntervals;
	}

	public static void setNoEndPtSampIntervals(int noEndPtSampIntervals) {
		GlobalParam.noEndPtSampIntervals = noEndPtSampIntervals;
	}
	
	public static void addNoEndPtSampIntervals(int noEndPtSampIntervals) {
		GlobalParam.noEndPtSampIntervals += noEndPtSampIntervals;
	}
	
	public static void incrNoEndPtSampIntervals() {
		GlobalParam.noEndPtSampIntervals++;
	}

	public static int getNoEndPtSampLBs() {
		return noEndPtSampLBs;
	}

	public static void setNoEndPtSampLBs(int noEndPtSampLBs) {
		GlobalParam.noEndPtSampLBs = noEndPtSampLBs;
	}
	
	public static void addNoEndPtSampLBs(int noEndPtSampLBs) {
		GlobalParam.noEndPtSampLBs += noEndPtSampLBs;
	}
	
	public static void incrNoEndPtSampLBs() {
		GlobalParam.noEndPtSampLBs++;
	}
	
	public static int getNoEntOnSamples() {
		return noEntOnSamples;
	}

	public static void setNoEntOnSamples(int noEntOnSamples) {
		GlobalParam.noEntOnSamples = noEntOnSamples;
	}

	public static void addNoEntOnSamples(int noEntOnSamples) {
		GlobalParam.noEntOnSamples += noEntOnSamples;
	}
	
	public static void incrNoEntOnSamples() {
		GlobalParam.noEntOnSamples++;
	}
	
	public static int getNoHeterIntervals() {
		return noHeterIntervals;
	}

	public static void setNoHeterIntervals(int noHeterIntervals) {
		GlobalParam.noHeterIntervals = noHeterIntervals;
	}
	
	public static void addNoHeterIntervals(int noHeterIntervals) {
		GlobalParam.noHeterIntervals += noHeterIntervals;
	}
	
	public static void incrNoHeterIntervals() {
		GlobalParam.noHeterIntervals++;
	}

	public static int getNoUnpEndPtSampLBs() {
		return noUnpEndPtSampLBs;
	}

	public static void setNoUnpEndPtSampLBs(int noUnpEndPtSampLBs) {
		GlobalParam.noUnpEndPtSampLBs = noUnpEndPtSampLBs;
	}

	public static void addNoUnpEndPtSampLBs(int noUnpEndPtSampLBs) {
		GlobalParam.noUnpEndPtSampLBs += noUnpEndPtSampLBs;
	}
	
	public static void incrNoUnpEndPtSampLBs() {
		GlobalParam.noUnpEndPtSampLBs++;
	}
	
	public static int getNoUnpIntervals() {
		return noUnpIntervals;
	}

	public static void setNoUnpIntervals(int noUnpIntervals) {
		GlobalParam.noUnpIntervals = noUnpIntervals;
	}

	public static void addNoUnpIntervals(int noUnpIntervals) {
		GlobalParam.noUnpIntervals += noUnpIntervals;
	}

	public static void incrNoUnpIntervals() {
		GlobalParam.noUnpIntervals++;
	}
	
	public static void clearStoredValues(){
		noNode =0;
		
		noEndPtIntervals = 0;  
		noHeterIntervals = 0;
		noUnpIntervals = 0;
		
		// For End-Point Sampling
		noEndPtSampLBs = 0;
		noUnpEndPtSampLBs = 0;
		noEndPtSampIntervals = 0;
		noEntOnSamples = 0;
	}

	
}
