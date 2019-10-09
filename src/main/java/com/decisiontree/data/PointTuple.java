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
 * PointTuple - Stores a fully point-valued tuple.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class PointTuple extends Tuple{
	
	/**
	 * Copy a tuple's value to a new tuple with a given weight.
	 * @param tuple
	 * @param weight
	 * @return
	 */
	public static PointTuple copy(PointTuple tuple , double weight){
		return new PointTuple(tuple.getAttributeSet(),tuple.getNoAttr(),tuple.getCls(), weight);
	}
	
	/**
	 * Copy a tuple's value to a new tuple
	 * @param tuple
	 * @return
	 */
	public static PointTuple copy(PointTuple tuple){
		return copy(tuple,tuple.getWeight());
	}
	
	/**
	 * Constructor
	 * @param noAttr the number of attributes
	 * @param cls the class of the the tuples
	 */
	protected PointTuple(int noAttr, int cls){
		super(noAttr,cls);
	}
	
	/**
	 * 
	 * Constructor with weight
	 * @param noAttr the number of attributes
	 * @param cls the class of the tuple
	 * @param weight the weight of the tuple
	 */
	protected PointTuple(int noAttr, int cls, double weight){
		super(noAttr,cls, weight);
	}
	
	/**
	 * Constructor by input data file name
	 * @param data input data file name
	 * @param noAttr the number of attributes
	 * @param cls the class of the tuple
	 */
	public PointTuple(String data, int noAttr, int cls){
		this(data, noAttr, cls, 1.0);
	}

	/**
	 * Constructor by input data file name and weight
	 * @param data input data file name
	 * @param noAttr the number of attributes
	 * @param cls the class of the tuple
	 * @param weight the weight of the tuple
	 */
	public PointTuple(String data, int noAttr,  int cls, double weight){
		super(noAttr, cls);

		String [] dataArray = data.split(GlobalParam.SEPERATOR);
		Attribute [] attrSet = new Attribute[noAttr];
		for(int i = 0; i < noAttr; i++){
			attrSet[i] = new PointAttribute(Double.parseDouble(dataArray[i]));
		}
		setAttributeSet(attrSet);
		
	}

	/**
	 * Constructor by attribute list
	 * @param attrSet the attribute list for the tuple
	 * @param noAttr the number of attributes
	 * @param cls the class of the tuple
	 */
	public PointTuple(Attribute [] attrSet, int noAttr, int cls){
		super(attrSet, noAttr, cls);
	}
	
	/**
	 * Constructor by attribute list with weight
	 * @param attrSet the attribute list for the tuple
	 * @param noAttr the number of attributes
	 * @param cls the class of the tuple
	 * @param w the weight of the tuple
	 */
	public PointTuple(Attribute [] attrSet, int noAttr,  int cls, double w){
		super(attrSet, noAttr, cls, w);
	}


}