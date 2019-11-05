package u_shapelet_transform;

import java.io.Serializable;

import timeseriesweka.filters.shapelet_transforms.Shapelet;
import timeseriesweka.filters.shapelet_transforms.ShapeletCandidate;
import weka.core.Instance;
import weka.core.Instances;

public class USubSeqDistance extends BaseUSubSeqDistance {
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

}
