package timeseriesweka.classifiers.AdvancedAbstractClassifier;

import evaluation.storage.ClassifierResults;
import net.sourceforge.sizeof.SizeOf;
import timeseriesweka.classifiers.CheckpointClassifier;
import timeseriesweka.classifiers.ContractClassifier;
import timeseriesweka.classifiers.SaveParameterInfo;
import utilities.OptionsSetter;
import utilities.Reproducible;
import utilities.TrainAccuracyEstimate;
import utilities.Utilities;
import weka.classifiers.Classifier;
import weka.core.Instances;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public interface AdvancedAbstractClassifierInterface extends Serializable, Reproducible, SaveParameterInfo, CheckpointClassifier, ContractClassifier, OptionsSetter, TrainAccuracyEstimate, Classifier {

    public long getMinCheckpointInterval();

    public void setMinCheckpointInterval(long nanoseconds);

    public long getPredictionContract();

    public void setPredictionContract(final long predictionContract);

    public long getTrainContract();

    public void setTrainContract(final long trainContract);

    public long getTestContract();

    public void setTestContract(final long testContract);

    public String getSavePath();

    @Override
    public void setSavePath(String path);

    @Override
    public void copyFromSerObject(final Object obj) throws Exception;

    public void reset();

    public void resetTest();

    public void setSeed(long seed);

    public void setRandom(Random random);

    @Override
    public String getParameters();

    public long getTrainTime();

    public long getTestTime();

    @Override
    public void setTimeLimit(final long nanoseconds);

    public boolean isCvTrain();

    public void setCvTrain(final boolean cvTrain);

    @Override
    public void setFindTrainAccuracyEstimate(final boolean setCV);

    @Override
    public boolean findsTrainAccuracyEstimate();

    @Override
    public void writeCVTrainToFile(final String train);

    @Override
    public ClassifierResults getTrainResults();

    public boolean isCheckpointing();

    public void setCheckpointing(boolean on);

    public boolean setOption(String key, String value);

}
