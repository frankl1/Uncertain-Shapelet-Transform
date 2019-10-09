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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * Tuple (Abstract class) - Represents a tuple.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public abstract class Tuple {
	
	protected Attribute [] attrSet;

	protected int noAttr;
	protected int cls;  
	protected double weight;
	
	/**
	 * Constructor by the number of attribute and the tuple class
	 * @param noAttr the number of attribute
	 * @param cls the tuple class
	 */
	public Tuple(int noAttr, int cls){
		this(noAttr, cls, 1.0);
	}
	
	/**
	 * Constructor by the weight of tuple, the number of attribute and the tuple class
	 * @param noAttr the number of attribute
	 * @param cls the class
	 * @param weight the weight
	 */
	public Tuple(int noAttr, int cls, double weight){
		setNoAttr(noAttr);
		setCls(cls);
		setWeight(weight);
	}
	
	/**
	 * Constructor by the set of attributes, the number of attribute and the tuple class
	 * @param attrSet the set of attribute
	 * @param noAttr the number of attribute
	 * @param cls the tuple class
	 */
	public Tuple(Attribute [] attrSet, int noAttr, int cls){
		this(attrSet,noAttr, cls, 1.0);
	}
	
	/**
	 * Constructor by the set of attributes, the weight of tuple, the number of attribute and the tuple class
	 * @param attrSet the set of attribute
	 * @param noAttr the number of attribute
	 * @param cls the tuple class
	 * @param weight the weight
	 */
	public Tuple(Attribute [] attrSet, int noAttr, int cls, double weight){
		setAttributeSet(attrSet);
		setNoAttr(noAttr);
		setCls(cls);
		setWeight(weight);
	}
	
	/**
	 * Get the weight of the tuples
	 * @return the weight
	 */
	public double getWeight(){
		return weight;
	}
	
	/**
	 * Set the weight of the tuples
	 * @param weight the weight
	 */
	public void setWeight(double weight){
		this.weight = weight;
	}

	/**
	 * Get the attribute by a attribute number
	 * @param attrNum the attribute
	 * @return the attribute
	 */
	public Attribute getAttribute(int attrNum){
		if(attrNum >= noAttr && attrNum < 0)
			return null;
		return attrSet[attrNum];
	}
	
	/**
	 * Set the attribute by the attrubute number
	 * @param attr the attribute
	 * @param attrNum the attribute number
	 */
	public void setAttribute(Attribute attr, int attrNum){
		attrSet[attrNum] = attr;
	}

	/**
	 * Get the set of all attributes in the tuple
	 * @return the set of attribute
	 */
	public Attribute [] getAttributeSet(){
		return attrSet;
	}

	/**
	 * Set the set of all attributes for the tuple
	 * @param attrSet the attribute set
	 */
	public void setAttributeSet(Attribute [] attrSet){
		this.attrSet = new Attribute[attrSet.length];
		for(int i = 0; i < attrSet.length;i++)
			this.attrSet[i] = attrSet[i];
	}

	/**
	 * Get the number of attributes
	 * @return the number of attributes
	 */
	public int getNoAttr(){
		return noAttr;
	}
	
	/**
	 * Set the number of attributes
	 * @param noAttr the number of attributes
	 */
	public void setNoAttr(int noAttr){
		this.noAttr = noAttr;
	}
	
	/**
	 * Get the class of the tuple
	 * @return the tuple class
	 */
	public int getCls(){
		return cls;
	}

	/**
	 * Set the class of the tuple
	 * @param cls the tuple class
	 */
	public void setCls(int cls){
		this.cls = cls;
	}

	/**
	 * Count the weighted number of tuple of a given set of tuples
	 * @param data the given set of tuples
	 * @return the weighted number of tuples
	 */
	public static double countWeightedTuples(List<Tuple> data){
		double count = 0;
		Iterator<Tuple> iter = data.iterator();
		while(iter.hasNext()){
			count += iter.next().getWeight();
		}
		return count;
	}

	public static double[] computeClsDist(List<Tuple> data, int noCls){

		double [] clsDist = new double[noCls];
		Arrays.fill(clsDist,0);

		Tuple tuple = null;
		Iterator<Tuple> iter = data.iterator();
		while(iter.hasNext()){
			tuple = iter.next();
			clsDist[tuple.getCls()] += tuple.getWeight();
		}

		return clsDist;
//		int max = 0,count =0;
//		for(int i = 0 ; i < noCls ; i++){
//			if(clsDist[max] < clsDist[i])
//				max = i;
//			if(clsDist[i] > 0)
//				count++;
//		}
//
//		this.cls = max;

//		sameCls = (count <= 1);
	}
	
	
}
