package u_shapelet_transform;

import java.io.Serializable;

import timeseriesweka.filters.shapelet_transforms.Shapelet;
import timeseriesweka.filters.shapelet_transforms.ShapeletCandidate;
import weka.core.Instance;
import weka.core.Instances;

public class USubSeqDistance extends BaseUSubSeqDistance {
	/**
	 * true if the measures are independent, random and governed by a normal distribution 
	 */
	protected boolean is_gaussian;

	public USubSeqDistance() {
		super();
		this.is_gaussian = true;
	}
	
	public USubSeqDistance(boolean is_gaussian) {
		super();
		this.is_gaussian = is_gaussian;
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
		double temp, tempErr;
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
				if(is_gaussian) {
					tempErr = Math.pow(Math.abs(cand.getShapeletContent()[j]) * cand.getShapeletContentErr()[j], 2);
					tempErr +=  Math.pow(Math.abs(subseq[j]) * subseqErr[j], 2);
					tempErr = Math.sqrt(tempErr);
				} else {
					tempErr = (Math.abs(cand.getShapeletContent()[j]) * cand.getShapeletContentErr()[j] +  Math.abs(subseq[j]) * subseqErr[j]);
				}
				err += (Math.abs(temp) * tempErr / 100);
			}
			
			err = (err * 2) / sum; // relative error
			sum = sum / length;
			
			uDistance = new UDistance(sum, err);
			
			if(uDistance.compareTo(bestDist) == -1) {
				bestDist = new UDistance(uDistance);
			}
		}
		return bestDist;
	}

}
