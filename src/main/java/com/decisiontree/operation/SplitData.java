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
package com.decisiontree.operation;

/**
 * 
 * SplitData - Stores the split information, including attribute, split point and the corresponding dispersion
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class SplitData{
	private int attrNum;
	private double splitPt;
	private double dispersion;

	/**
	 * Default Constructor 
	 */
	public SplitData(){
		dispersion = Double.POSITIVE_INFINITY;
	}
	
	/**
	 * Constructor with attribute number, split point and entropy
	 * @param attrNum the attribute number
	 * @param splitPt the split point
	 * @param dispersion the dispersion
	 */
	public SplitData(int attrNum, double splitPt, double dispersion){
		this.attrNum = attrNum;
		this.splitPt = splitPt;
		this.dispersion = dispersion;
	}

	/**
	 * Setting the split data values at one time.
	 * @param attrNum the attribute number
	 * @param splitPt the split point
	 * @param dispersion the dispersion
	 */
	public void setSplitData(int attrNum, double splitPt, double dispersion){
		this.attrNum = attrNum;
		this.splitPt = splitPt;
		this.dispersion = dispersion;
	}
	
	
	/**
	 * Set the split point corresponding to the attribute
	 * @param splitPt the split point
	 */
	public void setSplitPt(double splitPt){
		this.splitPt = splitPt;
	}

	/**
	 * Set the attribute number
	 * @param attrNum the attribute number
	 */
	public void setAttrNum(int attrNum){
		this.attrNum = attrNum;
	}
	
	/**
	 * Get the split point corresponding to the attribute
	 * @return the split point
	 */
	public double getSplitPt(){
		return splitPt;
	}

	
	/**
	 * Get the attribute number
	 * @return the attribute number
	 */
	public int getAttrNum(){
		return attrNum;
	}

	/**
	 * Set the dispersion of the split point
	 * @param dispersion the dispersion
	 */
	public void setDispersion(double dispersion){
		this.dispersion = dispersion;
	}

	/**
	 * Gett the dispersion of the split point
	 * @return the dispersion
	 */
	public double getDispersion(){
		return dispersion;
	}

	/**
	 * Check if it is a valid split point
	 * @return whether the split point is valid
	 */
	public boolean isValidSplit(){
		return dispersion != Double.POSITIVE_INFINITY;
	}

}
