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
 * Histogram - Stores the class distribution for an interval
 * @author Smith Tsang
 * @version 0.8
 *
 */
public class Histogram{

	private double start;
	private double end;
	private double classj[];
	private int noCls;

	public Histogram(int noCls){
		classj = new double[noCls];
		for(int i =0 ; i < noCls; i++)
			classj[i] = 0;	
		this.noCls = noCls;
	}

	public Histogram(int noCls, double value){
		this(noCls);
		this.end = value;
	}

	public Histogram(int noCls, double start, double end){
		this(noCls);
		this.start = start;
		this.end = end;
	}


	public void setStart(double start){
		this.start= start;
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
	
	public void setHist(double start, double end, int cls, double fraction){
		setValue(end);
		setStart(start);	
		addCls(cls,fraction);
	}

	public void setHist(double value, int cls, double fraction){
		setValue(value);	
		addCls(cls,fraction);
	}

	public void addCls(int cls, double fraction){
		classj[cls] += fraction;
	}

	public void mergeHist(Histogram h){
		end = h.getEnd();
		addAllCls(h.getAllCls());
	}

	public void addCls(int cls){
		classj[cls]++;
	}

	public double getValue(){
		return end;
	}
	
	public void setValue(double value){
		end = value;
	}

	public double [] getAllCls(){
		return classj;
	}

	public double getCls(int cls){
		return classj[cls];
	}

	public int getNoCls(){
		return noCls;
	}

	public boolean mulCls(){
		int count = 0;
		for(int i =0; i < noCls; i++){
			if(classj[i] != 0)
			       count++;
		}
		return count > 1;
	}

	public boolean empty(){
		for(int i =0; i < noCls; i++){
			if(classj[i] != 0)
			       return false;
		}
		return true;

	}

	public int singleCls(){
		for(int i = 0 ; i < noCls; i++)
			if(classj[i] > 0) return i;
	
		return -1;
	}

	public double size(){
		double totalTuple =0;
		for(int i =0; i < noCls; i++){
			totalTuple += classj[i];
		}
		return totalTuple;
	}
	
	public void addAllCls(double [] classj){
		for(int i =0; i< noCls; i++){
			if(classj[i] == 0) continue;
			this.classj[i] += classj[i];
		}
	}

	public boolean checkDist(Histogram h){

		double ratio = 0;

		for(int i =0; i< noCls; i++){
			if( classj[i] == 0 && h.getCls(i) == 0) continue;
			else if( classj[i] == 0 || h.getCls(i) == 0) return false;
			if(ratio == 0) ratio = classj[i]/h.getCls(i);
			else if(Math.abs(ratio - classj[i]/h.getCls(i)) > 1E-10) 
				return false;
		}
		
		return true;
	}

}
	
