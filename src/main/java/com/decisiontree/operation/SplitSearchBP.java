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
import java.util.Arrays;
import java.util.List;

import com.decisiontree.data.PointAttrClass;
import com.decisiontree.data.Tuple;

/**
 * 
 * SplitSearchBP - finding the best split point for a set of data using basic pruning technique.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class SplitSearchBP extends SplitSearchUnp{
	
	
	public SplitSearchBP(String dispersionStr){
		super(dispersionStr);
	}

	
	protected SplitSearchBP(Split split){
		super(split);
	}

	@Override
	protected Histogram [] SegGen(List<Tuple> data, int noCls, int attr){

		PointAttrClass [] attrClassSet =  generatePointAttrClass(data,attr);
		if(attrClassSet.length == 0){ 
			log.error("The attrClassSet size should not be 0."); 
			return null;
		}
		Arrays.sort(attrClassSet);
		
		Histogram tempSegmentSet[] = new Histogram[attrClassSet.length];

		int count =0;
		tempSegmentSet[0] = new Histogram(noCls);
		tempSegmentSet[0].setHist(attrClassSet[0].getValue(), attrClassSet[0].getCls(), attrClassSet[0].getWeight());
		for( int i =1; i < attrClassSet.length; i++){
			if(attrClassSet[i].getValue() == tempSegmentSet[count].getValue() || !tempSegmentSet[count].mulCls() && attrClassSet[i].getCls() == tempSegmentSet[count].singleCls())
				tempSegmentSet[count].setHist(attrClassSet[i].getValue(), attrClassSet[i].getCls(), attrClassSet[i].getWeight());
			else{
				tempSegmentSet[++count] = new Histogram(noCls);
				tempSegmentSet[count].setHist(attrClassSet[i].getValue(), attrClassSet[i].getCls(), attrClassSet[i].getWeight());
			}
		}
		int noSegments = count+1;
		
		Histogram segmentSet[] = new Histogram[noSegments];
		for(int i = 0; i < noSegments; i++){
			segmentSet[i] = tempSegmentSet[i];
		}

		return segmentSet;
	}


}

