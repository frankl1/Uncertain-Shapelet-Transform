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

import org.apache.log4j.Logger;

import com.decisiontree.build.TreeNode;
import com.decisiontree.exceptions.DecisionTreeFileException;
import com.decisiontree.file.DecisionTreeStorage;
import com.decisiontree.operation.SplitSearch;
import com.decisiontree.param.GlobalParam;

/**
 *
 * DecisionTree (Abstract class) - builds a decision tree for given dataset files.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public abstract class DecisionTree {

	protected static final Logger log = Logger.getLogger(DecisionTree.class);

	public static final String TIME = "TIME";
	public static final String ACCUR = "ACCUR";
	public static final String XFOLD = "XFOLD";

	protected SplitSearch splitSearch;

	protected double nodeSize = GlobalParam.DEFAULT_NODESIZE;
	protected double purity = GlobalParam.DEFAULT_PURITY_THRESHOLD;

	/**
	 * Constructor by the algorithm (SplitSearch) to find the best split point
	 * @param splitSearch the SplitSearch object
	 */
	public DecisionTree(SplitSearch splitSearch){
		setSplitSearch(splitSearch);
	}

	/**
	 * Constructor by the algorithm (SplitSearch) to find the best split point and the pre-pruning parameter
	 * @param splitSearch the SplitSearch object
	 * @param nodeSize the size of the node for the tree to stop
	 * @param purity the purity of the node for the tree to stop
	 */
	public DecisionTree(SplitSearch splitSearch, double nodeSize, double purity){
		this(splitSearch);
		setNodeSize(nodeSize);
		setPurity(purity);
	}

	/**
	 * Building and saving a decision tree from a given training data file
	 * @param training the training dataset file
	 * @param nameFile the property file
	 * @param path the path to store the decision tree
	 */
	public boolean buildAndSaveTree(String training, String nameFile, String path)  {
		TreeNode tree = buildTree(training, nameFile);
		DecisionTreeStorage treeStorage = new DecisionTreeStorage();
		try {
			treeStorage.saveTreeToFile(path, tree);
		} catch (DecisionTreeFileException e) {
			log.error("Fail to save tree to the given path.",e);
			return false;
		}
		return true;

	}


//	/**
//	 * Building and saving a decision treefrom a given training data file
//	 * @param training the training dataset file
//	 * @param path the path to store the decision tree
//	 * @deprecated
//	 */
//	public void buildAndSaveTree(String training, String path)  {
//		TreeNode tree = buildTree(training);
//		DecisionTreeStorage treeStorage = new DecisionTreeStorage();
//		try {
//			treeStorage.saveTreeToFile(path, tree);
//		} catch (DecisionTreeFileException e) {
//			log.error("Fail to save tree to the given path.",e);
//		}
//
//	}
//	public abstract void buildAndSaveTree(String training, String path);

//	/**
//	 * Building a deciison tree from the given training data file
//	 * @param training the training dataset file
//	 * @return the root node of the decision tree
//	 * @deprecated
//	 */
//	public TreeNode buildTree(String training){
//		return buildTree(training, training);
//	}


	/**
	 * Building a deciison tree from the given training data file
	 * @param training the training dataset file
	 * @param nameFile the property file
	 * @return the root node of the decision tree
	 */
	public abstract TreeNode buildTree(String training, String nameFile);

//	/**
//	 * Generate the decision tree by a set of training data and finding the accuracy
//	 * of testing the same set of data (Self-classifying)
//	 * @param training the training dataset file
//	 * @return the accuracy
//	 * @deprecated
//	 */
//	public double findAccuracy(String training){
//		return findAccuracy(training, training);
//	}

	/**
	 * Generate the decision tree by a set of training data and finding the accuracy
	 * of testing the same set of data (Self-classifying)
	 * @param training the training dataset file
	 * @param nameFile the property file
	 * @return the classification accuracy
	 */
	public abstract double findAccuracy(String training, String nameFile);

	/**
	 * Generate the decision tree by a set of training data and finding the accuracy
	 * of testing a set of testin data file
	 * @param training the training data file
	 * @param testing the testing data
	 * @param nameFile the property file
	 * @return the classification accuracy
	 */
	public abstract double findAccuracy(String training, String testing, String nameFile);


	/**
	 * Find the classification accuracy using the given tree stored in given path
	 * @param path the path of the tree
	 * @param testing the testing data file
	 * @param nameFile the property file
	 * @return the classification accuracy
	 */
	public abstract double findAccuracyByTree(String path, String testing, String nameFile);

	/**
	 * Find the classification accuracy using the given tree stored in given path
	 * @param treeRoot the tree denoted by tree root
	 * @param testing the testing data file
	 * @param nameFile the property file
	 * @return the classification accuracy
	 */
	protected abstract double findAccuracyByTree(TreeNode treeRoot, String testing, String nameFile);

	/**
	 * Find the cross-fold validataion accuracy
	 * @param training the training data file
	 * @param nameFile the property file
	 * @return the cross-fold validation accuracy
	 */
	public abstract double crossFold(String training, String nameName);

	/**
	 * Get the algorithm (SplitSearch) to find the best split point
	 * @return the SplitSearch object
	 */
	public SplitSearch getSplitSearch() {
		return splitSearch;
	}

	/**
	 * Set the algorithm (SplitSearch) to find the best split point
	 * @param splitSearch the splitSearch object
	 */
	public void setSplitSearch(SplitSearch splitSearch) {
		this.splitSearch = splitSearch;
	}

	/**
	 * Get the pruning node size during the building the decision
	 * @return the pruning node size
	 */
	public double getNodeSize() {
		return nodeSize;
	}

	/**
	 * Set the pruning node size during the building of decision tree
	 * @param nodeSize the pruning node size
	 */
	public void setNodeSize(double nodeSize) {
		this.nodeSize = nodeSize;
	}

	/**
	 * Get the pruning purity during the building of decision tree
	 * @return the pruning purity
	 */
	public double getPurity() {
		return purity;
	}

	/**
	 * Set the pruning purity during the building of decision tree
	 * @param purity the pruning purity
	 */
	public void setPurity(double purity) {
		this.purity = purity;
	}

	/**
	 * Getting the tree form the given file path
	 * @param path the given file path
	 * @return the tree root containing the tree
	 */
	protected TreeNode getTreeFromFile(String path){
		DecisionTreeStorage treeStorage = new DecisionTreeStorage();

		TreeNode treeRoot = null;
		try {
			treeRoot = treeStorage.readTreeFromFile(path);
		} catch (DecisionTreeFileException e) {
			log.error("Fail to load tree from the given path.",e);
		}
		return treeRoot;
	}

}
