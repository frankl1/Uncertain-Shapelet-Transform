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

/**
 * 
 * AbstractSplitSearch (Abstract class) - Finds the best split point for a set of data. 
 * The class contains implementation of some common methods.
 *
 * @author Smith Tsang
 * @since 0.9
 *
 */
public abstract class AbstractSplitSearch implements SplitSearch {

//	private Dispersion dispersion;
	private Split split;
	protected int noCls;
	
	
	public AbstractSplitSearch(Split split) {
//		this.dispersion = dispersion;
		this.split = split;
	}

	public void setSplit(Split split) {
		this.split = split;
	}

	protected Split getSplit(){
		return split;
	}
	
//	public Dispersion getDispersion() {
//		return dispersion;
//	}
//
//	public void setDispersion(Dispersion dispersion) {
//		this.dispersion = dispersion;
//	}

	@Override
	public double findDispersion(double [] dist, double distSize) {
		return getSplit().getDispersionMeasure().getDispersion(dist, distSize);
	}




}
