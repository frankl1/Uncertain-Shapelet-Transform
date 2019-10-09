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
package com.decisiontree.convertor;

/**
 *
 * SampleByteArrayConvertor
 *
 * @author Smith Tsang
 * @since 0.9
 *
 */
public class SampleByteArrayConvertor {

	public static byte[] doubleToByteArray(double d) {

		byte[] b = new byte[8];
		long l = Double.doubleToRawLongBits(d);
		for (int i = 0; i < 8; i++) {
			b[i] = new Long(l).byteValue();
			l = l >> 8;
		}
		return b;
	}


	public static double byteArrayToDouble(byte[] b) {

		long accum = 0;
		int i = 0;
		for (int shiftBy = 0; shiftBy < 64; shiftBy += 8) {
			accum |= ((long) (b[i] & 0xff)) << shiftBy;
			i++;
		}
		return Double.longBitsToDouble(accum);
	}



}
