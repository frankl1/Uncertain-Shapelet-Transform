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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.decisiontree.param.GlobalParam;

/**
 *
 * RangeDataSetInit - Initializes a RangeDataSet object.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class RangeDataSetInit extends DataSetInit{

	private static Logger log = Logger.getLogger(RangeDataSetInit.class);

	@Override
	public RangeDataSet getDataSet(){
		return (RangeDataSet)dataSet;
	}

	public RangeDataSetInit(String input){

			dataSet = new RangeDataSet(input, findNoCls(input),findNoAttr(input));
			dataSet.setClsNameList(findClsName(input));
			preProcess(input);
			dataSet.setNoTuples(countNoTuples(input));
			storeData(input);

	}

	/**
	 * Constructor by input data file name
	 * @param input the input data file name
	 */
	public RangeDataSetInit(String input, String name) {

		dataSet = new RangeDataSet(input,findNoCls(name), findNoAttr(name));
		dataSet.setClsNameList(findClsName(name));
		preProcess(name);
		dataSet.setNoTuples(countNoTuples(input));
		storeData(input);

	}

	@Override
	public int countNoTuples(String input) {
		return countNoTuples(input, RANGE_FILE);
	}

	@Override
	public void preProcess(String input) {
		preProcess(input, RANGE_FILE);

	}

	@Override
	public void storeData(String input) {
		RangeDataSet db = getDataSet();

		BufferedReader reader = null;
		try{

			int noTuples = db.getNoTuples();

			reader = new BufferedReader(new FileReader(input + RANGE_FILE));

			String data = "";
			List <Tuple> t = new ArrayList<Tuple>(noTuples);
			for(int i =0; (data = reader.readLine()) != null && i < noTuples; i++){
				int index = data.lastIndexOf(GlobalParam.SEPERATOR);
				int cls = db.getClsNum(data.substring(index+1));
				db.setClsDistribution(cls);
				t.add(new RangeTuple(data, db.getNoAttr(), cls));
			}

			db.setData(t);
		} catch (IOException e) {
			e.printStackTrace();
			log.error("No dataset file or file cannot access. Please try again!");
			System.exit(1);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}


}

