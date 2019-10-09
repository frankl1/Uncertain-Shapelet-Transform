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

import java.util.Arrays;

import org.apache.log4j.Logger;

/**
 * 
 * SampleAttribute -Stores a range data attribute with distribution represented by samples.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class SampleAttribute extends RangeAttribute {
	
	public static Logger log = Logger.getLogger(SampleAttribute.class);

	private Sample[] samples;

	private double curFrac = 1.0;

	private int startPos;

	private int endPos;

	private int noSamples;

	public SampleAttribute(double start, double end, 
			Sample[] samples, boolean averaging) {
		this(start, end, samples);
		if(averaging) setValue(getAverage());
	}
	
	public SampleAttribute(double start, double end, Sample[] samples) {
		super(start, end);
		this.samples = samples;
		noSamples = samples.length;
		setStartPos(-1);
		setEndPos(noSamples-1);
	}

	public SampleAttribute(double start, double end, double curStart,
			double curEnd, Sample[] samples) {
		this(start, end, samples);
		setStart(curStart);
		setEnd(curEnd);
		setCurFrac();
	}

	public SampleAttribute(SampleAttribute p, double curStart, double curEnd) {
		this(p.getAbsStart(), p.getAbsEnd(), curStart, curEnd, p.getSamples());
	}

	@Override
	public double getFrac(double left, double right) {

		Sample temp = new Sample(0, 0);
		if (left == getStart() && right == getEnd())
			return 1.0;

		temp.setValue(left);
		int pos1 = Arrays.binarySearch(samples, temp);

		double startFrac = 0.0, endFrac = 1.0;
		if (pos1 < 0) {
			pos1 = (pos1 + 1) * -1;
			if (pos1 != 0)
				startFrac = samples[pos1 - 1].getCDist();
		} else
			startFrac = samples[pos1].getCDist();

		temp.setValue(right);
		int pos2 = Arrays.binarySearch(samples, pos1, noSamples, temp);

		if (pos2 < 0) {
			pos2 = (pos2 + 1) * -1;
			if (pos2 != 0)
				endFrac = samples[pos2 - 1].getCDist();
			else
				endFrac = 0.0;
		} else
			endFrac = samples[pos2].getCDist();

		if (startPos >= 0)
			if (startFrac < samples[startPos].getCDist())
				startFrac = samples[startPos].getCDist();
		if (endFrac > samples[endPos].getCDist())
			endFrac = samples[endPos].getCDist();
		double frac = (endFrac - startFrac) / getCurFrac();
		return frac;
	}

	public int getStartPos() {
		return startPos;
	}

	public int getEndPos() {
		return endPos;
	}

	public void setStartPos(int startPos) {
		this.startPos = startPos;
	}

	public void setEndPos(int endPos) {
		this.endPos = endPos;
	}

	@Override
	public double getCurFrac() {
		return curFrac;
	}

	public void setCurFrac(double frac) {
		this.curFrac = frac;
	}

	public int binarySearch(double key) {

		int low = 0;
		int high = noSamples - 1;

		int mid = 0;
		while (high >= low) {
			mid = (low + high) / 2;
			double value = samples[mid].getValue();
			if (value < key)
				low = mid + 1;
			else if (value == key)
				return mid;
			else
				high = mid - 1;
		}

		return low;

	}

	public int binarySearch(double key, int pos1) {

		int low = pos1;
		int high = noSamples - 1;

		int mid = 0;
		while (high >= low) {
			mid = (low + high) / 2;
			double value = samples[mid].getValue();
			if (value < key)
				low = mid + 1;
			else if (value == key)
				return mid;
			else
				high = mid - 1;
		}

		return low;

	}

	public void setCurFrac() {

		if (isOriginal()) {
			curFrac = 1.0;
			startPos = -1;
			endPos = noSamples - 1;
		}

		int pos1 = -1;
		if (start - samples[0].getValue() > 1E-15) {
			pos1 = binarySearch(start);
			if (samples[pos1].getValue() > start)
				pos1--;
		} else if (Math.abs(start - samples[0].getValue()) < 1E-15) {
			pos1 = 0;
		}

		int pos2 = noSamples - 1;
		if (samples[noSamples - 1].getValue() - end > 1E-15) {
			pos2 = binarySearch(end);
			if (samples[pos2].getValue() > end)
				pos2--;
		} else if (Math.abs(samples[0].getValue() - end) < 1E-15) {
			pos2 = noSamples - 1;
		}

		curFrac = getSampleFrac(pos1, pos2);

		setStartPos(pos1);
		setEndPos(pos2);
	}

	public double getSampleFrac(int pos1, int pos2) {

		double left = 0, right = 1;
		if (pos1 < 0) {
			left = 0;
		} else if (pos1 >= noSamples) {
			left = 1;
		} else
			left = samples[pos1].getCDist();

		if (pos2 < 0) {
			right = 0;
		} else if (pos2 >= noSamples) {
			right = 1;
		} else
			right = samples[pos2].getCDist();
		return right - left;
	}

	public Sample getSample(int no) {
		if (no >= noSamples)
			return null;
		return samples[no];
	}

	public int getEqualOrLarger(double value) {
		Sample temp = new Sample(0, 0);
		temp.setValue(value);
		int pos1 = Arrays.binarySearch(samples, temp);

		if (pos1 < 0)
			return -1 * (pos1 + 1);

		if (samples[pos1].getValue() == value)
			pos1++;
		return pos1;
	}

	public int getNearSample(double value) {

		if (value >= getEnd())
			return endPos;
		if (value < getStart())
			return startPos;

		Sample temp = new Sample(0, 0);
		temp.setValue(value);
		int pos1 = Arrays.binarySearch(samples, temp);

		if (pos1 < 0)
			return -1 * (pos1 + 1) - 1;

		if (pos1 > endPos)
			return endPos;
		if (pos1 < startPos)
			return startPos;

		return pos1;
	}

	public int getNearSample(int start, double value) {

		if (value >= getEnd())
			return endPos;
		Sample temp = new Sample(0, 0);
		temp.setValue(value);
		int pos1 = Arrays.binarySearch(samples, start, noSamples, temp);
		if (pos1 < 0)
			return -1 * (pos1 + 1) - 1;

		if (pos1 > endPos)
			return endPos;
		if (pos1 < startPos)
			return startPos;

		return pos1;

	}

	public double getFrac(int startPos, int endPos) {
		if (startPos < 0)
			return samples[endPos].getCDist() / getCurFrac();
		return (samples[endPos].getCDist() - samples[startPos].getCDist())
				/ getCurFrac();
	}

	public double getSampleValue(int pos) {
		return samples[pos].getValue();
	}

	public double getCDist(double value) {
		Sample temp = new Sample(0, 0);
		temp.setValue(value);
		int pos1 = Arrays.binarySearch(samples, temp);

		if (pos1 < 0)
			pos1 = (pos1 + 1) * -1 - 1;

		return samples[pos1].getCDist();
	}

	public double getAverage() {

//		log.info("average");
		double mean = 0.0;
		double prevCDist = 0.0, cDist = 0.0;
		for(int i =0 ; i < noSamples; i++){
			cDist = samples[i].getCDist();
			mean += samples[i].getValue() * (cDist - prevCDist);
			prevCDist = cDist;
		}
		return mean;
//		final double midist = 0.5;
//
//		int start = 0, end = noSamples - 1, mid;
//		while (start < end) {
//			mid = (start + end) / 2;
//			if (samples[mid].getCDist() < midist)
//				start = mid + 1;
//			else
//				end = mid;
//		}
//		if (start == noSamples)
//			return samples[noSamples - 1].getValue();
//		else
//			return samples[start].getValue();
	}

	public Sample[] getSamples() {
		if (samples == null) {
			log.error("No Samples initialized");
			return null;
		}
		return samples;
	}

	@Override
	public int compareTo(Attribute attr) {
		double diff = getStart() - ((SampleAttribute) attr).getStart();
		if (diff > 0)
			return 1;
		else if (diff == 0)
			return 0;
		else
			return -1;
	}

	public static SampleAttribute cutCopy(SampleAttribute p, double curStart,
			double curEnd) {
		SampleAttribute newP = new SampleAttribute(p, curStart, curEnd);
		return newP;
	}

	public void setNoSample(int noSample) {
		this.noSamples = noSample;
	}

	public int getNoSample() {
		return noSamples;
	}

}
