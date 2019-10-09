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
package com.decisiontree.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.decisiontree.param.GlobalParam;

/**
 * 
 * DataSetInit (Abstract class) - Initializes the DB object.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public abstract class DataSetInit {
	
	private static Logger log = Logger.getLogger(DataSetInit.class);
	
	
	// File Type
	public static final String NAME_FILE = GlobalParam.NAME_FILE; 
	public static final String POINT_FILE = GlobalParam.POINT_FILE; 
	public static final String RANGE_FILE = GlobalParam.RANGE_FILE; 
	public static final String SAMPLE_FILE = GlobalParam.SAMPLE_FILE; 
	
	public static String CONTINUOUS = "continuous";
	
	protected DataSet dataSet;

	/**
	 * Find the number of attributes form input dataset property file
	 * @param input file for dataset properties
	 * @return the number of attribute
	 */
	public int findNoAttr(String input){
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(input + NAME_FILE));
			int i = 0;
			for(; reader.readLine() != null; i++);
			
			return i-1;
		}catch(IOException e){
			e.printStackTrace();
			log.error("No dataset config file or file cannot access. Please try again!");
			System.exit(1);
		}finally{
			try{
				if(reader!= null) 
					reader.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		return -1;
	}

	/**
	 * Find the number of classes form input dataset property file
	 * @param input file for dataset properties
	 * @return the number of classes
	 */
	public int findNoCls(String input){
		BufferedReader reader  = null;
		try{
			reader = new BufferedReader(new FileReader(input + NAME_FILE));
			String data = reader.readLine();
			data = data.replaceAll(" ", "" );
			String [] cls = data.split(GlobalParam.SEPERATOR);
			return cls.length;
		}catch(IOException e){
			e.printStackTrace();
			log.error("No dataset config file or file cannot access. Please try again!");
			System.exit(1);
		}finally{
			try{
				reader.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		return -1;
	}

	/**
	 * Find the list of class names form input dataset property file
	 * @param input file for dataset properties
	 * @return the list of class names
	 */
	public List<String> findClsName(String input){
		BufferedReader reader = null;
		try{
			reader =new BufferedReader(new FileReader(input + NAME_FILE));
			String data = "";
			data = reader.readLine();
			if(data.endsWith(".")) data = data.substring(0, data.length() -1);
			List<String> cls = Arrays.asList(data.split(", "));
			return cls;
		}catch(IOException e){
			e.printStackTrace();
			log.error("No dataset config file or file cannot access. Please try again!");
			System.exit(1);
		}finally{
			try{
				if(reader != null) reader.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Get the dataset
	 * @return the dataset
	 */
	public DataSet getDataSet(){
		return dataSet;
	}

	/**
	 * Preprocessing the data and store the data in dataset
	 * @param input the input data file name
	 */
	abstract public void preProcess(String input);
	
	/**
	 * Store the data in dataset
	 * @param input the input data file name
	 */
	abstract public void storeData(String input);
	
	/**
	 * Counting the number of input tuple
	 * @param input the input data file name
	 * @return the number of tuples
	 */
	abstract public int countNoTuples(String input);
	
	/**
	 * Preprocessing the data of a given fileType and store the data in dataset
	 * @param input the input data file name
	 * @param fileType the type of the file
	 */
	protected void preProcess(String input, String fileType) {

		BufferedReader r = null;
		try {
			r = new BufferedReader(new FileReader(input + NAME_FILE));
			String data = "";
			data = r.readLine();
			int i = 0;

			for (; (data = r.readLine()) != null && i < dataSet.getNoAttr(); i++) {
				String[] name = data.split(":");
				dataSet.setAttrName(i, name[0]);
				if (data.contains(CONTINUOUS))
					dataSet.setContinous(i, true);
				else
					dataSet.setContinous(i, false);
			}

//			dataSet.setNoTuples(countNoTuples(input, fileType));

		} catch (IOException e) {
			e.printStackTrace();
			log.error("No dataset access file or file cannot access. Please try again!");
			System.exit(1);
		} finally {
			try {
				r.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Counting the number of tuples in the given input file
	 * @param input the given input file
	 * @param fileType the file type
	 * @return the number of tuples
	 */
	protected int countNoTuples(String input, String fileType) {
		BufferedReader r = null;
		try {
			r = new BufferedReader(new FileReader(input + fileType));

			int count = 0;
			while (r.readLine() != null) {
				count++;
			}
			log.info("No of Tuples: " +count);
			return count;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				r.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return -1;
	}

}
