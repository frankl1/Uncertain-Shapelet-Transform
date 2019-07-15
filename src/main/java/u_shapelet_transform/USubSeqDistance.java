package u_shapelet_transform;

import java.io.Serializable;

import timeseriesweka.filters.shapelet_transforms.Shapelet;
import timeseriesweka.filters.shapelet_transforms.ShapeletCandidate;
import weka.core.Instance;
import weka.core.Instances;

public class USubSeqDistance implements Serializable {
	public static final double ROUNDING_ERROR_CORRECTION = 0.000000000000001;

	protected Instance candidateInst;
	protected Instance candidateErr;
	protected double[] candidateArray;
	protected double[] candidateErrArray;

	protected UShapelet shapelet;
	protected UShapeletCandidate cand;
	protected int seriesId;
	protected int startPos;
	protected int length;
	protected int dimension;

	protected long count;

	public void init(Instances data) {
		count = 0;
	}

	final void incrementCount() {
		count++;
	}

	public long getCount() {
		return count;
	}

	public UShapeletCandidate getCandidate() {
		return cand;
	}

	public void setShapelet(UShapelet shp) {
		shapelet = shp;
		startPos = shp.startPos;
		cand = shp.getContent();
		length = shp.getLength();
		dimension = shp.getDimension();
	}

	public void setCandidate(Instance inst, Instance err, int start, int len, int dim) {
		// extract shapelet and nomrliase.
		cand = new UShapeletCandidate();
		startPos = start;
		length = len;
		dimension = dim;

		// only call to double array when we've changed series.
		if (candidateInst == null || candidateInst != inst) {
			candidateArray = inst.toDoubleArray();
			candidateInst = inst;
		}
		
		if (candidateErr == null || candidateErr != inst) {
			candidateErrArray = err.toDoubleArray();
			candidateErr = err;
		}

		double[] temp = new double[length];
		double[] tempErr = new double[length];
		// copy the data from the whole series into a candidate.
		System.arraycopy(candidateArray, start, temp, 0, length);
		System.arraycopy(candidateErrArray, start, tempErr, 0, length);
		cand.setShapeletContent(temp, tempErr);

		// znorm candidate here so it's only done once, rather than in each distance
		// calculation
		cand.setShapeletContent(zNormalise(cand.getShapeletContent(), false), cand.getShapeletContentErr());
	}

	public void setSeries(int srsId) {
		seriesId = srsId;
	}

	public UDistance calculate(Instance timeSeries, Instance timeSeriesErr, int timeSeriesId) {
		return calculate(timeSeries.toDoubleArray(), timeSeriesErr.toDoubleArray(), timeSeriesId);
	}

	/**
	 * we take in a start pos, but we also start from 0.
	 * @param timeSeries
	 * @param timeSeriesId
	 * @return and array of two doubles [distance, distance_relative_uncertainty]
	 */
	public UDistance calculate(double[] timeSeries, double[] timeSeriesErr, int timeSeriesId) {
		UDistance bestDist = new UDistance(Double.MAX_VALUE, 0);
		UDistance uDistance;
		double sum;
		double[] subseq;
		double[] subseqErr;
		double temp;
		double err;

		for (int i = 0; i < timeSeries.length - length; i++) {
			sum = 0;
			err = 0;
			// get subsequence of two that is the same lengh as one
			subseq = new double[length];
			subseqErr = new double[length];
			System.arraycopy(timeSeries, i, subseq, 0, length);
			System.arraycopy(timeSeriesErr, i, subseqErr, 0, length);

			subseq = zNormalise(subseq, false); // Z-NORM HERE

			for (int j = 0; j < length; j++) {
				// count ops
				count++;
				temp = (cand.getShapeletContent()[j] - subseq[j]);
				sum = sum + (temp * temp);
				err = err + Math.abs(temp) * ( Math.abs(cand.getShapeletContent()[j]) * cand.getShapeletContentErr()[j] +  Math.abs(subseq[j]) * subseqErr[j]);
			}
			
			err = err * 2 /  length;
			sum = sum / length;
			
			uDistance = new UDistance(sum, err/sum);
			
			if(uDistance.compareTo(bestDist) == -1) {
				bestDist = new UDistance(uDistance);
			}
		}
		return bestDist;
	}

	/**
	 * Z-Normalise a time series
	 *
	 * @param input      the input time series to be z-normalised
	 * @param classValOn specify whether the time series includes a class value
	 *                   (e.g. an full instance might, a candidate shapelet
	 *                   wouldn't)
	 * @return a z-normalised version of input
	 */
	final public double[] zNormalise(double[] input, boolean classValOn) {
		double mean;
		double stdv;

		int classValPenalty = classValOn ? 1 : 0;
		int inputLength = input.length - classValPenalty;

		double[] output = new double[input.length];
		double seriesTotal = 0;
		for (int i = 0; i < inputLength; i++) {
			seriesTotal += input[i];
		}

		mean = seriesTotal / (double) inputLength;
		stdv = 0;
		double temp;
		for (int i = 0; i < inputLength; i++) {
			temp = (input[i] - mean);
			stdv += temp * temp;
		}

		stdv /= (double) inputLength;

		// if the variance is less than the error correction, just set it to 0, else
		// calc stdv.
		stdv = (stdv < ROUNDING_ERROR_CORRECTION) ? 0.0 : Math.sqrt(stdv);

		// System.out.println("mean "+ mean);
		// System.out.println("stdv "+stdv);

		for (int i = 0; i < inputLength; i++) {
			// if the stdv is 0 then set to 0, else normalise.
			output[i] = (stdv == 0.0) ? 0.0 : ((input[i] - mean) / stdv);
		}

		if (classValOn) {
			output[output.length - 1] = input[input.length - 1];
		}

		return output;
	}
}
