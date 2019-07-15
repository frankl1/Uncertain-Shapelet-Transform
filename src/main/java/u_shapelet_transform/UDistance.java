package u_shapelet_transform;

public class UDistance implements Comparable<UDistance> {
	double distance;
	double err;

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

	@Override
	public int compareTo(UDistance o) {
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

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.distance + "(" + this.err + "%)";
	}

}
