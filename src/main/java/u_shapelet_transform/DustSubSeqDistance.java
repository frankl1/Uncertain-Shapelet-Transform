package u_shapelet_transform;

public class DustSubSeqDistance extends BaseUSubSeqDistance {
	
	public enum DataDistribution{NORMAL, UNIFORM}
	protected DataDistribution dataDistribution;

	public DustSubSeqDistance(DataDistribution dataDistribution) {
		super();
		this.dataDistribution = dataDistribution;
	}
	
	public double getDivisor(double std) {
		double divisor;
		
		if(dataDistribution.equals(DataDistribution.UNIFORM)) {
			divisor = 2 * std;
		} else {
			divisor = 2 * std * (1 + std * std);
		}
		return divisor;
	}

	public UDistance calculate(double[] timeSeries, double[] timeSeriesErr, int timeSeriesId) {
		double bestDist = Double.MAX_VALUE;
		double sum;
		double[] subseq;
		double[] subseqErr;
		double temp;
		for (int i = 0; i < timeSeries.length - length; i++) {
			sum = 0;
			// get subsequence of two that is the same lengh as one
			subseq = new double[length];
			subseqErr = new double[length];
			System.arraycopy(timeSeries, i, subseq, 0, length);
			System.arraycopy(timeSeriesErr, i, subseqErr, 0, length);

			subseq = zNormalise(subseq, false); // Z-NORM HERE

			for (int j = 0; j < length; j++) {
				// count ops
				count++;
				temp = Math.abs(cand.getShapeletContent()[j] - subseq[j]);
				temp /= getDivisor(Math.max(cand.getShapeletContentErr()[j], subseqErr[j]));
				sum += (temp * temp);
			}
			
			if (sum < bestDist)
            {
				bestDist = sum;
            }
		}
		
		double dist = (bestDist == 0.0) ? 0.0 : (1.0 / length * bestDist);
		return new UDistance(dist, 0);
	}
}
