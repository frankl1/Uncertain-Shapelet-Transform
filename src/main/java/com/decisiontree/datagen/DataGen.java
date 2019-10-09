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

/**
 * 
 * DataGen (Interface) - Generates uncertain data from point-valued data
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public interface DataGen {

    public static final String RANGE_GEN = "RANGE";
    public static final String SAMPLE_GEN = "SAMPLE";

	/**
	 * Generate the uncertain dataset by a given dataset file name with a given interval width and store the generate data
	 * @param input the dataset file
	 * @param width the interval width list of the attributes of the tuples generated
	 */
	public void storeGeneratedData(String input, double [] width);
	
	/**
	 * Generate the uncertain dataset by given training and testing dataset files name with a given interval width 
	 * and store the generate dataset.
	 * @param training the training dataset file
	 * @param testing the testing dataset file
	 * @param width the interval width list of the attributes of the tuples generated
	 */
	public void storeGeneratedDataWithTest(String training, String testing, double [] width);

}
