package u_shapelet_transform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class UShapeletSearchFactory {
	private static final List<Function<UShapeletSearchOptions, UShapeletSearch>> searchConstructors = createSearchConstructors();
    //{FULL, FS, GENETIC, RANDOM, LOCAL, MAGNIFY, TIMED_RANDOM, SKIPPING, TABU, REFINED_RANDOM, IMP_RANDOM, SUBSAMPLE, SKEWED};

    UShapeletSearchOptions options;
    
    public UShapeletSearchFactory(UShapeletSearchOptions ops){
        options = ops;
    }
    
    private static List<Function<UShapeletSearchOptions, UShapeletSearch>> createSearchConstructors(){
        List<Function<UShapeletSearchOptions, UShapeletSearch>> sCons = new ArrayList();
        sCons.add(UShapeletSearch::new);
        return sCons;
    }
    
    public UShapeletSearch getShapeletSearch(){
        return searchConstructors.get(options.getSearchType().ordinal()).apply(options);
    }
    
    
    public static void main(String[] args) {
        System.out.println(new UShapeletSearchFactory(new UShapeletSearchOptions.Builder()
                                                    .setSearchType(UShapeletSearch.SearchType.FULL)
                                                    .build())
                                                    .getShapeletSearch());
    }
}
