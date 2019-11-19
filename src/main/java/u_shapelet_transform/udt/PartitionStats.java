package u_shapelet_transform.udt;

import java.util.HashMap;
import java.util.List;

public class PartitionStats {
	protected int[] classes;
	protected HashMap<Integer, Double> probaByClass;
	
	public PartitionStats(UInstances partition) {
		super();
		computeStats(partition.getInstances(), this);
	}
	
	public static void computeStats(List<UInstance> partition, PartitionStats stats) {
		int numInstances = partition.size();
		HashMap<Integer, Integer> countByClass = new HashMap<Integer, Integer>();
		
		if(numInstances == 0) {
			return;
		}
		int[] classes = new int [numInstances];
		for(int i = 0; i < numInstances; i++) {
			int _class = partition.get(i).getClassAttribute().getClassLabel();
			if(countByClass.containsKey(_class)) {
				countByClass.put(_class, countByClass.get(_class) + 1 / numInstances);
			} else {
				countByClass.put(_class, 1 / numInstances);
				classes[i] = _class;
			}
		}
	}

	public int[] getClasses() {
		return classes;
	}

	public void setClasses(int[] classes) {
		this.classes = classes;
	}

	public HashMap<Integer, Double> getProbaByClass() {
		return probaByClass;
	}

	public void setProbaByClass(HashMap<Integer, Double> pByClass) {
		this.probaByClass = pByClass;
	}

	public int getNumClasses() {
		return classes.length;
	}
	
	public double getProba(int c) {
		return probaByClass.get(c);
	}
	
	public double getEntropy() {
		double entropy = .0;
		double p;
		
		for(int c : classes) {
			p = probaByClass.get(c);
			entropy -= (p * Math.log(p));
		}
		
		return entropy;
	}
}
