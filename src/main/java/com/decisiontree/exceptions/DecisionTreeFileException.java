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
package com.decisiontree.exceptions;

/**
*
* DecisionTreeFileException - represent exception for decision tree
*
* @author Smith Tsang
* @since 0.9
*
*/
public class DecisionTreeFileException extends Exception {
	/**
	 *
	 */
	private static final long serialVersionUID = -2845603787524817184L;

	public DecisionTreeFileException(){
		super();
	}

    public DecisionTreeFileException(String msg){
        super(msg);
      }

    public DecisionTreeFileException(String msg, Throwable t){
        super(msg,t);
    }

    public DecisionTreeFileException(Throwable t){
    	super(t);
    }
}
