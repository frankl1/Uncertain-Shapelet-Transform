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
 * BinarySplitES - Finds the best binary split point of an attribute using the end-pt sampling technique.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class BinarySplitES extends BinarySplitGP {

	public BinarySplitES(String dispersionStr){
		super(dispersionStr);
	}
	
	public BinarySplitES(DispersionMeasure dispersion) {
		super(dispersion);
	}

	@Override
	public double[] preProcess(Histogram[] segmentSet) {
		int noSegments = segmentSet.length;

		double left[] = new double[noCls];
		double right[] = new double[noCls];
		for (int i = 0; i < noCls; i++) {
			left[i] = 0.0;
			for (int j = 0; j < noSegments; j++)
				right[i] += segmentSet[j].getCls(i);
		}

		int min = -1;
		double minEnt = Double.POSITIVE_INFINITY;

		double[] lower = new double[noSegments];

		for (int i = 0; i < noSegments; i++) {

			if (segmentSet[i].mulCls()) {
				double[] region = segmentSet[i].getAllCls();
				GlobalParam.incrNoHeterIntervals();
				lower[i] = dispersionMeasure.findLowerBound(left, right, region);
			}

			if (i == noSegments - 1)
				break;

			for (int j = 0; j < noCls; j++) {
				left[j] += segmentSet[i].getCls(j);
				right[j] -= segmentSet[i].getCls(j);
			}

			double avgEnt = dispersionMeasure.averageDispersion(left, right);
			if (minEnt - avgEnt >= 1E-12) {
				min = i;
				minEnt = avgEnt;
			}
		}

		if (min != -1)
			localOptimal = segmentSet[min].getEnd();
		threshold = minEnt;

		return lower;

	}

	public void run(Histogram[] segmentSet, double[] endPtSet, double[] lowerBounds,
			SampleAttrClass[] p, double threshold) {

		pruned = true;
		this.threshold = threshold;
		boolean unpruned[] = findUnprunedRegion(segmentSet, lowerBounds);

		if (pruned)
			return;

		double left[] = new double[noCls];
		double right[] = new double[noCls];

		for (int i = 0; i < segmentSet.length; i++) {
			for (int j = 0; j < noCls; j++) {
				right[j] += segmentSet[i].getCls(j);
			}

		}

		for (int i = 0; i < segmentSet.length; i++) {

			if (unpruned[i]) {
				double bestEnt = secondLevelPruning(segmentSet[i], endPtSet, p, left,
						right);
				if (threshold - bestEnt > 1E-12) {
					this.threshold = bestEnt;
					localOptimal = tempOptimal;
				}

			}
			for (int j = 0; j < noCls; j++) {
				left[j] += segmentSet[i].getCls(j);
				right[j] -= segmentSet[i].getCls(j);
			}

		}

	}

	protected int binarySearch(double[] endPtSet, double key) {
		return binarySearch(endPtSet, key, 0, endPtSet.length - 1);
	}

	protected int binarySearch(double[] endPtSet, double key, int low, int high) {
		/*
		 * int low = 0; int high = end_pt.length-1;
		 */
		int mid = 0;
		while (high >= low) {
			mid = (low + high) / 2;
			double value = endPtSet[mid];
			if (value < key)
				low = mid + 1;
			else if (value == key)
				return mid;
			else
				high = mid - 1;
		}

		return low;

	}

	protected double secondLevelPruning(Histogram segment, double[] endPtSet,
			SampleAttrClass[] attrClassSet, double[] left, double[] right) {
		double start = segment.getStart();
		double end = segment.getEnd();

		int startEndPt = binarySearch(endPtSet, start);
		int endEndPt = binarySearch(endPtSet, end, startEndPt, endPtSet.length - 1);

		if (endEndPt > startEndPt) {

			int noSegment = endEndPt - startEndPt;
			Histogram[] segmentSet = new Histogram[noSegment];

			int temp = 0;
			int endTuple = binarySearch(attrClassSet, end);

			for (int i = 0; i < noSegment; i++) {
				segmentSet[i] = new Histogram(noCls, endPtSet[startEndPt + i],
						endPtSet[startEndPt + i + 1]);
			}

			int currPos = -1 , prevPos = -1, startPos, endPos;
			double nextSampleValue;
			for (int i = 0; i < endTuple; i++) {

				if (attrClassSet[i].getEnd() < start)
					continue;

				for (; temp < noSegment
						&& attrClassSet[i].getStart() > segmentSet[temp].getEnd(); temp++)
					;
				if (temp >= noSegment)
					break;

				startPos = attrClassSet[i].getStartPos();
				endPos = attrClassSet[i].getEndPos();

				currPos = attrClassSet[i].getNearSample(segmentSet[temp].getStart());
				if (currPos >= endPos)
					continue;
				prevPos = currPos; // prevPos

				nextSampleValue = attrClassSet[i].getSampleValue(currPos + 1);
				for (int rtemp = temp; rtemp < noSegment
						&& attrClassSet[i].getEnd() > segmentSet[rtemp].getStart(); rtemp++) {
					if (segmentSet[rtemp].getEnd() < nextSampleValue)
						continue;
					currPos = attrClassSet[i].getNearSample(currPos + 1, segmentSet[rtemp]
							.getEnd());

					double frac = 0.0;
					if (currPos <= startPos)
						frac = 0.0;
					else if (currPos >= endPos)
						frac = attrClassSet[i].getFrac(prevPos, endPos);
					else
						frac = attrClassSet[i].getFrac(prevPos, currPos);

					if (frac > 1E-12)
						segmentSet[rtemp].addCls(attrClassSet[i].getCls(), frac
								* attrClassSet[i].getWeight());

					if (currPos >= endPos || frac >= (1.0 - 1E-12))
						break;
					nextSampleValue = attrClassSet[i].getSampleValue(currPos + 1);
					prevPos = currPos;
				}

			}

			double tempLeft[] = new double[noCls];
			double tempRight[] = new double[noCls];

			for (int i = 0; i < noCls; i++) {
				tempLeft[i] = left[i];
				tempRight[i] = right[i];
			}

			double tempLowerBound, tempEnt;
			double tempThres = Double.POSITIVE_INFINITY;
			double optimal = 0;
			int prevHomoSegNum = -1;
			int presentSegNum = -1;

			boolean mulCls = false;
			for (int i = 0; i < noSegment; i++) {

				if (segmentSet[i].empty())
					continue;

				mulCls = false;
				if (segmentSet[i].mulCls()) {
					tempLowerBound = dispersionMeasure.findLowerBound(tempLeft, tempRight, segmentSet[i].getAllCls());
					GlobalParam.incrNoEndPtSampLBs();
					if (threshold - tempLowerBound > 1E-14 && tempThres - tempLowerBound > 1E-14) {
						GlobalParam.incrNoUnpEndPtSampLBs();
						tempEnt = findEntInRegion(segmentSet[i], attrClassSet, tempLeft,
								tempRight);
						if (tempThres - tempEnt > 1E-14 && threshold - tempEnt > 1E-14) {
							tempThres = tempEnt;
							optimal = tempOptimal;
						}
					}
					prevHomoSegNum = -1;
					mulCls = true;
				} else {
					presentSegNum = segmentSet[i].singleCls();
					if (presentSegNum != prevHomoSegNum)
						prevHomoSegNum = presentSegNum;
				}

				if (i == noSegment - 1)
					break;
				for (int j = 0; j < noCls; j++) {
					tempLeft[j] += segmentSet[i].getCls(j);
					tempRight[j] -= segmentSet[i].getCls(j);
				}

				if (presentSegNum == prevHomoSegNum && !mulCls) {
					double avgEnt = dispersionMeasure.averageDispersion(tempLeft, tempRight);

					GlobalParam.incrNoEndPtSampIntervals();
					if (tempThres - avgEnt > 1E-14
							&& threshold - avgEnt > 1E-14) {
						optimal = segmentSet[i].getEnd();
						tempThres = avgEnt;
					}
				}

			}

			tempOptimal = optimal;
			return tempThres;

		} else
			return findEntInRegion(segment, attrClassSet, left, right);
	}

	@Override
	protected double findEntInRegion(Histogram segment, SampleAttrClass[] attrClassSet,
			double[] left, double[] right) {

		double start = segment.getStart();
		double end = segment.getEnd();

		double frac = 0;
		ArrayList<PointAttrClass> pointAttrClassList = new ArrayList<PointAttrClass>(
				(int) (segment.size()));

		int endPos = binarySearch(attrClassSet, end);
		int tempStartPos, tempEndPos;

		double segSize = 0;
		for (int i = 0; i < endPos; i++) {
			if (attrClassSet[i].getEnd() <= start)
				continue;
			tempStartPos = attrClassSet[i].getEqualOrLarger(start);
			tempEndPos = attrClassSet[i].getNearSample(end);

			if (tempStartPos < attrClassSet[i].getStartPos())
				tempStartPos = attrClassSet[i].getStartPos();
			if (tempEndPos > attrClassSet[i].getEndPos())
				tempEndPos = attrClassSet[i].getEndPos();
			// assume startPos < endPos;

			for (int j = tempStartPos; j <= tempEndPos; j++) {
				frac = attrClassSet[i].getFrac(j - 1, j);
				segSize += frac * attrClassSet[i].getWeight();
				pointAttrClassList.add(new PointAttrClass(attrClassSet[i].getSample(j), attrClassSet[i].getCls(),
						frac * attrClassSet[i].getWeight()));
			}
		}


		PointAttrClass pointAttrClassSet[] = new PointAttrClass[pointAttrClassList.size()];
		pointAttrClassList.toArray(pointAttrClassSet);
		Arrays.sort(pointAttrClassSet);

		Histogram miniSegmentSet[] = miniSegGen(pointAttrClassSet);

		double[] tempLeft = new double[noCls];
		double[] tempRight = new double[noCls];

		for (int i = 0; i < noCls; i++) {
			tempLeft[i] = left[i];
			tempRight[i] = right[i];
		}
		double split = start;

		double minEnt = Double.POSITIVE_INFINITY;

		for (int i = 0; i < miniSegmentSet.length; i++) {
			split = miniSegmentSet[i].getValue();
			for (int j = 0; j < noCls; j++) {
				tempLeft[j] += miniSegmentSet[i].getCls(j);
				tempRight[j] -= miniSegmentSet[i].getCls(j);
			}
			double regionEnt = dispersionMeasure.averageDispersion(tempLeft, tempRight);

			if (minEnt - regionEnt > 1E-14) {
				minEnt = regionEnt;
				tempOptimal = split;
			}
		}

		GlobalParam.addNoEntOnSamples(miniSegmentSet.length);

		return minEnt;

	}



}
