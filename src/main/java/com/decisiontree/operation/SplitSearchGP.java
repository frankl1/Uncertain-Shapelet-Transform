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

import com.decisiontree.data.SampleAttrClass;
import com.decisiontree.data.Tuple;
import com.decisiontree.param.GlobalParam;

/**
 *
 * SplitSearchGP - Finds the best split point for a set of data using global pruning technique.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class SplitSearchGP extends AbstractSplitSearch {
	
	public SplitSearchGP(String dispersionStr){
		this(new BinarySplitGP(dispersionStr));
	}

	
	protected SplitSearchGP(Split split){
		super(split);
	}


	protected SampleAttrClass []  getSampleAttrClass(List<Tuple> data,  int attr){

		int noTuples = data.size();
		SampleAttrClass [] attrClass= new SampleAttrClass[noTuples];
		for(int j = 0; j < noTuples; j++)
			attrClass[j] = new SampleAttrClass(data.get(j).getAttribute(attr),data.get(j).getCls(), data.get(j).getWeight());
		Arrays.sort(attrClass);

		return attrClass;
	}


	private Histogram [] SegGen(SampleAttrClass [] attrClassList, int noCls){

		int noTuples = attrClassList.length;

		int noEndPts = noTuples *2;
		double [] endPtSet = new double[noEndPts];
		for(int  i =0 ; i < noTuples; i++){
			endPtSet[2*i] = attrClassList[i].getStart();
			endPtSet[2*i +1] = attrClassList[i].getEnd();
		}

		Arrays.sort(endPtSet);

		int maxNoSegments = noEndPts -1;
		Histogram tempSegmentSet [] = new Histogram[maxNoSegments];
		for(int i =0 ; i < noEndPts-1; i++){
			tempSegmentSet[i] = new Histogram(noCls, endPtSet[i], endPtSet[i+1]);
		}

		int temp =0;
		double next =0.0;
		int prevPos = -1,currPos = -1, startPos = -1, endPos = -1;

		for(int i = 0; i < noTuples; i++){

			for(;temp < maxNoSegments && attrClassList[i].getStart() > tempSegmentSet[temp].getEnd();temp++);
			if(temp >= maxNoSegments) break;

			startPos = attrClassList[i].getStartPos();
			endPos = attrClassList[i].getEndPos();

			currPos = startPos;
			prevPos = startPos; // prevPos;

			//Assume pos1 < pos2

			next = attrClassList[i].getSampleValue(startPos+1); // need to add

			double totalFrac = 0;
			for(int rtemp = temp; rtemp < maxNoSegments && attrClassList[i].getEnd() > tempSegmentSet[rtemp].getStart(); rtemp++){
				if(tempSegmentSet[rtemp].getEnd() < next) continue;

				currPos = attrClassList[i].getNearSample(currPos+1, tempSegmentSet[rtemp].getEnd());

			 	double frac = 0.0;
				if(currPos <= startPos) frac = 0.0;
				else if(currPos >= endPos) frac = attrClassList[i].getFrac(prevPos, endPos); // need to add
				else frac = attrClassList[i].getFrac(prevPos, currPos);

				tempSegmentSet[rtemp].addCls(attrClassList[i].getCls(), frac * attrClassList[i].getWeight());

				totalFrac += frac;
				if(currPos >= endPos || frac >= (1.0 - 1E-14)) break;
				next = attrClassList[i].getSampleValue(currPos+1);
				prevPos = currPos;
			}

		}

		int noPrunedSegments = 0;
		if(tempSegmentSet[0].empty()) {tempSegmentSet[0] = null; noPrunedSegments++;}
		for(int i = 1; i < maxNoSegments; i++){
			if(tempSegmentSet[i].empty()) {
				tempSegmentSet[i] = tempSegmentSet[i-1];
				tempSegmentSet[i-1] = null;
				noPrunedSegments++;
				continue;
			}
			if(tempSegmentSet[i].mulCls() || tempSegmentSet[i-1] == null) continue;
			if(tempSegmentSet[i-1].singleCls() == tempSegmentSet[i].singleCls()){
				tempSegmentSet[i-1].mergeHist(tempSegmentSet[i]);
				tempSegmentSet[i] = tempSegmentSet[i-1];
				tempSegmentSet[i-1] = null;
				noPrunedSegments++;
			}
		}
		Histogram [] segmentSet = new Histogram[maxNoSegments - noPrunedSegments];
		int count = 0;
		for(int i = 0; i < maxNoSegments; i++)
			if(tempSegmentSet[i] != null){
				segmentSet[count++] = tempSegmentSet[i];
		 	}
		return segmentSet;

	}

	public SplitData findBestAttr(List<Tuple> data, int noCls, int noAttr) {

		SplitData splitData = new SplitData();
		splitData.setDispersion(Double.POSITIVE_INFINITY);
		double totalTuples = Tuple.countWeightedTuples(data);

//		BinarySplitGP binarySplit = new BinarySplitGP(dispersion,totalTuples, noCls);
		getSplit().init(totalTuples, noCls);
		
		Histogram allSegmentSet [][] = new Histogram[noAttr][];
		SampleAttrClass allAttrClassSet[][] = new SampleAttrClass[noAttr][];
		double allLowerBounds[][] = new double[noAttr][];

		for(int i = 0 ; i < noAttr; i++){
			allAttrClassSet[i] = getSampleAttrClass(data, i);
			allSegmentSet[i]  = SegGen(allAttrClassSet[i], noCls);

			GlobalParam.addNoEndPtIntervals(allSegmentSet[i].length);

			allLowerBounds[i] = getSplit().preProcess(allSegmentSet[i]);
			if(allSegmentSet[i].length == 1) continue;

			double localEnt = getSplit().getEnt();

			if(splitData.getDispersion() - localEnt > GlobalParam.DOUBLE_PRECISION){
				splitData.setDispersion(localEnt);
				splitData.setSplitPt(getSplit().getSplit());
				splitData.setAttrNum(i);
			}
		}

		for(int i = 0 ; i < noAttr; i++){

			getSplit().run(allSegmentSet[i], allLowerBounds[i], allAttrClassSet[i], splitData.getDispersion());
			if(!getSplit().isPruned()){
				double localEnt = getSplit().getEnt();
				if(splitData.getDispersion() - localEnt > GlobalParam.DOUBLE_PRECISION){
					splitData.setDispersion(localEnt);
					splitData.setSplitPt(getSplit().getSplit());
					splitData.setAttrNum(i);
				}
			}
		}

		log.debug("Best Split: " + splitData.getAttrNum() + ", " + splitData.getSplitPt() + ", " + splitData.getDispersion());

		return splitData;

	}

	protected BinarySplitGP getSplit(){
		return (BinarySplitGP) super.getSplit();
	}


}

