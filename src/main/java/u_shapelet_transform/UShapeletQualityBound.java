package u_shapelet_transform;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import utilities.class_counts.ClassCounts;
import utilities.class_counts.TreeSetClassCounts;

public abstract class UShapeletQualityBound implements Serializable {
	 /**
     * Best quality observed so far, which is used for determining if the
     * candidate can be pruned
     */
    protected double bsfQuality;
    /**
     * Orderline of the observed distance, class pairs
     */
    protected List<UOrderLineObj> orderLine;
    /**
     * Class distribution of the observed distance, class pairs
     */
    protected ClassCounts orderLineClassDist;
    /**
     * Class distribution of the dataset, which currently being processed
     */
    protected ClassCounts parentClassDist;
    /**
     * Number of instances in the dataset, which is currently being processed
     */
    protected int numInstances;
    /**
     * The percentage of data point that must be in the observed orderline
     * before the bounding mechanism is can be invoked
     */
    protected int percentage;

    /**
     *
     * @param classDist
     * @param percentage
     */
    protected void initParentFields(ClassCounts classDist, int percentage) {
        //Initialize the fields
        bsfQuality = Double.MAX_VALUE;
        orderLine = new ArrayList<>();

        orderLineClassDist = new TreeSetClassCounts();
        parentClassDist = classDist;
        this.percentage = percentage;

        //Initialize orderline class distribution
        numInstances = 0;
        for (Double key : parentClassDist.keySet()) {
            orderLineClassDist.put(key, 0);
            numInstances += parentClassDist.get(key);
        }
    }

    /**
     * Method to set the best quality so far of the shapelet
     *
     * @param quality quality of the best so far quality observed
     */
    public void setBsfQuality(double quality) {
        bsfQuality = quality;
    }

    /**
     * Method to update the ShapeletQualityBound with newly observed
     * OrderLineObj
     *
     * @param orderLineObj newly observed OrderLineObj
     */
    public void updateOrderLine(UOrderLineObj orderLineObj) {
        //Update classDistribution of unprocessed elements
        orderLineClassDist.put(orderLineObj.getClassVal(), orderLineClassDist.get(orderLineObj.getClassVal()) + 1);

        //use a binarySearch to update orderLine - rather than a O(n) search.
        int index = Collections.binarySearch(orderLine, orderLineObj);
        if (index < 0) {
            index *= -1;
            index -= 1;
        }
        orderLine.add(index, orderLineObj);

    }

    /**
     * Method to calculate the quality bound for the current orderline
     *
     * @return quality bound for the current orderline
     */
    protected abstract double calculateBestQuality();

    /**
     * Method to check if the current candidate can be pruned
     *
     * @return true if candidate can be pruned otherwise false
     */
    public boolean pruneCandidate() {
            //Check if the required amont of data has been observed and
        //best quality so far set
        if (bsfQuality == Double.MAX_VALUE || orderLine.size() * 100 / numInstances <= percentage) {
            return false;
        }

        //The precondition is met, so quality bound can be computed
        return calculateBestQuality() <= bsfQuality;
    }
}
