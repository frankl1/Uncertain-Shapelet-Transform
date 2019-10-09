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

import java.util.List;

/**
 * 
 * DataSet (Interface) - Stores the dataset informations and tuples.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public interface DataSet {
	public static final String POINT = "POINT";
	public static final String ERROR = "ERROR";
	public static final String RANGE = "RANGE";

	public static final boolean CONTINUOUS = true;
	public static final boolean DISCRETE = false;
	
	/**
	 * Get dataset class name by given class number
	 * @param clsNum the class number
	 * @return the corresponding class name
	 */
	public String getClsName(int clsNum);
	
	/**
	 * set dataset class name by given class number
	 * @param clsNum the class number
	 * @param clsName the class name to be set
	 */
	public void setClsName(int clsNum, String clsName);
	
	/**
	 * Get class number of the given dataset class name.
	 * @param clsName the class name
	 * @return the corresponding class number
	 */
	public int getClsNum(String clsName);
	
	/**
	 * Get the corresponding attribute name by given attribute number
	 * @param attrNum the attribute number
	 * @return the attribute name
	 */
	public String getAttrName(int attrNum);
	
	/**
	 * Set the attribute name by a given attribute number
	 * @param attrNum the attribute number
	 * @param attrName the attribute name
	 */
	public void setAttrName(int attrNum, String attrName);

	/**
	 * Get the corresponding attribute number by a given attribute name
	 * @param attrName the attribute name
	 * @return the corresponding attribute number
	 */
	public int getAttrNum(String attrName);

	/**
	 * Ger the dataset class name list
	 * @return the list of class names
	 */
	public List<String> getClsNameList();
	
	/**
	 * Set the dataset class name list
	 * @param clsNameList the list of class names
	 */
	public void setClsNameList(List<String> clsNameList);
	
	/**
	 * Get the dataset attribute name list
	 * @return the list of attribute name
	 */
	public List<String> getAttrNameList();
	
	/**
	 * Set the dataset attribute name list
	 * @param attrNameList the list of attribute name
	 */
	public void setAttrNameList(List<String> attrNameList);

	/**
	 * Get the number of classes in the dataset
	 * @return the number of classes
	 */
	public int getNoCls();
	
	/**
	 * Set the number of classes for the dataset
	 * @param noCls the number of classes
	 */
	public void setNoCls(int noCls);
	
	/**
	 * Get the number of attributes for the dataset
	 * @return the number of attributes
	 */
	public int getNoAttr();
	
	/**
	 * Set the number of attributes for the dataset
	 * @param noAttr the number of attributes
	 */
	public void setNoAttr(int noAttr);

	/**
	 * Check if a given attribute is a continuous attribute
	 * @param attrNum the attribute number
	 * @return the number of attributes
	 */
	public boolean isContinuous(int attrNum);

	/**
	 * Get the list of attribute is continuity
	 * @return the list of the attribute continuity
	 */
	public List<Boolean> isContinuousList();

	/**
	 * Get the number of tuples
	 * @return
	 */
	public int getNoTuples();
	
	/**
	 * Set the number of tuples by the given number of tuples
	 * @param noTuples the number of tuples
	 */
	public void setNoTuples(int noTuples);

	/**
	 * Get the class distribution of a given class number
	 * @param clsNum the class number
	 * @return the class distribution
	 */
	public int getClsDistribution(int clsNum);
	
	/**
	 * Set the class distribution of a given class number
	 * @param clsNum
	 */
	public void setClsDistribution(int clsNum);

	/**
	 * Set the continuity of a given attribute number
	 * @param attrNum the attribute number
	 * @param continuous the continuity
	 */
	public void setContinous(int attrNum, boolean continuous);

	/**
	 * Get the domain size of a given attribute number
	 * @param attrNum the attribute number
	 * @return the domain size
	 */
	public double getDomainSize(int attrNum);

	/**
	 * Get the max value of a given attribute number.
	 * @param attrNum the attribute number
	 * @return the max value
	 */
	public double getMax(int attrNum);
	
	/**
	 * Get the min value of a given attribute number.
	 * @param attrNum the attribute number
	 * @return the min value
	 */
	public double getMin(int attrNum);

	/**
	 * Get the name of the dataset
	 * @return the dataset name
	 */
	public String getName();
	
	/**
	 * Set the name of the dataset
	 * @param name the dataset name
	 */
	public void setName(String name);
	
	/**
	 * Get the list of tuples
	 * @return the list of tuples
	 */
	public List<Tuple> getData();
	
	/**
	 * Set the list of tuples
	 * @param data the list of tuples
	 */
	public void setData(List<Tuple> data);


	/**
	 * Find the entropy of the list of tuples
	 * @return the entropy value
	 */
	public double findEntropy();
}
