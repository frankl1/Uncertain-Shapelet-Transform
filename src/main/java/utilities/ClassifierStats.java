package utilities;

import java.io.Serializable;

public class ClassifierStats implements Serializable {
    private double meanAuroc;
    private double nll;
    private double balancedAccuracy;
    private double accuracy;
    private double f1;
    private double mcc;
    private double specificity;
    private double sensitivity;
    private double precision;
    private double recall;
    private long trainTime;

    public long getBenchmark() {
        return benchmark;
    }

    private void setBenchmark(final long benchmark) {
        this.benchmark = benchmark;
    }

    private long benchmark;

    public double getMeanAuroc() {
        return meanAuroc;
    }

    private void setMeanAuroc(final double meanAuroc) {
        this.meanAuroc = meanAuroc;
    }

    public double getNll() {
        return nll;
    }

    private void setNll(final double nll) {
        this.nll = nll;
    }

    public double getBalancedAccuracy() {
        return balancedAccuracy;
    }

    private void setBalancedAccuracy(final double balancedAccuracy) {
        this.balancedAccuracy = balancedAccuracy;
    }

    public double getAccuracy() {
        return accuracy;
    }

    private void setAccuracy(final double accuracy) {
        this.accuracy = accuracy;
    }

    public double getF1() {
        return f1;
    }

    private void setF1(final double f1) {
        this.f1 = f1;
    }

    public double getMcc() {
        return mcc;
    }

    private void setMcc(final double mcc) {
        this.mcc = mcc;
    }

    public double getSpecificity() {
        return specificity;
    }

    private void setSpecificity(final double specificity) {
        this.specificity = specificity;
    }

    public double getSensitivity() {
        return sensitivity;
    }

    private void setSensitivity(final double sensitivity) {
        this.sensitivity = sensitivity;
    }

    public double getPrecision() {
        return precision;
    }

    private void setPrecision(final double precision) {
        this.precision = precision;
    }

    public double getRecall() {
        return recall;
    }

    private void setRecall(final double recall) {
        this.recall = recall;
    }

    public long getTrainTime() {
        return trainTime;
    }

    private void setTrainTime(final long trainTime) {
        this.trainTime = trainTime;
    }

    public long getTestTime() {
        return testTime;
    }

    private void setTestTime(final long testTime) {
        this.testTime = testTime;
    }

    private long testTime;
    
    public ClassifierStats(ClassifierResults results) {
        results.findAllStatsOnce();
        setMeanAuroc(results.meanAUROC);
        setMcc(results.mcc);
        setNll(results.nll);
        setBalancedAccuracy(results.balancedAcc);
        setAccuracy(results.acc);
        setF1(results.f1);
        setPrecision(results.precision);
        setRecall(results.recall);
        setSensitivity(results.sensitivity);
        setSpecificity(results.specificity);
        setTestTime(results.getTestTime());
        setTrainTime(results.getTrainTime());
        setBenchmark(results.getBenchmark());
    }
}
