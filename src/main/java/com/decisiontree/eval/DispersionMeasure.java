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
package com.decisiontree.eval;

/**
 *
 * DispersionMeasure (Interface)
 *
 * @author Smith Tsang
 * @since 0.9
 *
 */
public interface DispersionMeasure {

	public static final String ENTROPY = "ENTROPY";

	public void init(double noTuples, int noCls);
	
	public double getDispersion(double [] dist, double distSize);

	public double averageDispersion(double [] left, double [] right);

	public double findLowerBound(double [] left, double [] right, double [] region);



}
