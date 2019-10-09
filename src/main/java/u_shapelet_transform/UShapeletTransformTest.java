package u_shapelet_transform;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import timeseriesweka.filters.shapelet_transforms.ShapeletTransform;
import timeseriesweka.filters.shapelet_transforms.class_value.BinaryClassValue;
import timeseriesweka.filters.shapelet_transforms.distance_functions.SubSeqDistance;
import timeseriesweka.filters.shapelet_transforms.quality_measures.ShapeletQuality.ShapeletQualityChoice;
import utilities.ClassifierTools;
import vector_classifiers.MultiLinearRegression;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.meta.RotationForest;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class UShapeletTransformTest {

	double[] attrMeans;
	double[][] attrIntervals;
	String classifier;
	
	public UShapeletTransformTest(String clf) {
		super();
		this.classifier = clf;
	}
	
	public UShapeletTransformTest() {
		super();
		this.classifier = "svm";
	}
	
	public AbstractClassifier getClassifier(){
		if(this.classifier.equals("RandF")) {
			return new RandomForest();
		} else if(this.classifier.equals("MLP")) {
			return new MultilayerPerceptron();
		} else if (this.classifier.equals("DT")) {
			return new J48();
		} else if (this.classifier.equals("RotF")) {
			return new RotationForest();
		}
		return new SMO();
	}

	public void setClassifier(String clf){
		this.classifier = clf;
		if(clf.equals("RandF")) {
			System.out.println("Combiner classifier: Random Forest");
		} else if(clf.equals("MLP")) {
			System.out.println("Combiner classifier: Multi-Layer Perceptron");
		} else if (clf.equals("DT")) {
			System.out.println("Combiner classifier: Decision Tree (J48)");
		} else if (clf.equals("RotF")) {
			System.out.println("Combiner classifier: Rotation Forest");
		}
	}
	
	double std = 1 / Math.sqrt(2*Math.PI);

	private void normalizeErrors(Instances errors, Instances data) {
		for (int j = 0; j < errors.size(); j++) {
			for (int i = 0; i < data.numAttributes() - 1; i++) {
				errors.get(j).setValue(i, Math.abs(errors.get(j).value(i) / data.get(j).value(i)));
			}
		}
	}

	public double mean(Instance inst) {
		double sum = 0;

		for (int i = 0; i < inst.numAttributes() - 1; i++) {
			sum += inst.value(i);
		}

		return sum / (inst.numAttributes() - 1);
	}

	public double std(Instance inst) {
		double sum = 0;
		double mean = mean(inst);

		for (int i = 0; i < inst.numAttributes() - 1; i++) {
			sum += Math.pow((inst.value(i) - mean), 2);
		}

		return Math.sqrt(sum / (inst.numAttributes() - 1));
	}

	public void zNormalise(Instance inst) {
		double mean = mean(inst);
		double std = mean(inst);

		for (int i = 0; i < inst.numAttributes() - 1; i++) {
			inst.setValue(i, (inst.value(i) - mean) / std);
		}
	}

	public void zNormalise(Instances instances) {
		for (int i = 0; i < instances.numInstances(); i++) {
			zNormalise(instances.get(i));
		}
	}

	synchronized public void writeResult(String file, String result) {
		try {
			FileWriter fileWriter = new FileWriter(file, true);
			fileWriter.write(result);
			fileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public double gaussianPDF(double x, double mu, double std) {
		double result, std2 = std * std;
		result = Math.exp(-Math.pow(x - mu, 2) / (2 * std2));
		result /= Math.sqrt(2 * Math.PI * std2);
		return result;
	}

	public void computeAttrMean(Instances train) {
		int nbAttrs = (train.numAttributes() - 1) / 2;
		double max, min, tmp;
		attrMeans = new double[nbAttrs];
		attrIntervals = new double[nbAttrs][2];
		
		for (int i = 0; i < nbAttrs; i++) {
			min = Double.MAX_VALUE;
			max = Double.MIN_VALUE;
			for(int j = 0; j < train.numInstances(); j++) {
				tmp = train.instance(j).value(i) - Math.abs(train.instance(j).value(i)) * train.instance(j).value(i + nbAttrs);
				if(tmp < min) {
					min = tmp;
				}
				tmp = train.instance(j).value(i) + Math.abs(train.instance(j).value(i)) * train.instance(j).value(i + nbAttrs);
				if(tmp > max) {
					max = tmp;
				}
			}
			attrMeans[i] = (min + max)/2;
			attrIntervals[i][0] = min;
			attrIntervals[i][1] = max;
//			attrMeans[i] = train.meanOrMode(i);
		}
	}
	
	public boolean isInInterval(double x, double[] interval) {
		if(x < interval[0] || x > interval[1])
			return false;
		return true;
	}

	public Instances makeDataset(Instances instances) {
		 int length = attrMeans.length;
		 FastVector atts = new FastVector();
	     String name;
	     for (int i = 0; i < length; i++) {
	    	 name = "Shapelet_" + i;
	    	 atts.addElement(new Attribute(name));
	     }
	     if (instances.classIndex() >= 0) {
	    	 Attribute target = instances.attribute(instances.classIndex());

	    	 FastVector vals = new FastVector(target.numValues());
	    	 for (int i = 0; i < target.numValues(); i++) {
	    		 vals.addElement(target.value(i));
	    	 }
	    	 atts.addElement(new Attribute(instances.attribute(instances.classIndex()).name(), vals));
	     }
	     Instances output = new Instances("GaussianBasedDS", atts, instances.numInstances());
	     if (instances.classIndex() >= 0) {
	    	 output.setClassIndex(output.numAttributes() - 1);
	     }
	     
	     for (int j = 0; j < instances.numInstances(); j++) {
	    	 DenseInstance inst = new DenseInstance(attrMeans.length + 1);
	    	 for (int i = 0; i < attrMeans.length; i++) {
//	    		 inst.setValue(i, gaussianPDF(instances.instance(j).value(i), attrMeans[i], this.std));
	    		 inst.setValue(i, isInInterval(instances.instance(j).value(i), attrIntervals[i])? gaussianPDF(instances.instance(j).value(i), attrMeans[i], this.std) : 0);
//	    		 inst.setValue(i, instances.instance(j).value(i));
	    	 }
	    	 output.add(inst);
	     }
	     
	     for (int j = 0; j < instances.numInstances(); j++) {
	    	 output.instance(j).setValue(attrMeans.length, instances.instance(j).classValue());
	     }
	     return output;
	}

	public void shapeletTransform(String dataset, String datasetfolder, String resultfolder_name, int lenghtIncrement)
			throws Exception {
		final String resampleLocation = datasetfolder;
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		final String resultPath = new File(datasetfolder).getParent() + File.separator + resultfolder_name
				+ File.separator;
		final String filePath = resampleLocation + File.separator + dataset + File.separator + dataset;

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String date = format.format(new Date());
		String outfile = "results-" + date + ".csv";
		File file = new File(resultPath + outfile);
		file.getParentFile().mkdirs();
		FileWriter fileWriter = new FileWriter(file);
		String header = "dataset,train_size,test_size,series_length,no_classes,ust_gauss_acc,st_acc,ust_flat_acc,ust_flat_gauss_acc,ued_duration,ed_duration,ued_flat_duration,min_shp,max_shp,increment\n";
		fileWriter.write(header);
		fileWriter.close();

		Instances test, train;
		test = utilities.ClassifierTools.loadData(filePath + "_TEST");
		train = utilities.ClassifierTools.loadData(filePath + "_TRAIN");

		int min = 3;
		int max = train.numAttributes() - 1;

		// use fold as the seed.
		// train = InstanceTools.subSample(train, 100, fold);

		System.out.println(dataset + " min=" + min + " max=" + max + " increment=" + lenghtIncrement);

		ShapeletTransform transform = new ShapeletTransform();
		transform.setClassValue(new BinaryClassValue());
		transform.setSubSeqDistance(new SubSeqDistance());
		transform.setShapeletMinAndMax(min, max);
		transform.setLengthIncrement(lenghtIncrement);
		transform.useCandidatePruning();
		transform.setNumberOfShapelets(train.numInstances() * 10);
		transform.setQualityMeasure(ShapeletQualityChoice.INFORMATION_GAIN);
		transform.setLogOutputFile(resultPath + dataset + File.separator + "ED_Shapelets.csv");
		transform.supressOutput();

		long startTime = bean.getCurrentThreadUserTime();

		System.out.println(dataset + " ST started");
		Instances tranTrain = transform.process(train);
		Instances tranTest = transform.process(test);

		long endTime = bean.getCurrentThreadUserTime();

		UShapeletTransform utransform1 = new UShapeletTransform();
		utransform1.setClassValue(new BinaryClassValue());
		utransform1.setSubSeqDistance(new USubSeqDistance());
		utransform1.setShapeletMinAndMax(min, max);
		transform.setLengthIncrement(lenghtIncrement);
		utransform1.useCandidatePruning();
		utransform1.setNumberOfShapelets(train.numInstances() * 10);
		utransform1.setQualityMeasure(ShapeletQualityChoice.INFORMATION_GAIN);
		utransform1.setLogOutputFile(resultPath + dataset + File.separator + "FOTS_Shapelets.csv");
		utransform1.supressOutput();

		startTime = bean.getCurrentThreadUserTime();

		System.out.println(dataset + " UST started");

		Instances errorTrain, errorTest;
		errorTrain = utilities.ClassifierTools.loadData(filePath + "_NOISE_TRAIN");
		errorTest = utilities.ClassifierTools.loadData(filePath + "_NOISE_TEST");
		normalizeErrors(errorTest, test);
		normalizeErrors(errorTrain, train);

		Instances uTranTrain = utransform1.process(train, errorTrain);
		Instances uTranTest = utransform1.process(test, errorTest);
		
		computeAttrMean(uTranTrain);
		
		Instances gaussianTrain = makeDataset(uTranTrain);
		Instances gaussianTest = makeDataset(uTranTest);

		endTime = bean.getCurrentThreadUserTime();

		System.out.println("Number of instances: " + train.size() + " == " + uTranTrain.size());
		System.out.println("Number of shapelet: ST=" + tranTrain.get(0).numAttributes() + ", UST="
				+ uTranTrain.get(0).numAttributes() + ", G-UST=" + gaussianTrain.get(0).numAttributes());
		System.out.println("Instance 0: " + train.get(0));
		System.out.println("Errors of Instance 0: " + errorTrain.get(0));
		System.out.println("Transform Instance 0: " + tranTrain.get(0));
		System.out.println("uTransform Instance 0: " + uTranTrain.get(0));
		System.out.println("Transform G-Instance 0(train): " + gaussianTrain.get(0));;
		
		AbstractClassifier stClf = getClassifier();
		
		stClf.buildClassifier(tranTrain);
		double st_acc = ClassifierTools.accuracy(tranTest, stClf);
		long st_duration = (long) ((endTime - startTime) * 1e-9);

		System.out.println("\tST: Accuracy: " + st_acc + ", transform duration: " + st_duration + " sec");
		
		AbstractClassifier gaussClf = getClassifier();

		gaussClf.buildClassifier(gaussianTrain);
		double ust_gauss_accuracy = ClassifierTools.accuracy(gaussianTest, gaussClf);
		long ust_gauss_duration = (long) ((endTime - startTime) * 1e-9);

		System.out.println("\tUST-Gauss: Accuracy: " + ust_gauss_accuracy + ", transform duration: " + ust_gauss_duration + " sec");
		
		AbstractClassifier flatClf = getClassifier();

		flatClf.buildClassifier(uTranTrain);
		double ust_flat_accuracy = ClassifierTools.accuracy(uTranTest, flatClf);
		long ust_flat_duration = (long) ((endTime - startTime) * 1e-9);

		System.out.println("\tUST-Flat: Accuracy: " + ust_flat_accuracy + ", transform duration: " + ust_flat_duration + " sec");
		
//		zNormalise(train);
//		zNormalise(test);
		
		Instances combinedTrain = makeUSTInstance(flatClf, gaussClf, uTranTrain, gaussianTrain);
		Instances combinedTest = makeUSTInstance(flatClf, gaussClf, uTranTest, gaussianTest);

		AbstractClassifier clf = getClassifier();
		
		clf.buildClassifier(combinedTrain);
		double flat_gauss_accuracy = ClassifierTools.accuracy(combinedTest, clf);

		System.out.println("\tUST-Flat-Gauss: Accuracy: " + flat_gauss_accuracy);
		
		String content = dataset + "," + train.numInstances() + "," + test.numInstances() + ","
				+ train.numAttributes() + "," + train.numClasses() + "," + ust_gauss_accuracy + "," + st_acc + "," + ust_flat_accuracy + "," + flat_gauss_accuracy
				+ "," + ust_gauss_duration + "," + st_duration + "," + ust_flat_duration + "," + min + "," + max + "," + lenghtIncrement
				+ "\n";
		writeResult(resultPath + outfile, content);
		
//		System.out.println("UST instances:\n" + makeUSTInstance(flatClf, gaussClf, uTranTrain, gaussianTrain));
	}
	
	public Instances makeUSTInstance(AbstractClassifier flatClf, AbstractClassifier gaussClf, Instances flatInstances, Instances gaussIntances) throws Exception {
		int length = flatInstances.numClasses();
		double[] distFlat, distGauss;
		 FastVector atts = new FastVector();
	     String name;
	    
	     for(int i=0; i<length; i++) {
	    	  atts.addElement(new Attribute("flatOutput_" + (i+1)));
	     }
	     for(int i=0; i<length; i++) {
	    	  atts.addElement(new Attribute("gaussOutput_" + (i+1)));
	     }
	     
		Attribute target = flatInstances.attribute(flatInstances.classIndex());

		FastVector vals = new FastVector(target.numValues());
		for (int i = 0; i < target.numValues(); i++) {
			vals.addElement(target.value(i));
		}
		atts.addElement(new Attribute(flatInstances.attribute(flatInstances.classIndex()).name(), vals));
	    
	     Instances output = new Instances("CombinedDS", atts, flatInstances.numInstances());
	     if (flatInstances.classIndex() >= 0) {
	    	 output.setClassIndex(2*length);
	     }
	     
	     for (int j = 0; j < flatInstances.numInstances(); j++) {
	    	 DenseInstance inst = new DenseInstance(2*length + 1);
	    	 distFlat = flatClf.distributionForInstance(flatInstances.instance(j));
	    	 distGauss = gaussClf.distributionForInstance(gaussIntances.instance(j));
	    	 for(int i=0; i < distFlat.length; i++) {
		         inst.setValue(i, distFlat[i]);
		         inst.setValue(i + distFlat.length, distGauss[i]);
	    	 }
	    	 output.add(inst);
	     }
	     
	     for (int j = 0; j < flatInstances.numInstances(); j++) {
	    	 output.instance(j).setValue(2*length, flatInstances.instance(j).classValue());
	     }
	     return output;
	}
	
	public double maxFromArray(double[] array) {
		double max = Double.MIN_VALUE;
		for(int i = 0; i < array.length; i++) {
			if(array[i] > max) {
				max = array[i];
			}
		}
		return max;
	}

	public void shapeletTransform(int nb_of_thread, String datasetfolder, String resultFolderName,
			int lenghtIncrement) {
		try {
			final String resampleLocation = datasetfolder;
			final String resultPath = new File(datasetfolder).getParent() + File.separator + resultFolderName
					+ File.separator;
			File folder = new File(resampleLocation);
			final String[] datasets = folder.list();

			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			String date = format.format(new Date());
			String outfile = "results-" + date + ".csv";
			File file = new File(resultPath + outfile);
			file.getParentFile().mkdirs();
			FileWriter fileWriter = new FileWriter(file);
			String header = "dataset,train_size,test_size,series_length,no_classes,ust_gauss_acc,st_acc,ust_flat_acc,ust_flat_gauss_acc,ued_duration,ed_duration,ued_flat_duration,min_shp,max_shp,increment\n";
			fileWriter.write(header);
			fileWriter.close();

			BlockingQueue<String> blockingQueue = new LinkedBlockingDeque<>();
			ExecutorService executor = Executors.newFixedThreadPool(nb_of_thread);

			Runnable consumerTask = () -> {
				try {

					String dataset;

					while ((dataset = blockingQueue.poll()) != null) {

						ThreadMXBean bean = ManagementFactory.getThreadMXBean();
						final String filePath = resampleLocation + File.separator + dataset + File.separator + dataset;

						Instances test, train;
						test = utilities.ClassifierTools.loadData(filePath + "_TEST");
						train = utilities.ClassifierTools.loadData(filePath + "_TRAIN");

						int min = 3;
						int max = train.numAttributes() - 1;

						// use fold as the seed.
						// train = InstanceTools.subSample(train, 100, fold);

						System.out.println(dataset + " min=" + min + " max=" + max + " increment=" + lenghtIncrement);

						ShapeletTransform transform = new ShapeletTransform();
						transform.setClassValue(new BinaryClassValue());
						transform.setSubSeqDistance(new SubSeqDistance());
						transform.setShapeletMinAndMax(min, max);
						transform.setLengthIncrement(lenghtIncrement);
						transform.useCandidatePruning();
						transform.setNumberOfShapelets(train.numInstances() * 10);
						transform.setQualityMeasure(ShapeletQualityChoice.INFORMATION_GAIN);
						transform.setLogOutputFile(resultPath + dataset + File.separator + "ED_Shapelets.csv");
						transform.supressOutput();

						long startTime = bean.getCurrentThreadUserTime();

						System.out.println(dataset + " ST started");
						Instances tranTrain = transform.process(train);
						Instances tranTest = transform.process(test);

						long endTime = bean.getCurrentThreadUserTime();

						AbstractClassifier stClf = getClassifier();

						stClf.buildClassifier(tranTrain);
						double st_acc = ClassifierTools.accuracy(tranTest, stClf);
						long st_duration = (long) ((endTime - startTime) * 1e-9);

						System.out.println(dataset + " ST finished");

						UShapeletTransform utransform1 = new UShapeletTransform();
						utransform1.setClassValue(new BinaryClassValue());
						utransform1.setSubSeqDistance(new USubSeqDistance());
						utransform1.setShapeletMinAndMax(min, max);
						transform.setLengthIncrement(lenghtIncrement);
						utransform1.useCandidatePruning();
						utransform1.setNumberOfShapelets(train.numInstances() * 10);
						utransform1.setQualityMeasure(ShapeletQualityChoice.INFORMATION_GAIN);
						utransform1.setLogOutputFile(resultPath + dataset + File.separator + "FOTS_Shapelets.csv");
						utransform1.supressOutput();

						startTime = bean.getCurrentThreadUserTime();

						System.out.println(dataset + " UST started");

						Instances errorTrain, errorTest;
						errorTrain = utilities.ClassifierTools.loadData(filePath + "_NOISE_TRAIN");
						errorTest = utilities.ClassifierTools.loadData(filePath + "_NOISE_TEST");
						normalizeErrors(errorTest, test);
						normalizeErrors(errorTrain, train);

						Instances uTranTrain = utransform1.process(train, errorTrain);
						Instances uTranTest = utransform1.process(test, errorTest);
						
						computeAttrMean(uTranTrain);
						
						Instances gaussianTrain = makeDataset(uTranTrain);
						Instances gaussianTest = makeDataset(uTranTest);

						endTime = bean.getCurrentThreadUserTime();

						AbstractClassifier gaussClf = getClassifier();

						gaussClf.buildClassifier(gaussianTrain);
						double ust_gauss_accuracy = ClassifierTools.accuracy(gaussianTest, gaussClf);
						long ust_gauss_duration = (long) ((endTime - startTime) * 1e-9);

						System.out.println(dataset + " UST finished");
						
						AbstractClassifier flatClf = getClassifier();

						flatClf.buildClassifier(uTranTrain);
						double ust_flat_accuracy = ClassifierTools.accuracy(uTranTest, flatClf);
						long ust_flat_duration = (long) ((endTime - startTime) * 1e-9);
						
						Instances combinedTrain = makeUSTInstance(flatClf, gaussClf, uTranTrain, gaussianTrain);
						Instances combinedTest = makeUSTInstance(flatClf, gaussClf, uTranTest, gaussianTest);
						
						AbstractClassifier clf = getClassifier();
						clf.buildClassifier(combinedTrain);
						double flat_gauss_accuracy = ClassifierTools.accuracy(combinedTest, clf);

//						zNormalise(train);
//						zNormalise(test);

						String content = dataset + "," + train.numInstances() + "," + test.numInstances() + ","
								+ train.numAttributes() + "," + train.numClasses() + "," + ust_gauss_accuracy + "," + st_acc + "," + ust_flat_accuracy + "," + flat_gauss_accuracy
								+ "," + ust_gauss_duration + "," + st_duration + "," + ust_flat_duration + "," + min + "," + max + "," + lenghtIncrement
								+ "\n";
						writeResult(resultPath + outfile, content);

						System.out.println("\tST: Accuracy: " + st_acc + ", transform duration: " + st_duration + " sec");
						System.out.println("\tUST-Flat: Accuracy: " + ust_flat_accuracy + ", transform duration: " + ust_flat_duration + " sec");
						System.out.println("\tUST-Gauss: Accuracy: " + ust_gauss_accuracy + ", transform duration: " + ust_gauss_duration + " sec");
						System.out.println("\tUST-Flat-Gauss: Accuracy: " + flat_gauss_accuracy);

					}

				} catch (InterruptedException e) {

					e.printStackTrace();

				} catch (Exception ex) {
					// TODO Auto-generated catch block
					Logger.getLogger(ShapeletTransform.class.getName()).log(Level.SEVERE, null, ex);
				}

			};

			for (String dataset : datasets) {
				blockingQueue.offer(dataset);
			}

			System.out.println("Q.size = " + blockingQueue.size());

			for (int i = 0; i < nb_of_thread; i++) {
				executor.execute(consumerTask);
			}

			executor.shutdown();

            while (!executor.awaitTermination(3, TimeUnit.MINUTES)) {
            	System.out.println("Still running");
            }

			System.out.println("FINISHED");
		} catch (IOException ex) {
			ex.printStackTrace();
			System.err.println(ex.getMessage());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//	public static void main(String argv[]) {
//		UShapeletTransformTest test = new UShapeletTransformTest();
//		
////		double[] ts = {5, 2, 3, 5, 6, 1, 1, 0, 9, 4};
////		
////		Matrix corr = fots.auto_corr_matrix(ts);
////	
////		System.out.println(corr);
////		
////		System.out.println("\n"+fots.eigenVectors(corr));
//		String datasetfolder = argv[0];
//		String resultFolderName = argv[1];
//		int lenghtIncrement = Integer.parseInt(argv[2]);
//		final int MAX_NB_THREAD = 20;
//		
//		System.out.println(datasetfolder);
//		if(argv.length == 4) {
//			test.setClassifier(argv[3]);
//		}
//		
//		test.shapeletTransform(MAX_NB_THREAD, datasetfolder, resultFolderName, lenghtIncrement);
//	} 

	public static void main(String[] argv) {
		UShapeletTransformTest test = new UShapeletTransformTest();
		String datasetfolder_noise = "/home/mimbouop/Codes/ust/Source-code/noised_dataset_nomal_pr_0_stdcoef1";
		String datasetfolder_clean = "/home/mimbouop/Codes/ust/Source-code/dataset";
		String dataset = "Chinatown";

		int lenghtIncrement = 1;

//		try {
//			System.out.println("\n\n" + datasetfolder_clean);
//			test.shapeletTransform(dataset, datasetfolder_clean, "result_cleaned", lenghtIncrement);
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		test.setClassifier("DT");
		try {
			System.out.println("\n\n" + datasetfolder_noise);
			test.shapeletTransform(dataset, datasetfolder_noise, "result_noised_nomal_std_1", lenghtIncrement);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		double mu = 0;
//		double std = 1 / Math.sqrt(2*Math.PI);
//		
//		System.out.println(test.gaussianPDF(0, mu, std));
	}

}
