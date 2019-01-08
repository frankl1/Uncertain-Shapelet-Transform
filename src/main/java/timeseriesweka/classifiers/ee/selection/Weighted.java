package timeseriesweka.classifiers.ee.selection;

public class Weighted<A> {
    private double weight;
    private final A subject;

    public Weighted(A subject, double weight) {
        this.weight = weight;
        this.subject = subject;
    }

    public void setWeight(double weighting) {
        this.weight = weighting;
    }

    public double getWeight() {
        return weight;
    }

    public A getSubject() {
        return subject;
    }
}
