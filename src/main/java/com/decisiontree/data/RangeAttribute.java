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
 * RangeAttribute - Stores a range data attribute.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class RangeAttribute extends PointAttribute {

	public double absStart;
	public double absEnd;
	public double start;
	public double end;

	
	public RangeAttribute(double start, double end){
		setValue((start +end)/2);
		setAbsStart(start);
		setAbsEnd(end);
		setStart(start);
		setEnd(end);
	}

	public RangeAttribute(double absStart, double absEnd, double start, double end){
		setValue((start + end)/2);
		setAbsStart(absStart);
		setAbsEnd(absEnd);
		setStart(start);
		setEnd(end);
	}


	public RangeAttribute(RangeAttribute r, double start, double end){
		this(r.getAbsStart(),r.getAbsEnd(), start, end);
	}


//	public void setRange(double absStart, double absEnd){	
//		setAbsStart(absStart);
//		setAbsEnd(absEnd);
//	}
//	
//	public void setAbsRange(double absStart, double absEnd){	
//		setAbsStart(absStart);
//		setAbsEnd(absEnd);
//	}
	public double getAbsStart(){
		return absStart;
	}
	
	public double getAbsEnd(){
		return absEnd;
	}
	public void setAbsStart(double start){
		this.absStart = start;
	}
	public void setAbsEnd(double end){
		this.absEnd = end;
	}

	public void setStart(double start){
		this.start = start;
	}

	public void setEnd(double end){
		this.end = end;
	}

	public double getStart(){
		return start;
	}
	
	public double getEnd(){
		return end;
	}

	public double getCurFrac(){
		return (end-start)/(absEnd-absStart);
	}
	
	public double getFrac(double testStart, double testEnd){
		return (testEnd-testStart)/(absEnd -absStart);
	}


	public double getMidPt(){
		return getValue();
	}
	public double getDiff(){
		return absEnd - absStart;
	}

	public boolean isOriginal(){
		return (absStart == start) && (absEnd == end);
	}
	
	public static RangeAttribute cutCopy(RangeAttribute p, double curStart,
			double curEnd) {
		RangeAttribute newP = new RangeAttribute(p, curStart, curEnd);
		return newP;
	}
	

	@Override
	public int compareTo(Attribute attr){
		double diff = getStart() -((RangeAttribute)attr).getStart();
		if(diff > 0) return 1;
		else if( diff ==0) return 0;
		else return -1;
	}
}