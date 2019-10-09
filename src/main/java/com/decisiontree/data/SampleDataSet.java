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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.decisiontree.convertor.SampleByteArrayConvertor;
import com.decisiontree.param.GlobalParam;

/**
 *
 * SampleDataSet - Stores the database information for interval-valued dataset with distribution represented by samples.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class SampleDataSet extends RangeDataSet {

	public static Logger log = Logger.getLogger(SampleDataSet.class);

	private int noSamples;

	public SampleDataSet(int noCls, int noAttr, int noSamples){
		super(noCls, noAttr);
		setNoSamples(noSamples);
	}

	public SampleDataSet(List<Tuple> data, int noCls, int noAttr, int noSamples){
		super(data, noCls, noAttr);
		setNoSamples(noSamples);
	}

	public SampleDataSet(String input, int noCls, int noAttr, int noSamples) {
		super(input, noCls, noAttr);
		setNoSamples(noSamples);
	}


	public int getNoSamples() {
		return noSamples;
	}

	public void setNoSamples(int noSamples){
		this.noSamples = noSamples;
	}

	@Deprecated
	private double byteToDouble(byte[] b) {

		long accum = 0;
		int i = 0;
		for (int shiftBy = 0; shiftBy < 64; shiftBy += 8) {
			accum |= ((long) (b[i] & 0xff)) << shiftBy;
			i++;
		}
		return Double.longBitsToDouble(accum);
	}

	private String getFileName(String name, int tupleNum, int attrNum) {
		StringBuffer sb = new StringBuffer(name);
		sb.append(GlobalParam.SAMPLE_PATH);
		sb.append(GlobalParam.SAMPLE_TUPLE);
		sb.append(tupleNum);
		sb.append(GlobalParam.SAMPLE_ATTR);
		sb.append(attrNum);
		return sb.toString();
	}

	public Sample[] getSamples(int tupleNum, int attrNum) {

		Sample [] samples = new Sample[getNoSamples()];
		BufferedInputStream reader = null;
		try {
			double a1, a2;
			reader = new BufferedInputStream(
					new FileInputStream(
							getFileName(getName(), tupleNum, attrNum)));
			byte[] b = new byte[8];
			for (int i = 0; i < noSamples; i++) {
				reader.read(b, 0, 8);
				a1 = SampleByteArrayConvertor.byteArrayToDouble(b);
				reader.read(b, 0, 8);
				a2 = SampleByteArrayConvertor.byteArrayToDouble(b);
				samples[i] = new Sample(a1, a2);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("No dataset sample files, file cannot access or wrong sample number. Please try again!");
			System.exit(1);
		}finally{
				try {
					if(reader != null) reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return samples;
//
    }

}
