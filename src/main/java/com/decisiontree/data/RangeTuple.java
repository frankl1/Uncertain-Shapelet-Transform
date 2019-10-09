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
 * RangeTuple - Stores a fully interval-valued tuple.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class RangeTuple extends PointTuple{

	public RangeTuple(Attribute [] set, int noAttr, int cls){
		this(set, noAttr, cls, 1.0);
	}

	public RangeTuple(Attribute [] set, int noAttr, int cls, double w){
		super(set, noAttr,  cls, w);
	}

	protected RangeTuple(int noAttr, int cls, double weight) {
		super(noAttr, cls, weight);
	}

	public RangeTuple(String data, int noAttr, int cls ){
		
		this(data, noAttr, cls, 1.0 );
	}

	public RangeTuple(String data, int noAttr, int cls, double weight){
		super(noAttr,cls, weight);

		String [] dataArray = data.split(GlobalParam.SEPERATOR);
		Attribute [] attrSet = new Attribute[noAttr];
		for(int i = 0; i < noAttr; i++){
			int index = dataArray[i].indexOf("->");
			
			double start = Double.parseDouble(dataArray[i].substring(0,index));
			double end = Double.parseDouble(dataArray[i].substring(index+2));

			attrSet[i] = new RangeAttribute(start, end);
			
		}
		setAttributeSet(attrSet);

	}
	

	public static RangeTuple copy(RangeTuple tuple, double weight){
		RangeTuple newTuple = new RangeTuple(tuple.getAttributeSet(), tuple.getNoAttr(), tuple.getCls(), weight);
		return newTuple;
	}

	public static RangeTuple copy(RangeTuple tuple, int attrNum, Attribute newAttr, double weight){
		RangeTuple newTuple = new RangeTuple(tuple.getAttributeSet(), tuple.getNoAttr(), tuple.getCls(), weight);
		newTuple.setAttribute(newAttr,attrNum);
		return newTuple;
	}

	
}


