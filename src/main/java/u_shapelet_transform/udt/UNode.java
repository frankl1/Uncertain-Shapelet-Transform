package u_shapelet_transform.udt;

import java.util.HashMap;

public class UNode {
	protected int attribute_pos;
	protected double split_value;
	protected boolean isLeaf;
	protected UNode leftChild;
	protected UNode rightChild;
	protected HashMap<Integer, Double> probaByClass;
	protected int id;
	public static int lastId = 0;
	
	public UNode() {
		super();
		probaByClass = new HashMap<Integer, Double>();
		isLeaf = false;
		id = generateId();
	}
	
	protected synchronized int generateId() {
		int id = lastId;
		lastId++;
		return id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	@Override
	public String toString() {
		if (isLeaf) {
			return "UNode [attribute_pos=" + attribute_pos + ", split_value=" + split_value + ", id=" + id + "]";
		} else {
			return "UNode [attribute_pos=" + attribute_pos + ", split_value=" + split_value + ", isLeaf=" + isLeaf
					+ ", leftChildID=" + leftChild.getId() + ", rightChildID=" + rightChild.getId() + ", id=" + id + "]";
		}
	}
	
}
