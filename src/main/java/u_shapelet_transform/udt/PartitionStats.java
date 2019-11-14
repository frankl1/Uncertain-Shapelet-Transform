package u_shapelet_transform.udt;

import java.util.HashMap;
import java.util.List;

public class PartitionStats {
	protected int[] classes;
	protected int majorityClass;
	protected int numInstances;
	protected HashMap<Integer, Integer> countByClass;
	
	public PartitionStats(UInstances partition) {
		super();
		computeStats(partition.getInstances(), this);
	}
	
	public static void computeStats(List<UInstance> partition, PartitionStats stats) {
		int majorityClass;
		int numInstances = partition.size();
		HashMap<Integer, Integer> countByClass = new HashMap<Integer, Integer>();
		
		if(numInstances == 0) {
			return;
		}
		majorityClass = partition.get(0).getClassAttribute().getClassLabel();
		int[] classes = new int [numInstances];
		for(int i = 0; i < numInstances; i++) {
			int _class = partition.get(i).getClassAttribute().getClassLabel();
			if(countByClass.containsKey(_class)) {
				countByClass.put(_class, countByClass.get(_class) + 1);
			} else {
				countByClass.put(_class, 1);
				classes[i] = _class;
			}
			if(countByClass.containsKey(majorityClass)) {
				if(countByClass.get(majorityClass) < countByClass.get(_class)) {
					majorityClass = _class;
				}
			} else {
				majorityClass = _class;
			}
		}
	}

	public int getMajorityClass() {
		return majorityClass;
	}

	public void setMajorityClass(int majorityClass) {
		this.majorityClass = majorityClass;
	}

	public int[] getClasses() {
		return classes;
	}

	public void setClasses(int[] classes) {
		this.classes = classes;
	}

	public int getNumInstances() {
		return numInstances;
	}

	public void setNumInstances(int numInstances) {
		this.numInstances = numInstances;
	}

	public HashMap<Integer, Integer> getCountByClass() {
		return countByClass;
	}

	public void setCountByClass(HashMap<Integer, Integer> countByClass) {
		this.countByClass = countByClass;
	}

	public int getNumClasses() {
		return classes.length;
	}
	
	public int getCountForClass(int _class) {
		return countByClass.get(_class);
	}
	
	public double getEntropy() {
		double entropy = .0;
		double p;
		
		for(int c : classes) {
			p = countByClass.get(c) / numInstances;
			entropy -= (p * Math.log(p));
		}
		
		return entropy;
	}
}
