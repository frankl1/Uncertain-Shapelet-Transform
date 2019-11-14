package u_shapelet_transform.udt;

public class UAttribute implements Comparable<UAttribute>{
	protected double mean;
	protected double std;
	protected String name;
	protected boolean isLabel;
	
	public UAttribute(double mean, double std) {
		super();
		this.mean = mean;
		this.std = std;
	}
	
	public UAttribute(double mean, double std, String name) {
		super();
		this.mean = mean;
		this.std = std;
		this.name = name;
	}

	public double getMean() {
		return mean;
	}

	public void setMean(double mean) {
		this.mean = mean;
	}

	public double getStd() {
		return std;
	}

	public void setStd(double std) {
		this.std = std;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isLabel() {
		return isLabel;
	}

	public void setLabel(boolean isLabel) {
		this.isLabel = isLabel;
	}
	
	public int getClassLabel() {
		return (int) mean;
	}

	@Override
	public int compareTo(UAttribute inst0) {
		// TODO Auto-generated method stub
		if(mean == inst0.getMean()) {
			return 0;
		} else if (mean < inst0.getMean()) {
			return -1;
		}
		return 1;
	}
	
}
