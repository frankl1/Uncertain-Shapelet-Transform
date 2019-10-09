package com.decisiontree.function;

import com.decisiontree.operation.SplitSearch;
import com.decisiontree.param.GlobalParam;

public class DecisionTreeFactory {

	public static DecisionTree createDecisionTree(String algorithm, SplitSearch splitSearch){
		return createDecisionTree(algorithm, splitSearch, GlobalParam.DEFAULT_NODESIZE, GlobalParam.DEFAULT_PURITY_THRESHOLD);
    	
    }
	
	
    public static DecisionTree createDecisionTree(String algorithm, SplitSearch splitSearch, double nodeSize, double purityThreshold){
    	DecisionTree decisionTree = null;
    	if(algorithm.equals(SplitSearch.AVG))
    	 	decisionTree = new SampleAvgDecisionTree(splitSearch, nodeSize, purityThreshold);
    	 		else if(algorithm.equals(SplitSearch.UDTUD))
    	 			decisionTree = new RangeDecisionTree(splitSearch, nodeSize, purityThreshold);
    			else if (algorithm.equals(SplitSearch.POINT))
    				decisionTree = new PointDecisionTree(splitSearch, nodeSize, purityThreshold);
    	 		else if(algorithm.equals(SplitSearch.AVGUD))
    	 			decisionTree = new RangeAvgDecisionTree(splitSearch, nodeSize, purityThreshold);
    	 		else
    	 			decisionTree = new SampleDecisionTree(splitSearch, nodeSize, purityThreshold);
		return decisionTree;
    	
    }
}
