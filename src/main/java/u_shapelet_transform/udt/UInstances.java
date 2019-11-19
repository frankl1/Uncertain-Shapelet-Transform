package u_shapelet_transform.udt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import weka.core.Instances;

public class UInstances {
	protected List<UInstance> instances;
	protected HashSet<Integer> classes;
	protected PartitionStats stats;
	protected boolean updateStat = true;
	
	public UInstances() {
		instances = new ArrayList<UInstance>();
	}
	
	public UInstances(Instances means, Instances stds) {
		instances = new ArrayList<UInstance>();
		for (int i = 0; i < means.numInstances(); i++) {
			instances.add(new UInstance(means.get(i), stds.get(i)));
			classes.add((int) means.get(i).classValue());
		}
	}
	
	public UInstances(UInstances instances) {
		setInstances(instances.getInstances());
		setClasses(instances.getClasses());
	}
	
	public PartitionStats getStats() {
		if (stats == null || updateStat) {
			stats = new PartitionStats(this);
			updateStat = false;
		}
		return stats;
	}

	public HashSet<Integer> getClasses() {
		return classes;
	}

	public void setClasses(HashSet<Integer> classes) {
		this.classes = classes;
	}

	public void setStats(PartitionStats stats) {
		this.stats = stats;
	}

	public int getNumAttributes() {
		return instances.get(0).getNumAttribute();
	}

	public int getNumClasses() {
		return classes.size();
	}

	public List<UInstance> getInstances() {
		return instances;
	}

	public void setInstances(java.util.List<UInstance> instances) {
		this.instances = instances;
		updateStat = true;
	}
	
	public void setInstance(UInstance instance, int pos) {
		this.instances.set(pos, instance);
		classes.add(instance.getClassAttribute().getClassLabel());
		updateStat = true;
	}
	
	public int getNumInstances() {
		return this.instances.size();
	}
	
	public UInstance getInstance(int pos) {
		return this.instances.get(pos);
	}
	
	public void addInstance(UInstance inst) {
		this.instances.add(inst);
		this.classes.add(inst.getClassAttribute().getClassLabel());
		updateStat = true;
	}
	
	public UInstances getSubset(int start, int end) {
		UInstances uinstances = new UInstances();
		uinstances.setInstances(getInstances().subList(start, end));
		return uinstances;
	}
}
