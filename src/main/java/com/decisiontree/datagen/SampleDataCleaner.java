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
package com.decisiontree.datagen;

import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.decisiontree.param.GlobalParam;

/**
 * 
 * SampleDataCleaner - Cleans the generated interval-value with sampled distribution data files.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class SampleDataCleaner extends RangeDataCleaner {
	public static Logger log = Logger.getLogger(SampleDataCleaner.class);

	@Override
	public void cleanGeneratedData(String input) {

		File file = new File(input + GlobalParam.SAMPLE_FILE);
		if(file.exists()) file.delete();
		File directory = new File(input +GlobalParam.SAMPLE_PATH);
		if(!directory.exists()) return;
		
		File [] files = directory.listFiles();
		
		for(int i = 0; i < files.length; i++)
			if(files[i].exists()) files[i].delete();
		directory.delete();
		
	}
	
	public static void main(String args []){
		
		BasicConfigurator.configure();
		
		String training = null;
		String testing = null;
		
		String param = null;
		for(int i = 0; i < args.length; i++){
			param = args[i];
			if(i + 1 < args.length){
				String value = args[i+1];
				if(param.equals("-d"))
					training = value;
				if(param.equals("-t"))
					testing = value;
			}
		}
		
		
		if(training == null){
			log.error("Please input training set using -d option."); 
			System.exit(1);
		}
		
		SampleDataCleaner cleaner = new SampleDataCleaner();
		if(testing == null) cleaner.cleanGeneratedData(training);
		else cleaner.cleanGeneratedDataWithTest(training, testing);
		
	}
	
}
