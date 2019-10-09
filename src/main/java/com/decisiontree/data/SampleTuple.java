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

import com.decisiontree.param.GlobalParam;

/**
 * 
 * SampleTuple - Stores a fully interval-valued tuple with distribution represented by samples.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class SampleTuple extends RangeTuple {
	
	
	/**
	 * @param noAttr
	 * @param cls
	 * @param weight
	 */
	protected SampleTuple(int noAttr, int cls, double weight) {
		super(noAttr, cls, weight);
	}

	public SampleTuple(Attribute [] set, int noAttr, int cls){
		this(set, noAttr, cls, 1.0);
	}

	
	public SampleTuple(Attribute [] set, int noAttr, int cls, double w){
		super(set, noAttr, cls, w);
	}

	public SampleTuple(String data, int noAttr, int cls, int num, SampleDataSet db){
		
		this(data, noAttr, cls, 1.0, num, db);
	}

	public SampleTuple(String data, int noAttr, int cls, int num, SampleDataSet db, boolean averaging){
		
		this(data, noAttr, cls, 1.0, num, db, averaging);
	}
	
	public SampleTuple(String data, int noAttr, int cls, double weight, int num, SampleDataSet db, boolean averaging){
		super(noAttr,cls, weight);
		String [] dataArray = data.split(GlobalParam.SEPERATOR);
		Attribute [] attrSet = new Attribute[noAttr];
		for(int i = 0; i < noAttr; i++){
			int index = dataArray[i].indexOf("->");
			
			double start = Double.parseDouble(dataArray[i].substring(0,index));
			double end = Double.parseDouble(dataArray[i].substring(index+2));

			Sample samples [] = db.getSamples(num,i);
			attrSet[i] = new SampleAttribute(start, end, samples, averaging);
			
		}
		setAttributeSet(attrSet);
	}
	
	public SampleTuple(String data, int noAttr, int cls, double weight, int num, SampleDataSet db){
		this(data, noAttr, cls, weight, num, db, false);
	}
	

	public static SampleTuple copy(SampleTuple tuple, double weight){
		SampleTuple newTuple = new SampleTuple(tuple.getAttributeSet(), tuple.getNoAttr(), tuple.getCls(), weight);
		return newTuple;
	}

	public static SampleTuple copy(SampleTuple tuple, int attrNum, Attribute newAttr, double weight){
		SampleTuple newTuple = new SampleTuple(tuple.getAttributeSet(), tuple.getNoAttr(), tuple.getCls(), weight);
		newTuple.setAttribute(newAttr,attrNum);
		return newTuple;
	}

}
