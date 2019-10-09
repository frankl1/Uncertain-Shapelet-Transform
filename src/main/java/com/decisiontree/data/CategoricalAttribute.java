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
 * CategoricalAttribute - Stores a categorical attribute value. (Current not implemented)
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class CategoricalAttribute implements Attribute{

	private String value;

	/**
	 * Default Constructor
	 */
	protected CategoricalAttribute(){
	}
	
	/**
	 * Creating new attribute with given value
	 * @param value
	 */
	public CategoricalAttribute(String value){
		this.value = value;
	}

	/**
	 * Getting the attribute
	 * @return the value stored
	 */
	public String getValue(){
		return value;
	}

	/**
	 * Setting the attribute
	 * @param value the value to be stored
	 */
	public void setValue(String value){
		this.value = value;
	}
	
	/**
	 * Compare with another object
	 * @param attr the object to be compare
	 * @return 0 if the value are equal or -1 if not equal or object is not an categorical attribute.
	 */
	public int compareTo(Attribute attr){
		if(!(attr instanceof CategoricalAttribute)) return -1;
		if( value.equals(((CategoricalAttribute)attr).value)) return 0;
		return -1;
	}

}

