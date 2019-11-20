package u_shapelet_transform.udt;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.special.Erf;

import weka.classifiers.Classifier;
import weka.classifiers.RandomizableClassifier;
import weka.core.Instance;
import weka.core.Instances;

public class UDT extends RandomizableClassifier {
	protected UNode root;
	protected HashSet<Integer> classes;

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
		int[] attributeList = new int[uData.getNumAttributes()];
		classes = uData.getClasses();
		root = generateUDT(uData, attributeList);
	}

	public UNode generateUDT(UInstances partition, int[] attributesList) {
		UNode root = new UNode();
		PartitionStats pStats = partition.getStats();

		if (pStats.getNumClasses() == 1) {
			root.setLeaf(true);
			root.getProbaByClass().put(pStats.getClasses()[0], pStats.getProba(pStats.getClasses()[0]));
			return root;
		}

		if (attributesList.length == 0) {
			root.setLeaf(true);
			for (int c : pStats.getClasses()) {
				root.getProbaByClass().put(c, pStats.getProba(c));
			}
			return root;
		}

		UInstances bestLeftPart = new UInstances();
		UInstances bestRightPart = new UInstances();
		UNode node = new UNode();
		System.out.println("NUM ATTR:"  + attributesList.length);
		findBestSplit(partition, attributesList, bestLeftPart, bestRightPart, root);

		if (bestLeftPart.getNumInstances() == 0) {
			node.setProbaByClass(partition.getStats().getProbaByClass());
			node.setLeaf(true);
			root.setLeftChild(node);
		} else {
			root.setLeftChild(generateUDT(bestLeftPart, attributesList));
		}

		node = new UNode();
		if (bestRightPart.getNumInstances() == 0) {
			node.setProbaByClass(partition.getStats().getProbaByClass());
			node.setLeaf(true);
			root.setRightChild(node);
		} else {
			root.setRightChild(generateUDT(bestRightPart, attributesList));
		}

		return root;
	}

	public HashMap<Integer, Double> predictProba(UInstance inst) {
		return predictProba(inst, root);
	}

	public HashMap<Integer, Double> predictProba(UInstance inst, UNode root) {
		double leftProba;
		UAttribute attr;
		HashMap<Integer, Double> predictions = new HashMap<Integer, Double>();
		for (int c : classes) {
			predictions.put(c, 0.0);
		}
		if (root.isLeaf()) {
			for (int c : root.getProbaByClass().keySet()) {
				predictions.put(c, predictions.get(c) + inst.getFuzziness() * root.getProbaByClass().get(c));
			}
		} else {
			attr = inst.getAttribute(root.getAttribute_pos());
			leftProba = inst.getFuzziness() * cdf(root.getSplit_value(), attr.getMean(), attr.getStd());

			if (leftProba == 0) {
				inst.setFuzziness(1);
				mergeProba(predictions, predictProba(inst, root.getRightChild()));
			} else if (leftProba == 1) {
				inst.setFuzziness(1);
				mergeProba(predictions, predictProba(inst, root.getLeftChild()));
			} else {
				inst.setFuzziness(leftProba);
				mergeProba(predictions, predictProba(inst, root.getLeftChild()));

				inst.setFuzziness(1 - leftProba);
				mergeProba(predictions, predictProba(inst, root.getRightChild()));
			}
		}

		return predictions;
	}

	public int classifyInstance(UInstance inst) {
		double max = -1;
		int _class = -1;
		HashMap<Integer, Double> probas = predictProba(inst);
		for (int c : probas.keySet()) {
			if (probas.get(c) > max) {
				max = probas.get(c);
				_class = c;
			}
		}

		return _class;
	}

	public double accuracy(UInstances test) {
		double a = 0;
		int size = test.getNumInstances();
		UInstance d;
		double predictedClass, trueClass;
		for (int i = 0; i < size; i++) {
			d = test.getInstance(i);
			try {
				predictedClass = classifyInstance(d);
				trueClass = d.getClassAttribute().getClassLabel();
				if (trueClass == predictedClass)
					a++;
//				System.out.println("True = "+trueClass+" Predicted = "+predictedClass);
			} catch (Exception e) {
				System.out.println(" Error with instance " + i + " Exception =" + e);
				e.printStackTrace();
				System.exit(0);
			}
		}
		return a / size;
	}

	public void mergeProba(HashMap<Integer, Double> result, HashMap<Integer, Double> prob2) {
		for (int c : prob2.keySet()) {
			result.put(c, result.getOrDefault(c, 0.0) + prob2.getOrDefault(c, 0.0));
		}
	}

	public void findBestSplit(UInstances partition, int[] attributesList, UInstances bestLeftPart,
			UInstances bestRightPart, UNode node) {
		int bestAttribute = 0;
		double bestSplitPoint = 0;
		double maxGainRatio = 0;
		double splitPoint;
		double gainRatio;
		UInstances leftPart, rightPart;

		for (int a=0; a < attributesList.length; a++) {
			final int pos = a;
			partition.getInstances().sort(new Comparator<UInstance>() {

				@Override
				public int compare(UInstance uinst0, UInstance uinst1) {
					// TODO Auto-generated method stub
					return uinst0.getAttribute(pos).compareTo(uinst1.getAttribute(pos));
				}
			});
			
//			System.out.println("Trying attr "  + a);

			for (int i = 0; i < partition.getNumInstances() - 1; i++) {
//				System.out.println("\t testing: i=" + i + ") " + partition.getInstance(i).getAttribute(a).getMean() + " vs " + partition.getInstance(i+1).getAttribute(a).getMean());
				if (partition.getInstance(i).getClassAttribute().getClassLabel() == partition.getInstance(i + 1)
						.getClassAttribute().getClassLabel()) {
					continue;
				}

				splitPoint = (partition.getInstance(i).getAttribute(a).getMean()
						+ partition.getInstance(i + 1).getAttribute(a).getMean()) / 2;
				leftPart = new UInstances();
				rightPart = new UInstances();
				splitPartition(a, splitPoint, leftPart, rightPart, partition);
				gainRatio = computeGainRation(partition, leftPart, rightPart);
				System.out.println("\t\tgainration = "  + gainRatio);
				if (gainRatio > maxGainRatio) {
					maxGainRatio = gainRatio;
					bestSplitPoint = splitPoint;
					bestAttribute = a;
					bestLeftPart = new UInstances(leftPart);
					bestRightPart = new UInstances(rightPart);
				}
			}
		}
		node.setAttribute_pos(bestAttribute);
		node.setSplit_value(bestSplitPoint);
	}

	public double computeGainRation(UInstances partition, UInstances leftPart, UInstances rightPart) {
		double gain = 0;
		double splitInfo = 0;
		double tmp;

		gain += (leftPart.getNumInstances() * leftPart.getStats().getEntropy());
		gain += (rightPart.getNumInstances() * rightPart.getStats().getEntropy());
		gain /= partition.getNumInstances();
		gain = partition.getStats().getEntropy() - gain;

		tmp = leftPart.getNumInstances() / partition.getNumInstances();
		splitInfo -= (tmp * Math.log(tmp));
		tmp = rightPart.getNumInstances() / partition.getNumInstances();
		splitInfo -= (tmp * Math.log(tmp));

//		return gain / splitInfo;
		return gain;
	}

	public void splitPartition(int attributePos, double splitPoint, UInstances leftPart, UInstances rightPart,
			UInstances partition) {
		double leftProba;
		
//		System.out.println("\tSplit point: " + splitPoint);
		for (UInstance inst : partition.getInstances()) {
			leftProba = cdf(splitPoint, inst.getAttribute(attributePos).getMean(),
					inst.getAttribute(attributePos).getStd());
//			System.out.println("\tLeftproba: " + leftProba);
			if (leftProba == 0) { // the instance completely lies in the right part
				inst.setFuzziness(1);
				rightPart.addInstance(inst);
			} else if (leftProba == 1) { // the instance completely lies in the left part
				inst.setFuzziness(1);
				leftPart.addInstance(inst);
			} else { // This instance has a non zero probability of being in each part
				inst.setFuzziness(leftProba);
				leftPart.addInstance(inst);

				inst.setFuzziness(1 - leftProba);
				rightPart.addInstance(inst);
			}
		}
	}

	/**
	 * compute the probability that a random variable X following a normal
	 * distribution will take a value less or equal to x
	 * 
	 * @param x
	 * @param mean
	 * @param std
	 * @return
	 */
	public double cdf(double x, double mean, double std) {
		double prob;

		prob = (x - mean) / (std * Math.sqrt(2));
		prob = (1 + Erf.erf(prob)) / 2;

		return prob;
	}
	
	public void printUDT(UNode root) {
		if (root == null) {
			System.out.println("Root: null");
			return ;
		}
		
		System.out.println(root);
		if(!root.isLeaf()) {
			printUDT(root.getLeftChild());
			printUDT(root.getRightChild());
		}
	}
	
	public void printUDT() {
		printUDT(root);
	}

}
