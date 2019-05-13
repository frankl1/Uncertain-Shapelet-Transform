package timeseriesweka.filters.shapelet_transforms.distance_functions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import timeseriesweka.filters.shapelet_transforms.Shapelet;
import timeseriesweka.filters.shapelet_transforms.ShapeletTransform;
import timeseriesweka.filters.shapelet_transforms.ShapeletTransformFactory;
import timeseriesweka.filters.shapelet_transforms.search_functions.ShapeletSearch;
import timeseriesweka.filters.shapelet_transforms.ShapeletTransformFactoryOptions;
import timeseriesweka.filters.shapelet_transforms.class_value.BinaryClassValue;
import timeseriesweka.filters.shapelet_transforms.quality_measures.ShapeletQuality.ShapeletQualityChoice;
import timeseriesweka.filters.shapelet_transforms.search_functions.ShapeletSearchOptions;
import utilities.ClassifierTools;
import weka.classifiers.meta.RotationForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.matrix.EigenvalueDecomposition;
import weka.core.matrix.Matrix;

public class FOTS extends SubSeqDistance {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected int w = 4; // windows size
	protected int k = 4; // first k eigen vectors to use
	protected int step = 1; // sliding windows step
	
	public FOTS() {
		super();
	}
	
	
	
	public int getW() {
		return w;
	}



	public FOTS setW(int w) {
		this.w = w;
		return this;
	}



	public int getK() {
		return k;
	}



	public FOTS setK(int k) {
		this.k = k;
		return this;
	}



	public int getStep() {
		return step;
	}



	public FOTS setStep(int step) {
		this.step = step;
		return this;
	}



	@Override
	public void setShapelet(Shapelet shp) {
		// TODO Auto-generated method stub
		super.setShapelet(shp);
		if(this.length < w)
			this.w = this.length;
	}



	@Override
	public void setCandidate(Instance inst, int start, int len, int dim) {
		// TODO Auto-generated method stub
		super.setCandidate(inst, start, len, dim);
		if(this.length < w)
			this.w = this.length;
	}



	public Matrix eigenVectors(Matrix corrMat){
		Matrix result = new Matrix(corrMat.getRowDimension(), this.k);
		EigenvalueDecomposition eigDec = corrMat.eig();
		Matrix eigVecMatrix = eigDec.getV();
		
		int count = 0;
		for(int i=eigVecMatrix.getColumnDimension()-1; i>-1; i--) {
			for (int j=0; j<eigVecMatrix.getRowDimension(); j++) {
				result.set(j, eigVecMatrix.getColumnDimension() - 1 - i, eigVecMatrix.get(j, i));
			}
			count++;
			if(count == this.k)
				break;
		}
		
		return result;
	}

	Matrix auto_corr_matrix(double[] ts) {
		Matrix corr_mat = new Matrix(this.w, this.w);
		double[] temp = new double[this.w];
		
		for (int tau = 0; tau <= ts.length - this.w - this.step + 1; tau += this.step) {
	        System.arraycopy(ts, tau, temp, 0, this.w);
	        corr_mat = corr_mat.plus(outer_product(temp, temp));
		}
		
		//System.out.print(corr_mat);
		
		return corr_mat;
	}
	
	Matrix outer_product(double[] columnVec1, double[] columnVec2){
		double[][] outProd = new double[columnVec1.length][columnVec2.length];
		
		for(int i=0; i<columnVec1.length; i++) {
			for (int j=0; j<columnVec2.length; j++) {
				outProd[i][j] = columnVec1[i]*columnVec2[j];
			}
		}
		
		return new Matrix(outProd);
	}
	
	
	
	@Override
	public double calculate(double[] timeSeries, int timeSeriesId) {
        double bestFots = Double.MAX_VALUE;
        double fots;
        double[] subseq;

        Matrix eigenVectCand = this.eigenVectors(this.auto_corr_matrix(cand.getShapeletContent()));

		
        for (int i = 0; i < timeSeries.length - length; i++)
        {
    		//System.out.println(i+"=>TS LEN="+timeSeries.length + " LEN="+this.length);
            fots = Double.MAX_VALUE;
            // get subsequence of two that is the same lengh as one
            subseq = new double[length];
            System.arraycopy(timeSeries, i, subseq, 0, length);

            subseq = zNormalise(subseq, false); // Z-NORM HERE
            
            Matrix subSeqAutoCorr = this.auto_corr_matrix(subseq);
            
            fots = eigenVectCand.minus(this.eigenVectors(subSeqAutoCorr)).normF();
            
            if (fots < bestFots)
            {
                bestFots = fots;
            }
            count += length;
        }

        return bestFots / this.length;
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
	
	public void shapeletTransform(int nb_of_thread) {
        try {
            final String resampleLocation = "C:\\Users\\mfmbouopda\\Desktop\\stage-m2-limos\\Source-code\\dataset"; 
            final String resultPath = "C:\\Users\\mfmbouopda\\Desktop\\stage-m2-limos\\Source-code\\results" + File.separator;
            File folder = new File(resampleLocation);
            final String[] datasets = folder.list();

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String date = format.format(new Date());
            String outfile = "results-"+date+".csv";
			FileWriter fileWriter = new FileWriter(resultPath + outfile);
            String header = "dataset,train_size,test_size,series_length,no_classes,fots_acc,ed_acc,fots_duration,ed_duration,min_shp,max_shp,increment\n";
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

						int lenghtIncrement = 10;
						// use fold as the seed.
						// train = InstanceTools.subSample(train, 100, fold);

						System.out.println(dataset + " min=" + min + " max=" + max + " increment=" + lenghtIncrement);

						FOTS fots_distance = new FOTS();
						fots_distance.setW(4).setStep(1);

						ShapeletTransform transform = new ShapeletTransform();
						transform.setRoundRobin(true);
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

						System.out.println(dataset + " ED started");
						Instances tranTrain = transform.process(train);
						Instances tranTest = transform.process(test);

						long endTime = bean.getCurrentThreadUserTime();

						RotationForest rot1 = new RotationForest();

						rot1.buildClassifier(tranTrain);
						double ed_acc = ClassifierTools.accuracy(tranTest, rot1);
						long ed_duration = (long) ((endTime - startTime) * 1e-9);

						System.out.println(dataset + " ED finished");

						ShapeletTransform transform1 = new ShapeletTransform();
						transform1.setRoundRobin(true);
						transform1.setClassValue(new BinaryClassValue());
						transform1.setSubSeqDistance(fots_distance);
						transform1.setShapeletMinAndMax(min, max);
						transform.setLengthIncrement(lenghtIncrement);
						transform1.useCandidatePruning();
						transform1.setNumberOfShapelets(train.numInstances() * 10);
						transform1.setQualityMeasure(ShapeletQualityChoice.INFORMATION_GAIN);
						transform1.setLogOutputFile(resultPath + dataset + File.separator + "FOTS_Shapelets.csv");
						transform1.supressOutput();

						startTime = bean.getCurrentThreadUserTime();

						System.out.println(dataset + " FOST started");

						Instances tranTrain1 = transform1.process(train);
						Instances tranTest1 = transform1.process(test);

						endTime = bean.getCurrentThreadUserTime();

						rot1 = new RotationForest();

						rot1.buildClassifier(tranTrain1);
						double fots_acc = ClassifierTools.accuracy(tranTest1, rot1);
						long fots_duration = (long) ((endTime - startTime) * 1e-9);

						System.out.println(dataset + " FOTS finished");

						String content = dataset + "," + train.numInstances() + "," + test.numInstances() + ","
								+ train.numAttributes() + "," + train.numClasses() + "," + fots_acc + "," + ed_acc + ","
								+ fots_duration + "," + ed_duration + "," + min + "," + max + "," + lenghtIncrement
								+ "\n";
						writeResult(resultPath + outfile, content);
			            System.out.println("\tED: Accuracy: " + ed_acc + ", transform duration: " + ed_duration + " sec");
			            System.out.println("\tFOTS: Accuracy: " + fots_acc + ", transform duration: " + fots_duration + " sec");

					}

				}catch (InterruptedException e) {

					e.printStackTrace();

				} catch (Exception ex) {
					// TODO Auto-generated catch block
					Logger.getLogger(ShapeletTransform.class.getName()).log(Level.SEVERE, null, ex);
				}

			};
            
            for (String dataset: datasets) {
                blockingQueue.offer(dataset);
            }
            
            System.out.println("Q.size = " + blockingQueue.size());
            
            for(int i=0; i<nb_of_thread; i++) {
            	executor.execute(consumerTask);
            }
            
            executor.shutdown();
//            while (!executor.awaitTermination(3, TimeUnit.MINUTES)) {
//                System.out.println("\nNot yet. Still waiting for termination\n");
//            }
            
            System.out.println("FINISHED");
        } catch(IOException ex) {
        	ex.printStackTrace();
        	System.err.println(ex.getMessage());
        }
	}

	public static void main(String argv[]) {
		FOTS fots = new FOTS();
		
//		double[] ts = {5, 2, 3, 5, 6, 1, 1, 0, 9, 4};
//		
//		Matrix corr = fots.auto_corr_matrix(ts);
//	
//		System.out.println(corr);
//		
//		System.out.println("\n"+fots.eigenVectors(corr));
		final int MAX_NB_THREAD = 10;
		fots.shapeletTransform(MAX_NB_THREAD);
	}
}
