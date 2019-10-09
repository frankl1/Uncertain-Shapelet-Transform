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
package com.decisiontree.app;


/**
*
* GlobalProp - store for properties name of the property file
*
* @author Smith Tsang
* @since 0.9
*
*/
public class GlobalProp {
	public static final String MODE = "udt.mode";
	public static final String DATASET = "udt.dataset";
	public static final String TESTSET = "udt.testset";
	public static final String PROPERTY = "udt.property";
	public static final String NOSAMPLES = "udt.nosamples";
	public static final String ALGORITHM = "udt.algorithm";
	public static final String TYPE = "udt.type";
	public static final String NODESIZE ="udt.nodesize";
	public static final String PURITY = "udt.purity";
	public static final String TREE = "udt.tree";
	public static final String WIDTH = "udt.width";
	public static final String SEED = "udt.seed";
	public static final String TRIAL = "udt.trial";
	public static final String SAVE = "udt.save";
}
