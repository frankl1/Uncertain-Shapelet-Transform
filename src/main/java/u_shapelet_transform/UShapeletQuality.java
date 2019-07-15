package u_shapelet_transform;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import timeseriesweka.filters.shapelet_transforms.OrderLineObj;
import timeseriesweka.filters.shapelet_transforms.quality_measures.FStat;
import timeseriesweka.filters.shapelet_transforms.quality_measures.FStatBound;
import timeseriesweka.filters.shapelet_transforms.quality_measures.InformationGain;
import timeseriesweka.filters.shapelet_transforms.quality_measures.InformationGainBound;
import timeseriesweka.filters.shapelet_transforms.quality_measures.KruskalWallis;
import timeseriesweka.filters.shapelet_transforms.quality_measures.KruskalWallisBound;
import timeseriesweka.filters.shapelet_transforms.quality_measures.MoodsMedian;
import timeseriesweka.filters.shapelet_transforms.quality_measures.MoodsMedianBound;
import timeseriesweka.filters.shapelet_transforms.quality_measures.ShapeletQuality;
import timeseriesweka.filters.shapelet_transforms.quality_measures.ShapeletQualityBound;
import timeseriesweka.filters.shapelet_transforms.quality_measures.ShapeletQualityMeasure;
import timeseriesweka.filters.shapelet_transforms.quality_measures.ShapeletQuality.ShapeletQualityChoice;
import utilities.class_counts.ClassCounts;

public class UShapeletQuality implements Serializable {
	
    public ShapeletQualityChoice getChoice() {
        return choice;
    }

    public UShapeletQualityMeasure getQualityMeasure() {
        return qualityMeasure;
    }

    public Optional<UShapeletQualityBound> getBound() {
        return bound;
    }
    
    ShapeletQualityChoice choice;
    UShapeletQualityMeasure qualityMeasure;
    Optional<UShapeletQualityBound> bound = Optional.empty();
    
    //init static lists of constructors.
    private static final List<Supplier<UShapeletQualityMeasure>> qualityConstructors = createQuality();
    private static final List<BiFunction<ClassCounts, Integer, UShapeletQualityBound>>  boundConstructor = createBound();
    private static List<Supplier<UShapeletQualityMeasure>> createQuality(){
        List<Supplier<UShapeletQualityMeasure>> cons = new ArrayList<>();
        cons.add(UInformationGain::new);
        return cons;
    }
    
    private static List<BiFunction<ClassCounts, Integer, UShapeletQualityBound>> createBound(){
        List<BiFunction<ClassCounts, Integer, UShapeletQualityBound>> cons = new ArrayList();
        cons.add(UInformationGainBound::new);
        return cons;
    }
    
    public UShapeletQuality(ShapeletQualityChoice choice){
        this.choice = choice;
        qualityMeasure = qualityConstructors.get(choice.ordinal()).get();
    }
    
    public void initQualityBound(ClassCounts classDist, int percentage){
        bound = Optional.of(boundConstructor.get(choice.ordinal()).apply(classDist, percentage));
    }
    
    public void setBsfQuality(double bsf){
        if(bound.isPresent())
            bound.get().setBsfQuality(bsf);
    }
    
    public boolean pruneCandidate(){
        return bound.isPresent() && bound.get().pruneCandidate();
    }
    
    public void updateOrderLine(UOrderLineObj obj){
        if(bound.isPresent())
            bound.get().updateOrderLine(obj);
    }
}
