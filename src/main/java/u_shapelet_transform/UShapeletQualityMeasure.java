package u_shapelet_transform;

import java.util.List;

import utilities.class_counts.ClassCounts;

public interface UShapeletQualityMeasure {
	 public double calculateQuality(List<UOrderLineObj> orderline, ClassCounts classDistribution);

     public double calculateSeperationGap(List<UOrderLineObj> orderline);
}
