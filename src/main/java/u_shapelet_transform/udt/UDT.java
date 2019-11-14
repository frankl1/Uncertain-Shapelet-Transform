package u_shapelet_transform.udt;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;

import weka.classifiers.RandomizableClassifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;

public class UDT extends RandomizableClassifier {
	protected UNode root;
	
	@Override
	public void buildClassifier(Instances data) throws Exception {
		// TODO Auto-generated method stub
	}
	
	public void buildClassifier(Instances means, Instances stds) throws Exception {
		// TODO Auto-generated method stub
		buildClassifier(new UInstances(means, stds));
	}
	
	public void buildClassifier(UInstances uData) throws Exception {
		// TODO Auto-generated method stub
		int[] attributeList = new int [uData.getNumAttributes()];
		root = new UNode();
		generateUDT(uData, attributeList, root);
	}
	
	public void generateUDT(UInstances partition, int[] attributesList, UNode root) {
		PartitionStats pStats = new PartitionStats(partition);
		
		if (pStats.classes.length == 1) {
			root.setLeaf(true);
			root.set_class(pStats.classes[0]);
			return;
		}
		
		if(attributesList.length == 0) {
			root.setLeaf(true);
			root.set_class(pStats.getMajorityClass());
			return;
		}
		
		
	}
	
	public void findBestSplit(UInstances partition, int[] attributesList, double entropy) {
		int bestAttribute;
		double bestSplitPoint;
		double splitPoint;
		double maxGain;
		double gain;
		
		for (int a : attributesList) {
			partition.getInstances().sort(new Comparator<UInstance>() {

				@Override
				public int compare(UInstance uinst0, UInstance uinst1) {
					// TODO Auto-generated method stub
					return uinst0.getAttribute(a).compareTo(uinst1.getAttribute(a));
				}
			});
			
			for (int i = 0; i < partition.getNumInstances() - 1; i++) {
				if(partition.getInstance(i).getClassAttribute().getClassLabel() == partition.getInstance(i+1).getClassAttribute().getClassLabel()) {
					continue;
				}
				
				splitPoint = (partition.getInstance(i).getAttribute(a).getMean() + partition.getInstance(i+1).getAttribute(a).getMean()) / 2;
			}
		}
	}

}
