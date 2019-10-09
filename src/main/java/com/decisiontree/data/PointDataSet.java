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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * PointDB - Stores the database information for point-value dataset.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class PointDataSet implements DataSet {

	protected List<Tuple> data;
	protected int noTuples;
	
	protected String name;

	private List<String> clsNameList;
	private List<Integer> clsDist;
	private List<String> attrNameList;
	private List<Boolean> continuousList;
	private int noCls;
	private int noAttr;


	/**
	 * Default Constructor
	 *
	 */
	protected PointDataSet(){
		
	}

	/**
	 * Constructor to set the number of classes and attributes for the dataset
	 * @param noCls the number of classes
	 * @param noAttr the number of attributes
	 */
	public PointDataSet(int noCls, int noAttr){
		
		this.noCls = noCls;
		this.noAttr = noAttr;

		clsNameList = new ArrayList<String>(noCls);
		clsDist = new ArrayList<Integer>(noCls);

		for(int i = 0; i < noCls; i++){
			clsNameList.add("");
			clsDist.add(0);
		}
		attrNameList = new ArrayList<String>(noAttr);
		continuousList = new ArrayList<Boolean>(noAttr);

		for(int i =0 ; i < noAttr; i++){
			attrNameList.add("");
			continuousList.add(false);
		}
		
	}
	
	/**
	 * Constructor by given data, the number of classes and attributes
	 * @param data the list of data tuples
	 * @param noCls the number of classes
	 * @param noAttr the number of attributes
	 */
	public PointDataSet(List<Tuple> data, int noCls, int noAttr){
		this(noCls, noAttr);
		
		this.data = data;
	}

	/**
	 * Constructor with input filename
	 * @param input the input dataset file
	 * @param noCls the number of classes
	 * @param noAttr the number of attribute
	 */
	public PointDataSet(String input, int noCls, int noAttr) {
		this(noCls, noAttr);
		this.name = input;
		
	}

	public String getClsName(int clsNum){
		return clsNameList.get(clsNum);
	}

	public int getClsNum(String clsName){
		Iterator<String> iter = clsNameList.iterator();
		for(int i =0; iter.hasNext(); i++)
			if(iter.next().equals(clsName))
				return i;
		return -1;
	}
	
	public String getAttrName(int attrNum){
		return attrNameList.get(attrNum);
	}

	public int getAttrNum(String attrName){
		return attrNameList.indexOf(attrName);
	}

	public List<String> getClsNameList(){
		return clsNameList;
	}
	
	public List<String> getAttrNameList(){
		return attrNameList;
	}

	public int getNoCls(){
		return noCls;
	}

	public int getNoAttr(){
		return noAttr;
	}

	public int getNoTuples(){
		return noTuples;
	}

	public boolean isContinuous(int i){
 		return continuousList.get(i);
	}

	@Override
	public List<Boolean> isContinuousList(){
		return continuousList;
	}

	public int getClsDistribution(int cls){
		return clsDist.get(cls);
	}

	public void setClsName(int i, String name){
		clsNameList.set(i,name);
	}
	
	
	public void setClsNameList(List<String> clsName){
		this.clsNameList = clsName;
	}

	public void setAttrName(int i, String name){
		attrNameList.set(i,name);
	}
	
	public void setAttrNameList(List<String> attrName){
		this.attrNameList = attrName;
	}

	public void setNoCls(int noCls){
		this.noCls = noCls;
	}

	public void setNoAttr(int noAttr){
		this.noAttr = noAttr;
	}
	public void setNoTuples(int noTuples){
		this.noTuples = noTuples;
	}

	public void setClsDistribution(int cls){
		clsDist.set(cls,clsDist.get(cls)+1);
	}

	public void setContinous(int attrNum, boolean continuous){
		continuousList.set(attrNum, continuous);
	}
	
	public void setData(List<Tuple> data){
		this.data = data;
	}

	public List<Tuple> getData(){
		return data;	
	}
	
	public double findEntropy(){
		
		double ent = 0;
		int clsDist[] = new int[noCls];
		for(int i =0; i< noCls; i++){
			clsDist[i] = 0;
		}
		Iterator<Tuple> iter = data.iterator();
		while(iter.hasNext()){
			clsDist[iter.next().getCls()]++;
		}

		for(int i =0; i< noCls; i++){
			ent += clsDist[i]
					* Math.log(clsDist[i] *1.0/noTuples);
		}
		return -1 * ent / Math.log(2.0) / noTuples;

	}


	public double getDomainSize(int attrNum) {
		return getMax(attrNum) - getMin(attrNum);
	}

	
	public double getMax(int attrNum){

		if( !isContinuous(attrNum)) return -1;
		double max = Double.NEGATIVE_INFINITY;
		Iterator<Tuple> iter = data.iterator();
		while(iter.hasNext()){
			PointAttribute b = (PointAttribute) iter.next().getAttribute(attrNum);
	
			if(b.getValue() > max)
				max = b.getValue();
		}

		return max;
	}

	public double getMin(int attrNum){
	
		if( !isContinuous(attrNum)) return -1;
		double min = Double.POSITIVE_INFINITY;
		Iterator<Tuple> iter = data.iterator();
		while(iter.hasNext()){
			PointAttribute b = (PointAttribute) iter.next().getAttribute(attrNum);
		
			if(b.getValue() < min)
				min = b.getValue();
		}

		return min;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
