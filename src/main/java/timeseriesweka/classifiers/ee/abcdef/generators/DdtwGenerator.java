package timeseriesweka.classifiers.ee.abcdef.generators;

import timeseriesweka.classifiers.NearestNeighbour;
import timeseriesweka.classifiers.ee.abcdef.*;
import timeseriesweka.classifiers.ee.index.IndexedSupplier;
import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.ddtw.Ddtw;
import timeseriesweka.measures.dtw.Dtw;
import utilities.Box;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DdtwGenerator extends DtwGenerator {
    @Override
    protected DistanceMeasure getDistanceMeasure() {
        distanceMeasureBox.setContents(new Ddtw());
        return distanceMeasureBox.getContents();
    }
}
