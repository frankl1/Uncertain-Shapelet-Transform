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
 * RangeAttrClass - Stores a RangeAttribute with class.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class RangeAttrClass extends PointAttrClass{

	public RangeAttrClass(Attribute b, int c, double w){
		super(b,c,w);
	}
	
	@Override
	public double getValue(){
		return getMidPt();	
	}

	public double getStart(){
		return getAttribute().getStart();
	}

	public double getEnd(){
		return getAttribute().getEnd();
	}

	public RangeAttrClass(Attribute b, int c){
		super(b,c);
	}

	public double getMidPt(){
		return getAttribute().getMidPt();
	}

	public double getDiff(){
		return getAttribute().getDiff();
	}
	
	public double getFrac(double testStart, double testEnd){
		return getAttribute().getFrac(testStart, testEnd);
	}
	
	@Override
	public RangeAttribute getAttribute(){
		return (RangeAttribute) super.getAttribute();
	}

	@Override
	public int compareTo(AttrClass attrClass){
		double diff = getValue() -((RangeAttrClass)attrClass).getValue();
		if(diff > 0) return 1;
		else if( diff ==0) return 0;
		else return -1;
	}
}

