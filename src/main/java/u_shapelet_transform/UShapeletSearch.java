package u_shapelet_transform;

import static utilities.multivariate_tools.MultivariateInstanceTools.channelLength;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

import timeseriesweka.filters.shapelet_transforms.Shapelet;
import timeseriesweka.filters.shapelet_transforms.search_functions.ShapeletSearchOptions;
import timeseriesweka.filters.shapelet_transforms.search_functions.ShapeletSearch.ProcessCandidate;
import weka.core.Instance;
import weka.core.Instances;

public class UShapeletSearch implements Serializable {
	public enum SearchType {
		FULL, FS, GENETIC, RANDOM, LOCAL, MAGNIFY, TIMED_RANDOM, SKIPPING, TABU, REFINED_RANDOM, IMP_RANDOM,
		SUBSAMPLE_RANDOM, SKEWED
	};

	public interface ProcessCandidate {
		public default UShapelet process(Instance candidate, Instance err, int start, int length) {
			return process(candidate, err, start, length, 0);
		}

		public UShapelet process(Instance candidate, Instance err, int start, int length, int dimension);
	}

	ArrayList<String> shapeletsVisited = new ArrayList<>();
	int seriesCount;

	public ArrayList<String> getShapeletsVisited() {
		return shapeletsVisited;
	}

	protected Comparator<UShapelet> comparator;

	public void setComparator(Comparator<UShapelet> comp) {
		comparator = comp;
	}

	protected int seriesLength;
	protected int minShapeletLength;
	protected int maxShapeletLength;

	protected int numDimensions;

	protected int lengthIncrement = 1;
	protected int positionIncrement = 1;

	protected Instances inputData;
	protected Instances inputDataErr;

	transient protected UShapeletSearchOptions options;

	protected UShapeletSearch(UShapeletSearchOptions ops){
	        options = ops;
	        
	        minShapeletLength = ops.getMin();
	        maxShapeletLength = ops.getMax();
	        lengthIncrement = ops.getLengthInc();
	        positionIncrement = ops.getPosInc();      
	        numDimensions = ops.getNumDimensions();
	    }

	public void setMinAndMax(int min, int max) {
		minShapeletLength = min;
		maxShapeletLength = max;
	}

	public int getLengthIncrement() {
		return lengthIncrement;
	}

	public void setLengthIncrement(int lengthIncrement) {
		this.lengthIncrement = lengthIncrement;
	}

	public int getMin() {
		return minShapeletLength;
	}

	public int getMax() {
		return maxShapeletLength;
	}

	public void init(Instances input, Instances err) {
		inputData = input;
		inputDataErr = err;

		// we need to detect whether it's multivariate or univariate.
		// this feels like a hack. BOO.
		// one relational and a class att.
		seriesLength = getSeriesLength();
	}

	public int getSeriesLength() {
		return inputData.numAttributes() >= maxShapeletLength ? inputData.numAttributes()
				: channelLength(inputData) + 1; // we add one here, because lots of code assumes it has a class value on
												// the end/
	}

	// given a series and a function to find a shapelet
	public ArrayList<UShapelet> SearchForShapeletsInSeries(Instance timeSeries, Instance timeSeriesErr, ProcessCandidate checkCandidate) {
		ArrayList<UShapelet> seriesShapelets = new ArrayList<>();

		// for univariate this will just
		for (int length = minShapeletLength; length <= maxShapeletLength; length += lengthIncrement) {
			// for all possible starting positions of that length. -1 to remove classValue
			// but would be +1 (m-l+1) so cancel.
			for (int start = 0; start < seriesLength - length; start += positionIncrement) {
				// for univariate this will be just once.
				for (int dim = 0; dim < numDimensions; dim++) {
					UShapelet shapelet = checkCandidate.process(getTimeSeries(timeSeries, dim), getTimeSeries(timeSeriesErr, dim), start, length, dim);
					if (shapelet != null) {
						seriesShapelets.add(shapelet);
						shapeletsVisited.add(seriesCount + "," + length + "," + start + "," + shapelet.qualityValue);
					}
				}
			}
		}

		seriesCount++;
		return seriesShapelets;
	}

	protected Instance getTimeSeries(Instance timeSeries, int dim) {
		if (numDimensions > 1)
			return utilities.multivariate_tools.MultivariateInstanceTools
					.splitMultivariateInstanceWithClassVal(timeSeries)[dim];
		return timeSeries;
	}
}
