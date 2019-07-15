package u_shapelet_transform;

import java.io.Serializable;

import timeseriesweka.filters.shapelet_transforms.OrderLineObj;

public class UOrderLineObj implements Comparable<UOrderLineObj>, Serializable {

	private UDistance udistance;
    private double classVal;
    /**
     * Constructor to build an orderline object with a given distance and class value
     * @param udistance distance from the obj to the shapelet that is being assessed
     * @param classVal the class value of the object that is represented by this OrderLineObj
     */
    public UOrderLineObj(UDistance distance, double classVal){
        this.udistance = new UDistance(distance);
        this.classVal = classVal;
    }

    /**
     * Accessor for the distance field
     * @return this UOrderLineObj's distance
     */
    public UDistance getDistance(){
        return this.udistance;
    }

    /**
     * Accessor for the class value field of the object
     * @return this OrderLineObj's class value
     */
    public double getClassVal(){
        return this.classVal;
    }

    /**
     * Mutator for the distance field
     * @param distance new distance for this OrderLineObj
     */
    public void setDistance(UDistance distance){
        this.udistance = new UDistance(distance);
    }
    
    /**
     * Mutator for the class value field of the object
     * @param classVal new class value for this OrderLineObj
     */
    public void setClassVal(double classVal){
        this.classVal = classVal;
    }
    
    /**
     * Comparator for two OrderLineObj objects, used when sorting an orderline
     * @param o the comparison OrderLineObj
     * @return the order of this compared to o: -1 if less, 0 if even, and 1 if greater.
     */
    @Override
    public int compareTo(UOrderLineObj o) {
        // return distance - o.distance. compareTo doesnt care if its -1 or -inf. likewise +1 or +inf.  
        return this.udistance.compareTo(o.getDistance());
    }
    
    @Override
    public String toString()
    {
        return udistance + "," + classVal;
    }

}
