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
import com.decisiontree.data.SampleDataSetInit;
import com.decisiontree.operation.SplitSearch;
import com.decisiontree.param.GlobalParam;

/**
 * 
 * SampleAvgDecisionTree - builds a decision tree for given interval-valued sampled-dstributed dataset 
 * 						   files with averaging techniques.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class SampleAvgDecisionTree extends PointDecisionTree {
	
	
	private int noSamples = GlobalParam.DEFAULT_NO_SAMPLES;
	
	public SampleAvgDecisionTree(SplitSearch splitSearch){
		super(splitSearch);
	}
	
	public SampleAvgDecisionTree(SplitSearch splitSearch, double nodeSize, double purityThreshold) {
		super(splitSearch, nodeSize, purityThreshold);
	}

	public SampleAvgDecisionTree(SplitSearch splitSearch, int noSamples, double nodeSize, double purityThreshold) {
		this(splitSearch, nodeSize, purityThreshold);
		setNoSamples(noSamples);
	}

	public SampleAvgDecisionTree(SplitSearch splitSearch, int noSamples) {
		this(splitSearch);
		setNoSamples(noSamples);
	}

	protected PointDataSet generateDataSet(String training, String nameFile, int noSamples){
		SampleDataSetInit init = new SampleDataSetInit(training, noSamples, true);
		return init.getDataSet();
	}
	
	@Override
	public double crossFold(String training, String nameFile) {

		PointDataSet dataSet = generateDataSet(training, nameFile, noSamples);
		
		PointClassification classification = new PointClassification(dataSet, splitSearch);
		return classification.crossAllFold(nodeSize, purity);
		
	}

	public int getNoSamples() {
		return noSamples;
	}

	public void setNoSamples(int noSamples) {
		this.noSamples = noSamples;
	}

}
