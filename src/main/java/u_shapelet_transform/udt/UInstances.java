package u_shapelet_transform.udt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import weka.core.Instances;

public class UInstances {
	protected List<UInstance> instances;
	protected int numAttributes;
	protected HashSet<Integer> classes;
	
	public UInstances(Instances means, Instances stds) {
		instances = new ArrayList<UInstance>();
		numAttributes = means.numAttributes();
		for (int i = 0; i < means.numInstances(); i++) {
			instances.add(new UInstance(means.get(i), stds.get(i)));
			classes.add((int) means.get(i).classValue());
		}
	}
	
	public UInstances() {
		instances = new ArrayList<UInstance>();
	}

	public int getNumAttributes() {
		return numAttributes;
	}

	public void setNumAttributes(int numAttributes) {
		this.numAttributes = numAttributes;
	}

	public int getNumClasses() {
		return classes.size();
	}

	public List<UInstance> getInstances() {
		return instances;
	}

	public void setInstances(java.util.List<UInstance> instances) {
		this.instances = instances;
	}
	
	public void setInstance(UInstance instance, int pos) {
		this.instances.set(pos, instance);
		classes.add(instance.getClassAttribute().getClassLabel());
	}
	
	public int getNumInstances() {
		return this.instances.size();
	}
	
	public UInstance getInstance(int pos) {
		return this.instances.get(pos);
	}
	
	public UInstances getSubset(int start, int end) {
		UInstances uinstances = new UInstances();
		uinstances.setNumAttributes(numAttributes);
		uinstances.setInstances(getInstances().subList(start, end));
		return uinstances;
	}
}
