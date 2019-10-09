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
 * SampleAttrClass - Stores a SampleAttribute with class.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class SampleAttrClass extends RangeAttrClass{
	
	public SampleAttrClass(Attribute b, int c, double w){
		super(b,c,w);
	}

	@Override
	public double getStart(){
		return getAttribute().getStart();
	}

	@Override
	public double getEnd(){
		return getAttribute().getEnd();
	}

	public SampleAttrClass(Attribute b, int c){
		super(b,c);
	}

	public double getAverage(){
		return getAttribute().getAverage();
	}

	@Override
	public double getDiff(){
		return getAttribute().getDiff();
	}

	@Override
	public double getFrac(double left, double right){
		
		return getAttribute().getFrac(left,right);
	}

	public double getCDist(double value){
		return getAttribute().getCDist(value);
	}

	public Sample getSample(int no){
		return getAttribute().getSample(no);
	}


	public int getEqualOrLarger(double value){
		return getAttribute().getEqualOrLarger(value);
	}

	public int getNearSample(double value){
		return getAttribute().getNearSample(value);
	}

	public int getNearSample(int start, double value){
		return getAttribute().getNearSample(start, value);
	}

	public int getStartPos(){
		return getAttribute().getStartPos();
	}

	public int getEndPos(){
		return getAttribute().getEndPos();

	}
	
	public double getCurFrac(){
		return ((getAttribute())).getCurFrac();
	}
	
	public double getFrac(int startPos, int endPos){
		return ((getAttribute())).getFrac(startPos, endPos);
	}

	public double getSampleValue(int pos){
		return ((getAttribute())).getSampleValue(pos);
	}
	
	public Sample [] getSamples(){
		return getAttribute().getSamples();
	}
	
	@Override
	public SampleAttribute getAttribute(){
		return (SampleAttribute) super.getAttribute();
	}

	@Override
	public int compareTo(AttrClass attrClass){
		double diff = getStart() -((SampleAttrClass)attrClass).getStart();
		if(diff > 0) return 1;
		else if( diff ==0) return 0;
		else return -1;
	}
}
