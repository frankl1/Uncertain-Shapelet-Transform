package u_shapelet_transform.udt;

import java.util.Arrays;

import weka.core.Instance;

public class UInstance {
	protected int classIndex;
	protected UAttribute[] attributes;
	protected double fuzziness; // the likelihood of being a member of a set of UInstances
	
	public UInstance(int numAttribute) {
		attributes = new UAttribute [numAttribute];
		classIndex = numAttribute - 1;
		fuzziness = 1;
	}
	
	public UInstance(UInstance uinstance) {
		classIndex = uinstance.getClassIndex();
		fuzziness = uinstance.getFuzziness();
		attributes = Arrays.copyOfRange(uinstance.getAttributes(), 0, uinstance.getNumAttribute());
	}
	
	public UInstance(Instance mean, Instance std) {
		attributes = new UAttribute [mean.numAttributes()];
		classIndex = mean.numAttributes() - 1;
		for (int i = 0; i < getNumAttribute(); i++) {
			attributes[i] = new UAttribute(mean.value(i), std.value(i), mean.attribute(i).name());
		}
		attributes[classIndex].setLabel(true);
	}
	
	public double getFuzziness() {
		return fuzziness;
	}

	public void setFuzziness(double fuzziness) {
		this.fuzziness = fuzziness;
	}

	public int getClassIndex() {
		return classIndex;
	}

	public void setClassIndex(int classIndex) {
		this.classIndex = classIndex;
	}

	public UAttribute[] getAttributes() {
		return attributes;
	}

	public void setAttributes(UAttribute[] attributes) {
		this.attributes = attributes;
	}
	
	public UAttribute getAttribute(int pos) {
		return attributes[pos];
	}
	
	public UAttribute getClassAttribute() {
		return attributes[classIndex];
	}
	
	public void setAttribute(UAttribute attribute, int pos) {
		this.attributes[pos] = attribute;
	}

	public int getNumAttribute() {
		return attributes.length;
	}
}
