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

/**
 * 
 * PointAttribute - Stores a point data attribute.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class PointAttribute implements Attribute {
	private double value;
	
	/**
	 * Default Constructor
	 */
	protected PointAttribute(){
	}
	
	/**
	 * Constructining a new PointAttribute with a given value
	 * @param value
	 */
	public PointAttribute(double value){
		this.value = value;
	}

	/**
	 * Get attribute value
	 * @return the value
	 */
	public double getValue(){
		return value;
	}

	/**
	 * Set attribute value
	 * @param value the value
	 */
	public void setValue(double value){
		this.value = value;
	}

	/**
	 * Compare with a given object
	 * @param attr the object to be compare
	 */
	public int compareTo(Attribute attr){
		double diff = value -((PointAttribute)attr).value;
		if(diff > 0) return 1;
		else if( diff ==0) return 0;
		else return -1;
	}

}
