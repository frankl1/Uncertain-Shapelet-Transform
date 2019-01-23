package timeseriesweka.classifiers.ee.constituents.generators;

import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.ddtw.Ddtw;

public class DdtwGenerator extends DtwGenerator {
    @Override
    protected DistanceMeasure getDistanceMeasure() {
        distanceMeasureBox.setContents(new Ddtw());
        return distanceMeasureBox.getContents();
    }
}
