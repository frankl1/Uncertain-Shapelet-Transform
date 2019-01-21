package timeseriesweka.classifiers.ee.abcdef.generators;

import timeseriesweka.measures.DistanceMeasure;
import timeseriesweka.measures.wddtw.Wddtw;

public class WddtwGenerator extends WdtwGenerator {

    @Override
    protected DistanceMeasure getDistanceMeasure() {
        distanceMeasureBox.setContents(new Wddtw());
        return distanceMeasureBox.getContents();
    }
}
