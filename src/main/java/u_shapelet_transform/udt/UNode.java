package u_shapelet_transform.udt;

public class UNode {
	protected int attribute_pos;
	protected int split_value;
	protected boolean isLeaf;
	protected int _class;
	protected UNode leftChild;
	protected UNode rightChild;
	
	public UNode() {
		super();
		isLeaf = false;
	}

	public int getAttribute_pos() {
		return attribute_pos;
	}

	public void setAttribute_pos(int attribute_pos) {
		this.attribute_pos = attribute_pos;
	}

	public int getSplit_value() {
		return split_value;
	}

	public void setSplit_value(int split_value) {
		this.split_value = split_value;
	}

	public boolean isLeaf() {
		return isLeaf;
	}

	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}

	public int get_class() {
		return _class;
	}

	public void set_class(int _class) {
		this._class = _class;
	}
	
}
