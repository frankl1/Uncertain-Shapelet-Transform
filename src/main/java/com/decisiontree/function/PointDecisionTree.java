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

import java.util.List;

import com.decisiontree.build.PointClassification;
import com.decisiontree.build.PointTree;
import com.decisiontree.build.TreeNode;
import com.decisiontree.data.PointDataSet;
import com.decisiontree.data.PointDataSetInit;
import com.decisiontree.data.Tuple;
import com.decisiontree.operation.SplitSearch;

/**
 * 
 * PointDecisionTree -  builds a decision tree for given  point-valued dataset files.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class PointDecisionTree extends DecisionTree {

	public PointDecisionTree(SplitSearch splitSearch){
		super(splitSearch);
	}
	
	public PointDecisionTree(SplitSearch splitSearch, double nodeSize, double purityThreshold) {
		super(splitSearch, nodeSize, purityThreshold);
	}

	
	protected PointDataSet generateDataSet(String training, String nameFile){
		PointDataSetInit init = new PointDataSetInit(training, nameFile);
		return init.getDataSet();
	}


	
	@Override
	public TreeNode buildTree(String training, String nameFile) {
		
		PointDataSet dataSet = generateDataSet(training, nameFile);
		
		PointTree tree = new PointTree(dataSet, getSplitSearch(),nodeSize, purity);
		
		tree.constructFinalTree(false); // TODO: allow print tree
		
		return tree.getRoot();
		
	}

	@Override
	public double crossFold(String training, String nameFile) {

		PointDataSet dataSet = generateDataSet(training, nameFile);
		
		PointClassification classification = new PointClassification(dataSet, splitSearch);
		return classification.crossAllFold(nodeSize, purity);
		
	}

	@Override
	public double findAccuracy(String training, String testing, String nameFile) {
		
		PointDataSet dataSet = generateDataSet(training, nameFile);
		
		PointTree tree = new PointTree(dataSet,splitSearch, nodeSize, purity);

		tree.constructFinalTree(false);

		return findAccuracyByTree(tree.getRoot(), training, nameFile);

	}

	@Override
	public double findAccuracy(String training, String nameFile) {

		PointDataSet dataSet = generateDataSet(training, nameFile);
		
		PointTree tree = new PointTree(dataSet,splitSearch);

		tree.constructFinalTree(false);

		PointClassification test = new PointClassification(dataSet, splitSearch);

		List<Tuple> testSet =  dataSet.getData();
		return test.ClassifyAll(tree.getRoot(), testSet);

	}

	@Override
	public double findAccuracyByTree(String path, String testing, String nameFile) {

		TreeNode treeRoot = getTreeFromFile(path);
		if(treeRoot == null) return 0;

		return findAccuracyByTree(treeRoot, testing, nameFile);

	}

	@Override
	protected double findAccuracyByTree(TreeNode treeRoot, String testing,
			String nameFile) {
		PointDataSet testDataSet = generateDataSet(testing, nameFile);
		PointClassification test = new PointClassification(testDataSet, splitSearch);

		List<Tuple> testSet =  testDataSet.getData();
		return test.ClassifyAll(treeRoot, testSet);
	}


}
