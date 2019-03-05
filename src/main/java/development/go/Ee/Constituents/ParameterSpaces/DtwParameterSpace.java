package development.go.Ee.Constituents.ParameterSpaces;

import development.go.Indexed.DoubleLinearInterpolator;
import development.go.Indexed.IndexConsumer;
import timeseriesweka.measures.dtw.Dtw;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DtwParameterSpace extends ParameterSpace<Dtw> {

    private final DoubleLinearInterpolator warpingWindowValues = new DoubleLinearInterpolator(0d, 1d, 1);
    private double warpingWindowValue;
    private final IndexConsumer<Double> warpingWindowParameter = new IndexConsumer<>(warpingWindowValues, v -> warpingWindowValue = v);

    protected DoubleLinearInterpolator getWarpingWindowValues( ){
        return warpingWindowValues;
    }

    @Override
    protected List<IndexConsumer<?>> setupParameters(final Instances instances) {
        warpingWindowValues.setSize(101);
        return new ArrayList<>(Collections.singletonList(warpingWindowParameter));
    }

    @Override
    public void configure(Dtw subject) {
        subject.setWarpingWindow(warpingWindowValue);
    }

    @Override
    protected Dtw get() {
        return new Dtw();
    }

}
