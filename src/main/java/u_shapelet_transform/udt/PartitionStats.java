package u_shapelet_transform.udt;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;

public class PartitionStats {
	protected HashMap<Integer, Double> probaByClass;
	
	public PartitionStats(UInstances partition) {
		super();
		computeStats(partition.getInstances(), this);
	}
	
	public void computeStats(List<UInstance> partition, PartitionStats stats) {
		int numInstances = partition.size();
		probaByClass = new HashMap<Integer, Double>();
		
		if(numInstances == 0) {
			return;
		}
		for(int i = 0; i < numInstances; i++) {
			int _class = partition.get(i).getClassAttribute().getClassLabel();
			if(probaByClass.containsKey(_class)) {
				probaByClass.put(_class, probaByClass.get(_class) + 1 / numInstances);
			} else {
				probaByClass.put(_class, 1.0 / numInstances);
			}
		}
	}

	public HashMap<Integer, Double> getProbaByClass() {
		return probaByClass;
	}
	
	public int getNumClasses() {
		return probaByClass.keySet().size();
	}
	
	public Integer[] getClasses() {
		return probaByClass.keySet().toArray(new Integer[probaByClass.size()]);
	}

	public void setProbaByClass(HashMap<Integer, Double> pByClass) {
		this.probaByClass = pByClass;
	}
	
	public double getProba(int c) {
		return probaByClass.get(c);
	}
	
	public double getEntropy() {
		double entropy = .0;
		double p;
		for(int c : probaByClass.keySet()) {
			p = probaByClass.get(c);
			entropy -= (p * Math.log(p));
		}
		
		return entropy;
	}
}
