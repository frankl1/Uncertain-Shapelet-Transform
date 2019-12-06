package u_shapelet_transform;

import org.apache.commons.math3.special.Erf;

public class UDistance implements Comparable<UDistance> {
	double distance;
	double err; // relative error

	public UDistance(double distance, double err) {
		super();
		this.distance = distance;
		this.err = err;
	}

	public UDistance() {
		this.distance = 0;
		this.err = 0;
	}

	public UDistance(UDistance udist) {
		// TODO Auto-generated constructor stub
		this.distance = udist.getDistance();
		this.err = udist.getErr();
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getErr() {
		return err;
	}

	public void setErr(double err) {
		this.err = err;
	}
	
	public double cdf(double x, double mean, double std) {
		double prob;

		prob = (x - mean) / (std * Math.sqrt(2));
		prob = (1 + Erf.erf(prob)) / 2;

		return prob;
	}

	
	public int simpleCompare(UDistance o) {
		if (o.getDistance() == this.distance) {
			if(o.getErr() < this.getErr()) {
				return 1;
			}else if(o.getErr() > this.getErr()) {
				return -1;
			}else {
				return 0;
			}
		} else if (o.getDistance() < this.distance) {
			return 1; // this is greater than o
		} else {
			return -1; // this is lesser than o
		}
	}
	
	public int probabilisticCompare(UDistance o) {
		double var = Math.pow(this.getErr(), 2) + Math.pow(o.getErr(), 2);
		double mean = this.distance - o.getDistance();
		double p = cdf(0, mean, Math.sqrt(var)); // probability of [this - o] being less or equal to 0
//		System.out.println("\t \t proba = " + p);
		if (Math.abs(p - 0.5) <= 0.1) {
			return 0;
		}
		
		if(p > 0.5)
			return -1;
		return 1;
	}

	@Override
	public int compareTo(UDistance o) {
		return probabilisticCompare(o);
//		return simpleCompare(o);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.distance + "(" + this.err + "%)";
	}

}
