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
package com.decisiontree.datagen;

import com.decisiontree.convertor.SampleByteArrayConvertor;
import com.decisiontree.data.Sample;

/**
 *
 * SampleDataTransformer - transform readable sample data file to byte code
 * (or vice versa).
 * We allow each sample file to store each a particular distribution
 * or all distributions. In our program, we use byte code sample file for efficiency.
 *
 * <p>Expected Sample Data Format for a file to store a single distribution:<br>
 * Each line contains two number separated by comma, the first number is the
 * sample value while the second number is the cummulative distribution of
 * the sample. The following is an example containing 5 samples.
 * <br>
 * <code>
 * 1.5,0.2
 * 1.7,0.3
 * 1.8,0.5
 * 2.0,0.7
 * 2.1,1.0
 * </code>
 * <p>Expected Sample Data Format for a file to store all distributions<br>
 * Sample as a single distribution, but the tuple number (#i) and attribute number
 * (#j) would be marked as T#iA#j for each sample distributions. The following
 * is an example containing 2 tuples with 2 attributes.<br>
 * <code>
 * T1A1
 * // samples...
 * T1A2
 * // samples...
 * T2A1
 * // samples...
 * T2A2
 * // samples...
 * </code>
 *
 * @author Smith Tsang
 * @since 0.9
 *
 */
public class SampleDataTransformer {



	/**
	 *
	 * @param sample
	 * @return
	 */
	public byte[][] sampleToByteArray(Sample sample){

		byte[][] byteArray = new byte[2][];
		byteArray[0] = SampleByteArrayConvertor.doubleToByteArray(sample.getValue());
		byteArray[1] = SampleByteArrayConvertor.doubleToByteArray(sample.getCDist());
		return byteArray;

	}

	public Sample byteArrayToSample(byte[][] byteArray){
		return byteCodeToSample(byteArray[0],byteArray[1]);
	}

	public Sample byteCodeToSample(byte[] value, byte[] cumDist){
		Sample sample = new Sample(SampleByteArrayConvertor.byteArrayToDouble(value),
				SampleByteArrayConvertor.byteArrayToDouble(cumDist));
		return sample;
	}


}
