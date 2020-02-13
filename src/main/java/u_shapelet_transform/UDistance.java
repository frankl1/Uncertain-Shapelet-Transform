package u_shapelet_transform;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.special.Erf;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import org.apache.commons.math3.stat.inference.TestUtils;

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
	
	public int stochasticOrder(UDistance o) {
		int c1=0, c2=0;
		double cdf1, cdf2;
		double min = Math.min(this.distance - this.err, o.distance - o.err);
		double max = Math.max(this.distance + this.err, o.distance + o.err);
		double step = (max - min + 1) / 100;
		
		for(double i = min; i<max; i+=step) {
			cdf1 = this.cdf(i, this.distance, this.err);
			cdf2 = this.cdf(i, o.distance, o.err);
			if (cdf1 != cdf2) {
				if(cdf1 > cdf2)
					c1++;
				if (cdf1 < cdf2)
					c2++;
			}
			
		}
		if (c1 == c2)
			return 0;
		if (c1 > c2)
			return -1;
		return 1;
	}

	@Override
	public int compareTo(UDistance o) {
		return simpleCompare(o);
//		return stochasticOrder(o);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.distance + "(" + this.err + "%)";
	}

}
