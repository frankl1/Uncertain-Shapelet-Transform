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
package com.decisiontree.function;

import com.decisiontree.build.PointClassification;
import com.decisiontree.data.PointDataSet;
import com.decisiontree.data.RangeDataSetInit;
import com.decisiontree.operation.SplitSearch;

/**
 * 
 * RangeAvgDecisionTree - builds a decision tree for given interval value dataset files with averaging techniques.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class RangeAvgDecisionTree extends PointDecisionTree {

	public RangeAvgDecisionTree(SplitSearch splitSearch, double nodeSize, double purityThreshold) {
		super(splitSearch, nodeSize, purityThreshold);
	}

	public RangeAvgDecisionTree(SplitSearch splitSearch) {
		super(splitSearch);
	}

	@Override
	protected PointDataSet generateDataSet(String training, String nameFile) {
		RangeDataSetInit init = new RangeDataSetInit(training, nameFile);
		return init.getDataSet();
	}
	
	@Override
	public double crossFold(String training, String nameFile) {

		PointDataSet dataSet = generateDataSet(training, nameFile);
		
		PointClassification classification = new PointClassification(dataSet, splitSearch);
		return classification.crossAllFold(nodeSize, purity);
		
	}

}
