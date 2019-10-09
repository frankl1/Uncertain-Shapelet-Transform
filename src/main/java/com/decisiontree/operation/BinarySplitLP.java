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

import java.util.ArrayList;
import java.util.Arrays;

import com.decisiontree.data.PointAttrClass;
import com.decisiontree.data.SampleAttrClass;
import com.decisiontree.eval.DispersionMeasure;
import com.decisiontree.param.GlobalParam;

/**
 *
 * BinarySplitLP - Finds the best binary split point of an attribute using the local pruning technique.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class BinarySplitLP extends BinarySplit{

	protected double tempOptimal;
	protected boolean pruned;

	public BinarySplitLP(String dispersionStr) {
		super(dispersionStr);
	}

	public BinarySplitLP(DispersionMeasure dispersion) {
		super(dispersion);
	}

	public double [] preProcess(Histogram [] segments){
		int noSegments = segments.length;

		double left[] = new double[noCls];
		double right[] = new double[noCls];
		for(int i = 0; i < noCls; i++){
			left[i] = 0.0;
			for(int j = 0; j < noSegments; j++)
				right[i] += segments[j].getCls(i);
		}

		int min = -1;
		double minEnt = Double.POSITIVE_INFINITY;

		double [] lowerBoundSet = new double[noSegments];

		for(int i = 0 ; i < noSegments; i++){

			if(segments[i].mulCls()){
				GlobalParam.incrNoHeterIntervals();
				double [] region = segments[i].getAllCls();

				lowerBoundSet[i] = dispersionMeasure.findLowerBound(left, right, region);
			}

			if(i == noSegments -1) break;

			for(int j = 0; j  < noCls; j++){
				left[j] += segments[i].getCls(j);
				right[j] -= segments[i].getCls(j);
			}

			double avgEnt = dispersionMeasure.averageDispersion(left, right);
			if(minEnt - avgEnt >= GlobalParam.DOUBLE_PRECISION){
				min = i;
				minEnt = avgEnt;
			}
		}

		if(min != -1)
			localOptimal = segments[min].getEnd();
		threshold = minEnt;
		return lowerBoundSet;

	}

	protected boolean [] findUnprunedRegion(Histogram [] segments, double [] lowerBounds){

		boolean [] unpruned = new boolean[segments.length];
		for(int i = 0; i < segments.length;i++){
			if(segments[i].mulCls() && threshold - lowerBounds[i] >= GlobalParam.DOUBLE_PRECISION){
				GlobalParam.incrNoUnpIntervals();
				unpruned[i] = true;
				setPruned(false);
			}
			else unpruned[i] = false;
		}

		return unpruned;
	}

	public void run(Histogram [] segmentSet, SampleAttrClass [] attrClassSet){


		double lowerBounds[] = preProcess(segmentSet);
		boolean unpruned[] = findUnprunedRegion(segmentSet, lowerBounds);

		if(isPruned()) return;

		double left[] = new double[noCls];
		double right[] = new double[noCls];

		for(int i = 0 ; i < segmentSet.length; i++){
			for(int j = 0 ; j < noCls; j++){
				right[j] += segmentSet[i].getCls(j);
			}

		}

		for(int i = 0; i < segmentSet.length; i++){

			if(unpruned[i]){
				double bestEnt = findEntInRegion(segmentSet[i],attrClassSet,left,right);
				if(threshold - bestEnt > GlobalParam.DOUBLE_PRECISION){
					threshold = bestEnt;
					localOptimal = tempOptimal;
				}
			}
			for(int j = 0 ; j < noCls; j++){
				left[j] += segmentSet[i].getCls(j);
				right[j] -= segmentSet[i].getCls(j);
			}


		}
	}



	@Override
	public double getEnt(){
		return threshold;
	}

	@Override
	public double getSplit(){
		return localOptimal;
	}


	protected Histogram [] miniSegGen(PointAttrClass [] pointAttrClassSet){

		int noTuples = pointAttrClassSet.length;

		int count =0;
		Histogram tempSegments[] = new Histogram[noTuples];
		tempSegments[0] = new Histogram(noCls);
		tempSegments[0].setHist(pointAttrClassSet[0].getValue(), pointAttrClassSet[0].getCls(), pointAttrClassSet[0].getWeight());

		for( int i =1; i < noTuples; i++){
			if(pointAttrClassSet[i].getValue() == tempSegments[count].getValue() || (!tempSegments[count].mulCls() && pointAttrClassSet[i].getCls() == tempSegments[count].singleCls()))
				tempSegments[count].setHist(pointAttrClassSet[i].getValue(), pointAttrClassSet[i].getCls(), pointAttrClassSet[i].getWeight());
			else{
				tempSegments[++count] = new Histogram(noCls);
				tempSegments[count].setHist(pointAttrClassSet[i].getValue(), pointAttrClassSet[i].getCls(), pointAttrClassSet[i].getWeight());
			}
		}
		int noSegments = count+1;
		Histogram segments[] = new Histogram[noSegments];
		for(int i = 0; i < noSegments; i++){
			segments[i] = tempSegments[i];
		}
		return segments;
	}


	protected int binarySearch(SampleAttrClass [] attrClassSet, double key){

		int low = 0;
		int high = attrClassSet.length-1;

		int mid = 0;
		while(high >= low){
			mid = (low+high)/2;
			double value = attrClassSet[mid].getStart();
			if(value < key) low = mid+1;
			else if( value == key) return mid;
			else high = mid-1;
		}

		return low;

	}

	protected double findEntInRegion(Histogram hist, SampleAttrClass [] attrClassSet,  double [] left, double [] right){

		double start = hist.getStart();
		double end = hist.getEnd();

		double frac = 0;

		ArrayList<PointAttrClass> nList = new ArrayList<PointAttrClass>((int)(hist.size()));
		int pos = binarySearch(attrClassSet, end);
		int pos2, pos3;

		for(int i = 0; i < pos; i++){
			if(attrClassSet[i].getEnd() <= start) continue;
			pos2 = attrClassSet[i].getEqualOrLarger(start);
			pos3 = attrClassSet[i].getNearSample(end);

			if(pos2 < attrClassSet[i].getStartPos()) pos2 = attrClassSet[i].getStartPos();
			if(pos3 > attrClassSet[i].getEndPos()) pos3 = attrClassSet[i].getEndPos();
			// assume startPos < endPos;

			for(int j =pos2 ; j <= pos3; j++){
				frac = attrClassSet[i].getFrac(j-1,j);
				nList.add( new PointAttrClass(attrClassSet[i].getSample(j),attrClassSet[i].getCls(), frac * attrClassSet[i].getWeight()));
			}
		}

		PointAttrClass nArray[] = new PointAttrClass[nList.size()];
		nList.toArray(nArray);

		Arrays.sort(nArray);

		Histogram miniSegmentSet[] = miniSegGen(nArray);

		double [] tempLeft = new double[noCls];
	       	double [] tempRight = new double[noCls];

		for(int i = 0 ; i < noCls; i++){
			tempLeft[i] = left[i];
			tempRight[i] = right[i];
		}
		double split = start;

		double minEnt = Double.POSITIVE_INFINITY;

		for(int i = 0 ; i <miniSegmentSet.length; i++){
			split = miniSegmentSet[i].getValue();
			for(int j = 0; j< noCls; j++){
				tempLeft[j] += miniSegmentSet[i].getCls(j);
				tempRight[j] -= miniSegmentSet[i].getCls(j);
			}
			double regionEnt = dispersionMeasure.averageDispersion(tempLeft, tempRight);
			if(regionEnt < minEnt){
				minEnt = regionEnt;
				tempOptimal = split;
			}
		}

		GlobalParam.addNoEntOnSamples(miniSegmentSet.length);
		return minEnt;


	}

	@Deprecated
	public double findLowerBound(double [] left, double [] right, double [] region){

		double leftSize = 0.0, rightSIze = 0.0;
		for(int i = 0 ; i < noCls; i++){
			leftSize += left[i];
			rightSIze += right[i] - region[i];
		}

		double ent = 0.0;
		for(int i = 0 ; i < noCls; i++){

			double leftT = left[i] + region[i];
			double logValueL = 0;
			if( leftT > 1E-12){
				logValueL = Math.log(leftT/(leftSize + region[i]))/Math.log(2.0);
			}
			double sumLeft = left[i] * logValueL;

			double logValueR = 0;
			if(right[i] > 1E-12)
				logValueR = Math.log(right[i]/(rightSIze + region[i]))/Math.log(2.0);
			double sumRight = (right[i] - region[i]) * logValueR;


			double remain = 0.0;
			if(region[i] > 1E-12){
				if(logValueL > logValueR)
					remain = region[i] * logValueL;
				else remain = region[i] * logValueR;
			}

			ent +=  (sumLeft + sumRight + remain);
		}

		return -1.0 * ent/noTuples;
	}

	@Override
	public void init(double noTuples, int noCls){
		super.init(noTuples, noCls);
		tempOptimal = Double.POSITIVE_INFINITY;
		this.pruned = true;
	}

	public boolean isPruned(){
		return pruned;
	}

	private void setPruned(boolean pruned) {
		this.pruned = pruned;
	}
}
