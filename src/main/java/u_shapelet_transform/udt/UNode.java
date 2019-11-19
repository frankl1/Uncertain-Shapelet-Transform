package u_shapelet_transform.udt;

import java.util.HashMap;

public class UNode {
	protected int attribute_pos;
	protected double split_value;
	protected boolean isLeaf;
	protected UNode leftChild;
	protected UNode rightChild;
	protected HashMap<Integer, Double> probaByClass;
	
	public UNode() {
		super();
		probaByClass = new HashMap<Integer, Double>();
		isLeaf = false;
	}

	public HashMap<Integer, Double> getProbaByClass() {
		return probaByClass;
	}

	public void setProbaByClass(HashMap<Integer, Double> probaByClass) {
		this.probaByClass = probaByClass;
	}

	public UNode getLeftChild() {
		return leftChild;
	}

	public void setLeftChild(UNode leftChild) {
		this.leftChild = leftChild;
	}

	public UNode getRightChild() {
		return rightChild;
	}

	public void setRightChild(UNode rightChild) {
		this.rightChild = rightChild;
	}

	public int getAttribute_pos() {
		return attribute_pos;
	}

	public void setAttribute_pos(int attribute_pos) {
		this.attribute_pos = attribute_pos;
	}

	public double getSplit_value() {
		return split_value;
	}

	public void setSplit_value(double bestSplitPoint) {
		this.split_value = bestSplitPoint;
	}

	public boolean isLeaf() {
		return isLeaf;
	}

	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}
	
}
