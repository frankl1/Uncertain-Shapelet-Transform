package timeseriesweka.Sampling;

import weka.core.Instance;

import java.util.List;

public class PredefinedSampler extends RandomRoundRobinSampler {

    public void setPredefinedInstances(final List<Instance> instances) {
        super.setInstances(instances);
    }

    @Override
    public void setInstances(final List<Instance> instances) {

    }
}
