package timeseriesweka.measures;

import timeseriesweka.measures.ddtw.Ddtw;
import timeseriesweka.measures.dtw.Dtw;
import timeseriesweka.measures.erp.Erp;
import timeseriesweka.measures.lcss.Lcss;
import timeseriesweka.measures.msm.Msm;
import timeseriesweka.measures.twe.Twe;
import timeseriesweka.measures.wddtw.Wddtw;
import timeseriesweka.measures.wdtw.Wdtw;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DistanceMeasureFactory {

    private DistanceMeasureFactory() {

    }

    public static DistanceMeasureFactory getInstance() {
        return INSTANCE;
    }

    private final static DistanceMeasureFactory INSTANCE = new DistanceMeasureFactory();

    private static final Class[] classes = new Class[] {Ddtw.class, Dtw.class, Wdtw.class, Wddtw.class, Lcss.class, Erp.class, Msm.class, Twe.class};

    public String getKey(Class clazz) {
        return clazz.getSimpleName();
    }

    public DistanceMeasure produce(String name) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        for(Class clazz : classes) {
            if(clazz.getSimpleName().equalsIgnoreCase(name)) {
                return (DistanceMeasure) clazz.getDeclaredConstructor().newInstance();
            }
        }
        throw new IllegalArgumentException();
    }
}
