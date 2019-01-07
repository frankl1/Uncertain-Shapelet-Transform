package timeseriesweka.measures;

import java.util.PrimitiveIterator;
import java.util.function.DoubleConsumer;
import java.util.stream.DoubleStream;

public class EuclideanMeasure extends Measure {
    @Override
    public double distance(DoubleStream timeSeriesA, DoubleStream timeSeriesB) {

        PrimitiveIterator.OfDouble iteratorA = timeSeriesA.parallel().iterator();
        PrimitiveIterator.OfDouble iteratorB = timeSeriesB.parallel().iterator();
        double distance = 0;
        while(iteratorA.hasNext() && iteratorB.hasNext()) {
            distance += (iteratorA.next().doubleValue() - iteratorB.next().doubleValue());
        }
        iteratorA.forEachRemaining(new DoubleConsumer() {
            @Override
            public void accept(double value) {
                System.out.println(value);
            }
        });
        return distance;
    }

    public static void main(String[] args) {
        EuclideanMeasure euclideanMeasure = new EuclideanMeasure();
        DoubleStream a = DoubleStream.of(1,2,3,4,5);
        DoubleStream b = DoubleStream.of(0,0,0,0);
        System.out.println(euclideanMeasure.distance(a,b));
    }
}
