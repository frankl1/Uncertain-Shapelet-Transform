package u_shapelet_transform;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import ml.dmlc.xgboost4j.java.Rabit.DataType;
import timeseriesweka.filters.shapelet_transforms.ShapeletTransform;
import timeseriesweka.filters.shapelet_transforms.class_value.BinaryClassValue;
import timeseriesweka.filters.shapelet_transforms.distance_functions.SubSeqDistance;
import timeseriesweka.filters.shapelet_transforms.quality_measures.ShapeletQuality.ShapeletQualityChoice;
import u_shapelet_transform.DustSubSeqDistance.DataDistribution;
import u_shapelet_transform.udt.UDT;
import u_shapelet_transform.udt.UInstance;
import u_shapelet_transform.udt.UInstances;
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
	int lenghtIncrement;
	
	/**
	 * true if the measures are independent, random and following normal distribution
	 */
	boolean isGaussian = false;
	
	public enum ExecMode{
		UST_FLAT("UST_FLAT"),
		UST_GAUSS("UST_GAUSS"), 
		UST_FLAT_GAUSS("UST_FLAT_GAUSS"),
		UST_UDT("UST_UDT"),
		DUST_NORMAL("DUST_NORMAL"),
		DUST_UNIFORM("DUST_UNIFORM"),
		ST("ST");
		
		private String name;
		
		private ExecMode(String name) {
			// TODO Auto-generated constructor stub
			this.name = name;
		}
		
		private String getName() {
			return this.name;
		}
	}
	
	ExecMode execMode;
	
	public UShapeletTransformTest(String clf) {
		super();
		this.classifier = clf;
	}
	
	public UShapeletTransformTest() {
		super();
		this.classifier = "svm";
	}
	
	public boolean isGaussian() {
		return isGaussian;
	}

	public void setGaussian(boolean isGaussian) {
		this.isGaussian = isGaussian;
	}

	public ExecMode getExecMode() {
		return execMode;
	}

	public void setExecMode(ExecMode execMode) {
		this.execMode = execMode;
	}

	public AbstractClassifier getClassifier(){
		if(this.classifier.equals("RandF")) {
			return new RandomForest();
		} else if(this.classifier.equals("MLP")) {
			return new MultilayerPerceptron();
		} else if (this.classifier.equals("SVM")) {
			return new SMO();
		} else if (this.classifier.equals("RotF")) {
			return new RotationForest();
		}
		return new J48();
	}

	public int getLenghtIncrement() {
		return lenghtIncrement;
	}

	public void setLenghtIncrement(int lenghtIncrement) {
		this.lenghtIncrement = lenghtIncrement;
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

	// from absolute errors to relative errors
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
	
	public DustSubSeqDistance getDustSubSeqDistance() {
		if(execMode.equals(ExecMode.DUST_UNIFORM)) {
			return new DustSubSeqDistance(DataDistribution.UNIFORM);
		}else {
			return new DustSubSeqDistance(DataDistribution.NORMAL);
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
	
	public void classicalShapeletTransform(String filePath, String resultPath, String outfile, String dataset) throws Exception {
		Instances test, train;
		test = utilities.ClassifierTools.loadData(filePath + "_TEST");
		train = utilities.ClassifierTools.loadData(filePath + "_TRAIN");
		int min = 3;
		int max = train.numAttributes() - 1;
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		System.out.println("Running " + this.execMode.getName() + " on " + dataset + " \n\tmin=" + min + " max=" + max + " increment=" + lenghtIncrement);
		
		ShapeletTransform transform = new ShapeletTransform();
		transform.setClassValue(new BinaryClassValue());
		transform.setSubSeqDistance(new SubSeqDistance());
		transform.setShapeletMinAndMax(min, max);
		transform.setLengthIncrement(lenghtIncrement);
		transform.useCandidatePruning();
		transform.setNumberOfShapelets(train.numInstances() * 10);
		transform.setQualityMeasure(ShapeletQualityChoice.INFORMATION_GAIN);
		transform.setLogOutputFile(resultPath + dataset + File.separator + "Shapelets.csv");
		transform.supressOutput();

		long startTime = bean.getCurrentThreadUserTime();

		Instances tranTrain = transform.process(train);
		Instances tranTest = transform.process(test);

		long endTime = bean.getCurrentThreadUserTime();
		
		AbstractClassifier stClf = getClassifier();
		
		stClf.buildClassifier(tranTrain);
		double acc = ClassifierTools.accuracy(tranTest, stClf);
		long duration = (long) ((endTime - startTime) * 1e-9);

		System.out.println("\tAccuracy: " + acc + ", duration: " + duration + " sec");
		
		String content = dataset + "," + train.numInstances() + "," + test.numInstances() + ","
				+ train.numAttributes() + "," + train.numClasses() + "," + acc + "," + duration + "," + min + "," + max + "," + lenghtIncrement
				+ "\n";
		writeResult(resultPath + outfile, content);
	}
	
	public void uShapeletTransformUDT(String filePath, String resultPath, String outfile, String dataset) throws Exception {
		Instances test, train;
		test = utilities.ClassifierTools.loadData(filePath + "_TEST");
		train = utilities.ClassifierTools.loadData(filePath + "_TRAIN");
		int min = 3;
		int max = train.numAttributes() - 1;
		long duration;
		double acc;
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		System.out.println("Running " + this.execMode.getName() + " on " + dataset + "\n\tmin=" + min + " max=" + max + " increment=" + lenghtIncrement);
		
		UShapeletTransform utransform = new UShapeletTransform();
		utransform.setClassValue(new BinaryClassValue());
		utransform.setSubSeqDistance(new USubSeqDistance(isGaussian));
		utransform.setShapeletMinAndMax(min, max);
		utransform.setLengthIncrement(lenghtIncrement);
		utransform.useCandidatePruning();
		utransform.setNumberOfShapelets(train.numInstances() * 10);
		utransform.setQualityMeasure(ShapeletQualityChoice.INFORMATION_GAIN);
		utransform.setLogOutputFile(resultPath + dataset + File.separator + "Shapelets.csv");
		utransform.supressOutput();

		long startTime = bean.getCurrentThreadUserTime(), endTime;

		Instances errorTrain, errorTest;
		errorTrain = utilities.ClassifierTools.loadData(filePath + "_NOISE_TRAIN");
		errorTest = utilities.ClassifierTools.loadData(filePath + "_NOISE_TEST");
		normalizeErrors(errorTest, test);
		normalizeErrors(errorTrain, train);

		UInstances uTranTrain = flatInstance2UInstance(utransform.process(train, errorTrain));
		UInstances uTranTest = flatInstance2UInstance(utransform.process(test, errorTest));
		
//		System.out.println("UInstance 0: " + uTranTest.getInstance(0));
		
		UDT udtClf = new UDT();
		
		System.out.println("Before train");
		udtClf.printUDT();
		
		udtClf.buildClassifier(uTranTrain);
		acc = udtClf.accuracy(uTranTest);
		
		System.out.println("After train");
		udtClf.printUDT();
			
		endTime = bean.getCurrentThreadUserTime();
		duration = (long) ((endTime - startTime) * 1e-9);
		System.out.println("\tAccuracy: " + acc + ", duration: " + duration + " sec ");
		
		String content = dataset + "," + train.numInstances() + "," + test.numInstances() + ","
				+ train.numAttributes() + "," + train.numClasses() + "," + acc + "," + duration + "," + min + "," + max + "," + lenghtIncrement
				+ "\n";
		writeResult(resultPath + outfile, content);

	}
	
	public void uShapeletTransform(String filePath, String resultPath, String outfile, String dataset) throws Exception {
		Instances test, train;
		test = utilities.ClassifierTools.loadData(filePath + "_TEST");
		train = utilities.ClassifierTools.loadData(filePath + "_TRAIN");
		int min = 3;
		int max = train.numAttributes() - 1;
		long duration;
		double acc;
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		System.out.println("Running " + this.execMode.getName() + " on " + dataset + "\n\tmin=" + min + " max=" + max + " increment=" + lenghtIncrement);
		
		UShapeletTransform utransform = new UShapeletTransform();
		utransform.setClassValue(new BinaryClassValue());
		utransform.setSubSeqDistance(new USubSeqDistance(isGaussian));
		utransform.setShapeletMinAndMax(min, max);
		utransform.setLengthIncrement(lenghtIncrement);
		utransform.useCandidatePruning();
		utransform.setNumberOfShapelets(train.numInstances() * 10);
		utransform.setQualityMeasure(ShapeletQualityChoice.INFORMATION_GAIN);
		utransform.setLogOutputFile(resultPath + dataset + File.separator + "Shapelets.csv");
		utransform.supressOutput();

		long startTime = bean.getCurrentThreadUserTime(), endTime;

		Instances errorTrain, errorTest;
		errorTrain = utilities.ClassifierTools.loadData(filePath + "_NOISE_TRAIN");
		errorTest = utilities.ClassifierTools.loadData(filePath + "_NOISE_TEST");
		normalizeErrors(errorTest, test);
		normalizeErrors(errorTrain, train);

		Instances uTranTrain = utransform.process(train, errorTrain);
		Instances uTranTest = utransform.process(test, errorTest);
		
		AbstractClassifier clf = getClassifier();
		
		if(execMode.equals(ExecMode.UST_GAUSS)) {
			computeAttrMean(uTranTrain);
			Instances gaussianTrain = makeDataset(uTranTrain);
			Instances gaussianTest = makeDataset(uTranTest);
			clf.buildClassifier(gaussianTrain);
			acc = ClassifierTools.accuracy(gaussianTest, clf);
		} else if(execMode.equals(ExecMode.UST_FLAT)) {
//			System.out.println("Flat Instance 0: " + uTranTest.instance(0));
			clf.buildClassifier(uTranTrain);
			acc = ClassifierTools.accuracy(uTranTest, clf);
		} else {
			AbstractClassifier gaussClf = getClassifier();
			
			Instances gaussianTrain = makeDataset(uTranTrain);
			Instances gaussianTest = makeDataset(uTranTest);
			gaussClf.buildClassifier(gaussianTrain);
			
			clf.buildClassifier(uTranTrain);
			
			Instances combinedTrain = makeUSTInstance(clf, gaussClf, uTranTrain, gaussianTrain);
			Instances combinedTest = makeUSTInstance(clf, gaussClf, uTranTest, gaussianTest);
			clf.buildClassifier(combinedTrain);
			acc = ClassifierTools.accuracy(combinedTest, clf);
		}
		
		endTime = bean.getCurrentThreadUserTime();
		duration = (long) ((endTime - startTime) * 1e-9);
		System.out.println("\tAccuracy: " + acc + ", duration: " + duration + " sec");
		
		String content = dataset + "," + train.numInstances() + "," + test.numInstances() + ","
				+ train.numAttributes() + "," + train.numClasses() + "," + acc + "," + duration + "," + min + "," + max + "," + lenghtIncrement
				+ "\n";
		writeResult(resultPath + outfile, content);

	}
	
	public void dustShapeletTransform(String filePath, String resultPath, String outfile, String dataset) throws Exception {
		Instances test, train;
		test = utilities.ClassifierTools.loadData(filePath + "_TEST");
		train = utilities.ClassifierTools.loadData(filePath + "_TRAIN");
		int min = 3;
		int max = train.numAttributes() - 1;
		long duration;
		double acc;
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		System.out.println("Running " + this.execMode.getName() + " on " + dataset + "\n\tmin=" + min + " max=" + max + " increment=" + lenghtIncrement);
		
		UShapeletTransform utransform = new UShapeletTransform();
		utransform.setClassValue(new BinaryClassValue());
		utransform.setSubSeqDistance(getDustSubSeqDistance());
		utransform.setShapeletMinAndMax(min, max);
		utransform.setLengthIncrement(lenghtIncrement);
		utransform.useCandidatePruning();
		utransform.setNumberOfShapelets(train.numInstances() * 10);
		utransform.setQualityMeasure(ShapeletQualityChoice.INFORMATION_GAIN);
		utransform.setLogOutputFile(resultPath + dataset + File.separator + "Shapelets.csv");
		utransform.supressOutput();

		long startTime = bean.getCurrentThreadUserTime(), endTime;

		Instances errorTrain, errorTest;
		errorTrain = utilities.ClassifierTools.loadData(filePath + "_NOISE_TRAIN");
		errorTest = utilities.ClassifierTools.loadData(filePath + "_NOISE_TEST");

		Instances uTranTrain = utransform.process(train, errorTrain);
		Instances uTranTest = utransform.process(test, errorTest);
		
		AbstractClassifier clf = getClassifier();
		
		clf.buildClassifier(uTranTrain);
		acc = ClassifierTools.accuracy(uTranTest, clf);
		
		endTime = bean.getCurrentThreadUserTime();
		duration = (long) ((endTime - startTime) * 1e-9);
		System.out.println("\tAccuracy: " + acc + ", duration: " + duration + " sec");
		
		String content = dataset + "," + train.numInstances() + "," + test.numInstances() + ","
				+ train.numAttributes() + "," + train.numClasses() + "," + acc + "," + duration + "," + min + "," + max + "," + lenghtIncrement
				+ "\n";
		writeResult(resultPath + outfile, content);

	}

	public void shapeletTransform(String dataset, String datasetfolder, String resultfolder_name)
			throws Exception {
		final String resampleLocation = datasetfolder;
		final String resultPath = new File(datasetfolder).getParent() + File.separator + resultfolder_name
				+ File.separator;
		final String filePath = resampleLocation + File.separator + dataset + File.separator + dataset;

		String outfile = "results.csv";
		File file = new File(resultPath + outfile);
		file.getParentFile().mkdirs();
		FileWriter fileWriter = new FileWriter(file);
		String header = "dataset,train_size,test_size,series_length,no_classes,acc,duration,min_shp,max_shp,increment\n";
		fileWriter.write(header);
		fileWriter.close();
		
		if(ExecMode.ST.equals(execMode)) {
//			classicalShapeletTransform(filePath, resultPath, outfile, dataset);
		} else if(ExecMode.DUST_NORMAL.equals(execMode) || ExecMode.DUST_UNIFORM.equals(execMode)){
//			dustShapeletTransform(filePath, resultPath, outfile, dataset);
		}else if (ExecMode.UST_UDT.equals(execMode)){
//			uShapeletTransformUDT(filePath, resultPath, outfile, dataset);
		} else {
			uShapeletTransform(filePath, resultPath, outfile, dataset);
		}
		
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
	
	public UInstances flatInstance2UInstance(Instances flatInstances) throws Exception {
		UInstances output = new UInstances();
	    for(int i = 0; i < flatInstances.numInstances(); i++) {
	    	output.addInstance(new UInstance(flatInstances.instance(i)));
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

	public void shapeletTransform(int nb_of_thread, String datasetfolder, String resultFolderName) {
		try {
			final String resampleLocation = datasetfolder;
			final String resultPath = new File(datasetfolder).getParent() + File.separator + resultFolderName
					+ File.separator;
			File folder = new File(resampleLocation);
			final String[] datasets = folder.list();

			String outfile = "results.csv";
			File file = new File(resultPath + outfile);
			file.getParentFile().mkdirs();
			FileWriter fileWriter = new FileWriter(file);
			String header = "dataset,train_size,test_size,series_length,no_classes,acc,duration,min_shp,max_shp,increment\n";
			fileWriter.write(header);
			fileWriter.close();

			BlockingQueue<String> blockingQueue = new LinkedBlockingDeque<>();
			ExecutorService executor = Executors.newFixedThreadPool(nb_of_thread);

			Runnable consumerTask = () -> {
				try {

					String dataset;

					while ((dataset = blockingQueue.poll()) != null) {
						final String filePath = resampleLocation + File.separator + dataset + File.separator + dataset;
						if(ExecMode.ST.equals(execMode)) {
//							classicalShapeletTransform(filePath, resultPath, outfile, dataset);
						} else if(ExecMode.DUST_NORMAL.equals(execMode) || ExecMode.DUST_UNIFORM.equals(execMode)){
//							dustShapeletTransform(filePath, resultPath, outfile, dataset);
						}else {
							uShapeletTransform(filePath, resultPath, outfile, dataset);
						}

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
//		String datasetfolder = argv[0];
//		String resultFolderName = argv[1];
//		int lenghtIncrement = Integer.parseInt(argv[2]);
//		final int MAX_NB_THREAD = 20;
//		
//		System.out.println(datasetfolder);
//		test.setClassifier(argv[3]);
//		
//		switch (argv[4]) {
//		case "DUST_UNIFORM":
//			test.setExecMode(ExecMode.DUST_UNIFORM);
//			break;
//		case "DUST_NORMAL":
//			test.setExecMode(ExecMode.DUST_NORMAL);
//			break;
//		case "UST_FLAT":
//			test.setExecMode(ExecMode.UST_FLAT);
//			break;
//		case "UST_GAUSS":
//			test.setExecMode(ExecMode.UST_GAUSS);
//			break;
//		case "UST_FLAT_GAUSS":
//			test.setExecMode(ExecMode.UST_FLAT_GAUSS);
//			break;
//		default:
//			test.setExecMode(ExecMode.ST);
//			break;
//		}
//		
//		if(argv[4].startsWith("UST")){
//			test.setGaussian("gaussian".equals(argv[5]));
//		}
//		
//		test.setLenghtIncrement(lenghtIncrement);
//		
//		test.shapeletTransform(MAX_NB_THREAD, datasetfolder, resultFolderName);
//	} 

	public static void main(String[] argv) {
		UShapeletTransformTest test = new UShapeletTransformTest();
		String datasetfolder_noise = "/home/mimbouop/Codes/ust/Source-code/uncertain-datasets/0_3";
		String dataset = "Chinatown";
		
//		List<UOrderLineObj> list = new ArrayList<UOrderLineObj>();
//		list.add(new UOrderLineObj(new UDistance(.2, .01), 0));
//		list.add(new UOrderLineObj(new UDistance(.5, .01), 0));
//		list.add(new UOrderLineObj(new UDistance(2, 1), 0));
//		list.add(new UOrderLineObj(new UDistance(4, .01), 0));
//		
//		Utils.sort(list, 0, list.size() - 1);
//		
//		for(UOrderLineObj l: list) {
//			System.out.println(l);
//		}

		System.out.println("Dataset folder :" + datasetfolder_noise);
		test.setClassifier("DT");
		test.setLenghtIncrement(1);
		test.setGaussian(true);
		
		for(ExecMode em : ExecMode.values()) {
			test.setExecMode(em);
			try {
				test.shapeletTransform(dataset, datasetfolder_noise, "result_new");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("\n");
		}
	}
	
	
//	java -cp UST.jar u_shapelet_transform.UShapeletTransformTest /home/etud/mbouopda/stage-limos/uncertain-dataset-0_2 results0_2/ust_gauss 1 DT UST_GAUSS gaussian
//  ../jdk-11/bin/java -cp UST.jar u_shapelet_transrm.UShapeletTransformTest /home/etud/mbouopda/stage-limos/uncertain-datasets/0_2 results0_2/ust_flat_p 1 DT UST_FLAT g

}
