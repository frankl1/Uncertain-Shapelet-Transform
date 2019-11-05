package u_shapelet_transform;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import timeseriesweka.classifiers.SaveParameterInfo;
import timeseriesweka.filters.shapelet_transforms.OrderLineObj;
import timeseriesweka.filters.shapelet_transforms.Shapelet;
import timeseriesweka.filters.shapelet_transforms.ShapeletCandidate;
import timeseriesweka.filters.shapelet_transforms.ShapeletTransform;
import timeseriesweka.filters.shapelet_transforms.ShapeletTransformFactory;
import timeseriesweka.filters.shapelet_transforms.ShapeletTransformFactoryOptions;
import timeseriesweka.filters.shapelet_transforms.class_value.BinaryClassValue;
import timeseriesweka.filters.shapelet_transforms.class_value.NormalClassValue;
import timeseriesweka.filters.shapelet_transforms.distance_functions.ImprovedOnlineSubSeqDistance;
import timeseriesweka.filters.shapelet_transforms.distance_functions.SubSeqDistance;
import timeseriesweka.filters.shapelet_transforms.quality_measures.ShapeletQuality;
import timeseriesweka.filters.shapelet_transforms.quality_measures.ShapeletQuality.ShapeletQualityChoice;
import timeseriesweka.filters.shapelet_transforms.search_functions.ShapeletSearch;
import timeseriesweka.filters.shapelet_transforms.search_functions.ShapeletSearchFactory;
import timeseriesweka.filters.shapelet_transforms.search_functions.ShapeletSearchOptions;
import utilities.ClassifierTools;
import utilities.class_counts.ClassCounts;
import weka.classifiers.meta.RotationForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.SimpleBatchFilter;

public class UShapeletTransform extends SimpleBatchFilter implements SaveParameterInfo, Serializable{
	//Variables for experiments
    protected static long subseqDistOpCount;
    private boolean removeSelfSimilar = true;
    
    @Override
    public String globalInfo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //this int is used to serliase our position when iterating through a dataset.
    public int casesSoFar;
    
    protected boolean supressOutput; // defaults to print in System.out AS WELL as file, set to true to stop printing to console
    protected int numShapelets;
    protected ArrayList<UShapelet> shapelets;
    protected String ouputFileLocation = "defaultShapeletOutput.txt"; // default store location
    protected boolean recordShapelets; // default action is to write an output file
    protected boolean roundRobin;

    public final static int DEFAULT_NUMSHAPELETS = 100;
    public final static int DEFAULT_MINSHAPELETLENGTH = 3;
    public final static int DEFAULT_MAXSHAPELETLENGTH = 23;

    protected transient UShapeletQuality quality;
    
    /*protected transient QualityMeasures.ShapeletQualityMeasure qualityMeasure;
    protected transient QualityMeasures.ShapeletQualityChoice qualityChoice;
    protected transient QualityBound.ShapeletQualityBound qualityBound;*/
    
    protected boolean useCandidatePruning;
    protected boolean useRoundRobin;

    protected Comparator<UShapelet> shapeletComparator;

    protected BaseUSubSeqDistance subseqDistance;
    protected NormalClassValue classValue;
    protected UShapeletSearch searchFunction;
    protected String serialName;
    protected UShapelet worstShapelet;
    
    protected Instances inputData;
    protected Instances inputDataErr;
    
    protected ArrayList<UShapelet> kShapelets;
    
    protected long count;

    public void setSubSeqDistance(BaseUSubSeqDistance ssd) {
        subseqDistance = ssd;
    }
    
    public long getCount() {
        return count;
    }

    public void setClassValue(NormalClassValue cv) {
        classValue = cv;
    }
    
    public void setSearchFunction(UShapeletSearch shapeletSearch) {
        searchFunction = shapeletSearch;
    }

    public UShapeletSearch getSearchFunction(){
        return searchFunction;
    }
    
    public void setSerialName(String sName) {
        serialName = sName;
    }

    public void useSeparationGap() {
        shapeletComparator = new UShapelet.ReverseSeparationGap();
    }
    
    public void setShapeletComparator(Comparator<UShapelet> comp){
        shapeletComparator = comp;
    }

    public void setUseRoundRobin(boolean b) {
        useRoundRobin = b;
    }
    
    public BaseUSubSeqDistance getSubSequenceDistance(){
        return subseqDistance;
    }

	protected int candidatePruningStartPercentage;

    protected static final double ROUNDING_ERROR_CORRECTION = 0.000000000000001;
    protected int[] dataSourceIDs;

    /**
     * Default constructor; Quality measure defaults to information gain.
     */
    public UShapeletTransform() {
        this(DEFAULT_NUMSHAPELETS, DEFAULT_MINSHAPELETLENGTH, DEFAULT_MAXSHAPELETLENGTH, ShapeletQualityChoice.INFORMATION_GAIN);
    }
    
    /**
     * Constructor for generating a shapelet transform from an ArrayList of
     * Shapelets.
     *
     * @param shapes
     */
    public UShapeletTransform(ArrayList<UShapelet> shapes) {
        this();
        this.shapelets = shapes;
        this.m_FirstBatchDone = true;
        this.numShapelets = shapelets.size();
    }

    /**
     * Single param constructor: Quality measure defaults to information gain.
     *
     * @param k the number of shapelets to be generated
     */
    public UShapeletTransform(int k) {
        this(k, DEFAULT_MINSHAPELETLENGTH, DEFAULT_MAXSHAPELETLENGTH, ShapeletQualityChoice.INFORMATION_GAIN);
    }

    /**
     * Full constructor to create a usable filter. Quality measure defaults to
     * information gain.
     *
     * @param k the number of shapelets to be generated
     * @param minShapeletLength minimum length of shapelets
     * @param maxShapeletLength maximum length of shapelets
     */
    public UShapeletTransform(int k, int minShapeletLength, int maxShapeletLength) {
        this(k, minShapeletLength, maxShapeletLength, ShapeletQualityChoice.INFORMATION_GAIN);

    }

    /**
     * Full, exhaustive, constructor for a filter. Quality measure set via enum,
     * invalid selection defaults to information gain.
     *
     * @param k the number of shapelets to be generated
     * @param minShapeletLength minimum length of shapelets
     * @param maxShapeletLength maximum length of shapelets
     * @param qualityChoice the shapelet quality measure to be used with this
     * filter
     */    
    public UShapeletTransform(int k, int minShapeletLength, int maxShapeletLength, ShapeletQualityChoice qualityChoice) {
        this.numShapelets = k;
        this.shapelets = new ArrayList<>();
        this.m_FirstBatchDone = false;
        this.useCandidatePruning = false;
        this.supressOutput = false;
        this.casesSoFar = 0;
        this.recordShapelets = true; // default action is to write an output file
        this.roundRobin = false;
        this.useRoundRobin = false;
        this.shapeletComparator = new UShapelet.LongOrder();
        this.kShapelets = new ArrayList<>();

        setQualityMeasure(qualityChoice);
        this.subseqDistance = new USubSeqDistance();
        this.classValue = new NormalClassValue();
        
        UShapeletSearchOptions sOp = new UShapeletSearchOptions.Builder().setMin(minShapeletLength).setMax(maxShapeletLength).build();   
        this.searchFunction = new UShapeletSearchFactory(sOp).getShapeletSearch();
    }

    /**
     * Returns the set of shapelets for this transform as an ArrayList.
     *
     * @return An ArrayList of Shapelets representing the shapelets found for
     * this Shapelet Transform.
     */
    public ArrayList<UShapelet> getShapelets() {
        return this.shapelets;
    }

    /**
     * Set the transform to round robin the data or not. This transform defaults
     * round robin to false to keep the instances in the same order as the
     * original data. If round robin is set to true, the transformed data will
     * be reordered which can make it more difficult to use the ensemble.
     *
     * @param val
     */
    public void setRoundRobin(boolean val) {
        this.roundRobin = val;
    }

    /**
     * Supresses filter output to the console; useful when running timing
     * experiments.
     */
    public void supressOutput() {
        this.supressOutput = true;
    }

    /**
     * Use candidate pruning technique when checking candidate quality. This
     * speeds up the transform processing time.
     */
    public void useCandidatePruning() {
        this.useCandidatePruning = true;
        this.candidatePruningStartPercentage = 10;
    }

    /**
     * Use candidate pruning technique when checking candidate quality. This
     * speeds up the transform processing time.
     *
     * @param percentage the percentage of data to be precocessed before pruning
     * is initiated. In most cases the higher the percentage the less effective
     * pruning becomes
     */
    public void useCandidatePruning(int percentage) {
        this.useCandidatePruning = true;
        this.candidatePruningStartPercentage = percentage;
    }

    /**
     * Mutator method to set the number of shapelets to be stored by the filter.
     *
     * @param k the number of shapelets to be generated
     */
    public void setNumberOfShapelets(int k) {
        this.numShapelets = k;
    }

    /**
     *
     * @return
     */
    public int getNumberOfShapelets() {
        return numShapelets;
    }

    /**
     * Turns off log saving; useful for timing experiments where speed is
     * essential.
     */
    public void turnOffLog() {
        this.recordShapelets = false;
    }

    /**
     * Set file path for the filter log. Filter log includes shapelet quality,
     * seriesId, startPosition, and content for each shapelet.
     *
     * @param fileName the updated file path of the filter log
     */
    public void setLogOutputFile(String fileName) {
        this.recordShapelets = true;
        this.ouputFileLocation = fileName;
    }

    /**
     * Mutator method to set the minimum and maximum shapelet lengths for the
     * filter.
     *
     * @param minShapeletLength minimum length of shapelets
     * @param maxShapeletLength maximum length of shapelets
     */
    public void setShapeletMinAndMax(int min, int max) {
        searchFunction.setMinAndMax(min, max);
    }

    /**
     * Mutator method to set the quality measure used by the filter. As with
     * constructors, default selection is information gain unless another valid
     * selection is specified.
     *
     * @return
     */
    public ShapeletQualityChoice getQualityMeasure() {
        return quality.getChoice();
    }

    /**
     *
     * @param qualityChoice
     */
    public void setQualityMeasure(ShapeletQualityChoice qualityChoice) {
        quality = new UShapeletQuality(qualityChoice);
    }
    
        /**
     *
     * @param classDist
     * @return
     */
    protected void initQualityBound(ClassCounts classDist) {
        if (!useCandidatePruning) return;
        quality.initQualityBound(classDist, candidatePruningStartPercentage);
    }

    /**
     *
     * @param f
     */
    public void setCandidatePruning(boolean f) {
        this.useCandidatePruning = f;
        this.candidatePruningStartPercentage = f ? 10 : 100;
    }

    /**
     * Sets the format of the filtered instances that are output. I.e. will
     * include k attributes each shapelet distance and a class value
     *
     * @param inputFormat the format of the input data
     * @return a new Instances object in the desired output format
     */
    //TODO: Fix depecrated FastVector
    @Override
    protected Instances determineOutputFormat(Instances inputFormat) throws IllegalArgumentException {

        if (this.numShapelets < 1) {
            
            System.out.println(this.numShapelets);
            throw new IllegalArgumentException("ShapeletFilter not initialised correctly - please specify a value of k that is greater than or equal to 1");
        }

        //Set up instances size and format.
        //int length = this.numShapelets;
        int length = this.shapelets.size();
        FastVector atts = new FastVector();
        String name;
        for (int i = 0; i < length; i++) {
            name = "Shapelet_" + i;
            atts.addElement(new Attribute(name));
        }
        
        for (int i = length; i < 2*length; i++) {
            name = "Err_" + i;
            atts.addElement(new Attribute(name));
        }

        if (inputFormat.classIndex() >= 0) {
            //Classification set, set class
            //Get the class values as a fast vector
            Attribute target = inputFormat.attribute(inputFormat.classIndex());

            FastVector vals = new FastVector(target.numValues());
            for (int i = 0; i < target.numValues(); i++) {
                vals.addElement(target.value(i));
            }
            atts.addElement(new Attribute(inputFormat.attribute(inputFormat.classIndex()).name(), vals));
        }
        Instances result = new Instances("Shapelets" + inputFormat.relationName(), atts, inputFormat.numInstances());
        if (inputFormat.classIndex() >= 0) {
            result.setClassIndex(result.numAttributes() - 1);
        }
        return result;
    }

    protected void inputCheck(Instances dataInst) throws IllegalArgumentException {
        if (numShapelets < 1) {
            throw new IllegalArgumentException("Number of shapelets initialised incorrectly - please select value of k (Usage: setNumberOfShapelets");
        }

        int maxPossibleLength;
        maxPossibleLength = dataInst.instance(0).numAttributes();

        if (dataInst.classIndex() >= 0) {
            maxPossibleLength -= 1;
        }
    }

    /**
     * The main logic of the filter; when called for the first time, k shapelets
     * are extracted from the input Instances 'data'. The input 'data' is
     * transformed by the k shapelets, and the filtered data is returned as an
     * output.
     * <p>
     * If called multiple times, shapelet extraction DOES NOT take place again;
     * once k shapelets are established from the initial call to process(), the
     * k shapelets are used to transform subsequent Instances.
     * <p>
     * Intended use:
     * <p>
     * 1. Extract k shapelets from raw training data to build filter;
     * <p>
     * 2. Use the filter to transform the raw training data into transformed
     * training data;
     * <p>
     * 3. Use the filter to transform the raw testing data into transformed
     * testing data (e.g. filter never extracts shapelets from training data,
     * therefore avoiding bias);
     * <p>
     * 4. Build a classifier using transformed training data, perform
     * classification on transformed test data.
     *
     * @param data the input data to be transformed (and to find the shapelets
     * if this is the first run)
     * @return the transformed representation of data, according to the
     * distances from each instance to each of the k shapelets
     */
    public Instances process(Instances data, Instances dataError) throws IllegalArgumentException {
        inputData = data;
        inputDataErr = dataError;
        dataError.setClassIndex(-1);
        inputDataErr.setClassIndex(-1);

        //check the input data is correct and assess whether the filter has been setup correctly.
        inputCheck(data);
        
        //checks if the shapelets haven't been found yet, finds them if it needs too.
        if (!m_FirstBatchDone) {
            trainShapelets(data, dataError);
            //we log the count from the subseqdistance before we reset it in the transform.
            //we only care about the count from the train.
            count = subseqDistance.getCount();
        }

        //build the transformed dataset with the shapelets we've found either on this data, or the previous training data
        return buildTansformedDataset(data, dataError);
    }

    protected void trainShapelets(Instances data, Instances dataErr) {
        //we might round robin the data in here. So we need to override the input data with the new ordering.
        inputData = initDataSource(data);
        
        searchFunction.setComparator(shapeletComparator);
        searchFunction.init(inputData, inputDataErr);
                //setup subseqDistance
        subseqDistance.init(inputData);
        //setup classsValue
        classValue.init(inputData);
                
        shapelets = findBestKShapeletsCache(inputData, inputDataErr); // get k shapelets
        m_FirstBatchDone = true;

        outputPrint(shapelets.size() + " Shapelets have been generated");
        
        //we don't need to undo the roundRobin because we clone the data into a different order.
    }
    
    private Instances initDataSource(Instances data) {

        int dataSize = data.numInstances();
        // shapelets discovery has not yet been caried out, so this must be training data
        dataSourceIDs = new int[dataSize];
        
        Instances dataset = data;
        if (roundRobin) {
            //Reorder the data in round robin order
            dataset = roundRobinData(data, dataSourceIDs);
        } else {
            for (int i = 0; i < dataSize; i++) {
                dataSourceIDs[i] = i;
            }
        }
        
        return dataset;
    }

    //given a set of instances transform it by the internal shapelets.
    public Instances buildTansformedDataset(Instances data, Instances dataErr) {
        
        //Reorder the training data and reset the shapelet indexes
        Instances output = determineOutputFormat(data);

        //init out data for transforming.
        subseqDistance.init(inputData);
        //setup classsValue
        classValue.init(inputData);

        UShapelet s;
        // for each data, get distance) to each shapelet and create new instance
        int size = shapelets.size();
        int dataSize = data.numInstances();

        //create our data instances
        for (int j = 0; j < dataSize; j++) {
            output.add(new DenseInstance(2*size + 1));
        }

        UDistance dist;
        for (int i = 0; i < size; i++) {
            s = shapelets.get(i);
            subseqDistance.setShapelet(s);

            for (int j = 0; j < dataSize; j++) {
                dist = subseqDistance.calculate(data.instance(j), dataErr.instance(j), j);
                output.instance(j).setValue(i, dist.getDistance());
                output.instance(j).setValue(i + size, dist.getErr());
            }
        }

        //do the classValues.
        for (int j = 0; j < dataSize; j++) {
            //we always want to write the true ClassValue here. Irrelevant of binarised or not.
            output.instance(j).setValue(2*size, data.instance(j).classValue());
        }

        return output;
    }

    /**
     * protected method for extracting k shapelets.
     *
     * @param data the data that the shapelets will be taken from
     * @return an ArrayList of FullShapeletTransform objects in order of their
     * fitness (by infoGain, seperationGap then shortest length)
     */
    public ArrayList<UShapelet> findBestKShapeletsCache(Instances data, Instances dataErr) {
        ArrayList<UShapelet> seriesShapelets;                                    // temp store of all shapelets for each time series

        //for all time series
        outputPrint("Processing data: ");

        int dataSize = data.numInstances();
        
        //for all possible time series.
        for(; casesSoFar < dataSize; casesSoFar++) {
            outputPrint("data : " + casesSoFar);

            //set the worst Shapelet so far, as long as the shapelet set is full.
            worstShapelet = kShapelets.size() == numShapelets ? kShapelets.get(numShapelets - 1) : null;

            //set the series we're working with.
            subseqDistance.setSeries(casesSoFar);
            //set the clas value of the series we're working with.
            classValue.setShapeletValue(data.get(casesSoFar));
           
            seriesShapelets = searchFunction.SearchForShapeletsInSeries(data.get(casesSoFar), dataErr.get(casesSoFar), this::checkCandidate);

            if(seriesShapelets != null){
                Collections.sort(seriesShapelets, shapeletComparator);
                
                if(isRemoveSelfSimilar())
                    seriesShapelets = removeSelfSimilar(seriesShapelets);
                kShapelets = combine(numShapelets, kShapelets, seriesShapelets);
            }
            
            createSerialFile();
        }

        this.numShapelets = kShapelets.size();

        if (recordShapelets) 
            recordShapelets(kShapelets, this.ouputFileLocation);
        if (!supressOutput)
            writeShapelets(kShapelets, new OutputStreamWriter(System.out));

        return kShapelets;
    }
    
    public void createSerialFile()
    {
        if(serialName == null) return;
        
        //Serialise the object.
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(serialName));
            out.writeObject(this);
        } catch (IOException ex) {
            System.out.println("Failed to write " + ex);
        }
        finally{
            if(out != null){
                try {
                    out.close();
                } catch (IOException ex) {
                    System.out.println("Failed to close " + ex);
                }
            }
        }
    }

    /**
     * protected method for extracting k shapelets.
     *
     * @param numShapelets
     * @param data the data that the shapelets will be taken from
     * @param minShapeletLength
     * @param maxShapeletLength
     * @return an ArrayList of FullShapeletTransform objects in order of their
     * fitness (by infoGain, seperationGap then shortest length)
     */
    public ArrayList<UShapelet> findBestKShapeletsCache(int numShapelets, Instances data, Instances dataErr, int minShapeletLength, int maxShapeletLength) {
        this.numShapelets = numShapelets;
        //setup classsValue
        classValue.init(data);
        //setup subseqDistance
        subseqDistance.init(data);
        initDataSource(data);
        return findBestKShapeletsCache(data, dataErr);
    }



    /**
     * Private method to combine two ArrayList collections of
     * FullShapeletTransform objects.
     *
     * @param k the maximum number of shapelets to be returned after combining
     * the two lists
     * @param kBestSoFar the (up to) k best shapelets that have been observed so
     * far, passed in to combine with shapelets from a new series (sorted)
     * @param timeSeriesShapelets the shapelets taken from a new series that are
     * to be merged in descending order of fitness with the kBestSoFar
     * @return an ordered ArrayList of the best k (or less) (sorted)
     * FullShapeletTransform objects from the union of the input ArrayLists
     */
    protected ArrayList<UShapelet> combine(int k, ArrayList<UShapelet> kBestSoFar, ArrayList<UShapelet> timeSeriesShapelets) {
        //both kBestSofar and timeSeries are sorted so we can explot this.
        //maintain a pointer for each list.
        ArrayList<UShapelet> newBestSoFar = new ArrayList<>();

        //best so far pointer
        int bsfPtr = 0;
        //new time seris pointer.
        int tssPtr = 0;


        for (int i = 0; i < k; i++) {
            UShapelet shapelet1 = null, shapelet2 = null;

            if (bsfPtr < kBestSoFar.size()) {
                shapelet1 = kBestSoFar.get(bsfPtr);
            }
            if (tssPtr < timeSeriesShapelets.size()) {
                shapelet2 = timeSeriesShapelets.get(tssPtr);
            }

            boolean shapelet1Null = shapelet1 == null;
            boolean shapelet2Null = shapelet2 == null;

            //both lists have been explored, but we have less than K elements.
            if (shapelet1Null && shapelet2Null) {
                break;
            }

            //one list is expired keep adding the other list until we reach K.
            if (shapelet1Null) {
                newBestSoFar.add(shapelet2);
                tssPtr++;
                continue;
            }

            //one list is expired keep adding the other list until we reach K.
            if (shapelet2Null) {
                newBestSoFar.add(shapelet1);
                bsfPtr++;
                continue;
            }

            //if both lists are fine then we need to compare which one to use.
            if (shapeletComparator.compare(shapelet1, shapelet2) == -1) {
                newBestSoFar.add(shapelet1);
                bsfPtr++;
                shapelet1 = null;
            } else {
                newBestSoFar.add(shapelet2);
                tssPtr++;
                shapelet2 = null;
            }
            
            
        }

        return newBestSoFar;
    }

    /**
     * protected method to remove self-similar shapelets from an ArrayList (i.e.
     * if they come from the same series and have overlapping indicies)
     *
     * @param shapelets the input Shapelets to remove self similar
     * FullShapeletTransform objects from
     * @return a copy of the input ArrayList with self-similar shapelets removed
     */
    protected static ArrayList<UShapelet> removeSelfSimilar(ArrayList<UShapelet> shapelets) {
        // return a new pruned array list - more efficient than removing
        // self-similar entries on the fly and constantly reindexing
        ArrayList<UShapelet> outputShapelets = new ArrayList<>();
        int size = shapelets.size();
        boolean[] selfSimilar = new boolean[size];

        for (int i = 0; i < size; i++) {
            if (selfSimilar[i]) {
                continue;
            }

            outputShapelets.add(shapelets.get(i));

            for (int j = i + 1; j < size; j++) {
                // no point recalc'ing if already self similar to something
                if ((!selfSimilar[j]) && selfSimilarity(shapelets.get(i), shapelets.get(j))) {
                    selfSimilar[j] = true;
                }
            }
        }
        return outputShapelets;
    }

    protected UShapelet checkCandidate(Instance series, Instance seriesErr, int start, int length, int dimension) {
        //init qualityBound.        
        initQualityBound(classValue.getClassDistributions());        
        
        //Set bound of the bounding algorithm
        if (worstShapelet != null) {
            quality.setBsfQuality(worstShapelet.qualityValue);
        }
        
        //set the candidate. This is the instance, start and length.
        subseqDistance.setCandidate(series, seriesErr, start, length, dimension);

        // create orderline by looping through data set and calculating the subsequence
        // distance from candidate to all data, inserting in order.
        ArrayList<UOrderLineObj> orderline = new ArrayList<>();

        int dataSize = inputData.numInstances();

        for (int i = 0; i < dataSize; i++) {
            
            //Check if it is possible to prune the candidate
            if (quality.pruneCandidate()) {
                return null;
            }

            UDistance udistance = new UDistance();
            //don't compare the shapelet to the the time series it came from because we know it's 0.
            if (i != casesSoFar) {
                udistance = new UDistance(subseqDistance.calculate(inputData.instance(i), inputDataErr.instance(i), i));
            }

            //this could be binarised or normal. 
            double classVal = classValue.getClassValue(inputData.instance(i));

            // without early abandon, it is faster to just add and sort at the end
            orderline.add(new UOrderLineObj(udistance, classVal));

            //Update qualityBound - presumably each bounding method for different quality measures will have a different update procedure.
            quality.updateOrderLine(orderline.get(orderline.size() - 1));
        }

        UShapelet shapelet = new UShapelet(subseqDistance.getCandidate(), dataSourceIDs[casesSoFar], start, quality.getQualityMeasure());
        
        //this class distribution could be binarised or normal.
        shapelet.calculateQuality(orderline, classValue.getClassDistributions());
        shapelet.classValue = classValue.getShapeletValue(); //set classValue of shapelet. (interesing to know).
        shapelet.dimension = dimension;
        return shapelet;
    }
    

    /**
     * Load a set of Instances from an ARFF
     *
     * @param fileName the file name of the ARFF
     * @return a set of Instances from the ARFF
     */
    public static Instances loadData(String fileName) {
        Instances data = null;
        try {
            FileReader r;
            r = new FileReader(fileName);
            data = new Instances(r);

            data.setClassIndex(data.numAttributes() - 1);
        } catch (IOException e) {
            System.out.println(" Error =" + e + " in method loadData");
        }
        return data;
    }

    /**
     * A private method to assess the self similarity of two
     * FullShapeletTransform objects (i.e. whether they have overlapping
     * indicies and are taken from the same time series).
     *
     * @param shapelet the first FullShapeletTransform object (in practice, this
     * will be the dominant shapelet with quality >= candidate)
     * @param candidate the second FullShapeletTransform
     * @return
     */
    private static boolean selfSimilarity(UShapelet shapelet, UShapelet candidate) {
        //check whether they're the same dimension or not.
        if (candidate.seriesId == shapelet.seriesId && candidate.dimension == shapelet.dimension) {
            if (candidate.startPos >= shapelet.startPos && candidate.startPos < shapelet.startPos + shapelet.getLength()) { //candidate starts within exisiting shapelet
                return true;
            }
            if (shapelet.startPos >= candidate.startPos && shapelet.startPos < candidate.startPos + candidate.getLength()) {
                return true;
            }
        }
        return false;
    }

    /**
     * A method to read in a FullShapeletTransform log file to reproduce a
     * FullShapeletTransform
     * <p>
     * NOTE: assumes shapelets from log are Z-NORMALISED
     *
     * @param fileName the name and path of the log file
     * @return a duplicate FullShapeletTransform to the object that created the
     * original log file
     * @throws Exception
     */
    public static UShapeletTransform createFilterFromFile(String fileName) throws Exception {
        return createFilterFromFile(fileName, Integer.MAX_VALUE);
    }

        /**
     * A method to obtain time taken to find a single best shapelet in the data
     * set
     *
     * @param data the data set to be processed
     * @param minShapeletLength minimum shapelet length
     * @param maxShapeletLength maximum shapelet length
     * @return time in seconds to find the best shapelet
     */
    public double timingForSingleShapelet(Instances data, Instances dataErr, int minShapeletLength, int maxShapeletLength) {
        data = roundRobinData(data, null);
        long startTime = System.nanoTime();
        findBestKShapeletsCache(1, data, dataErr, minShapeletLength, maxShapeletLength);
        long finishTime = System.nanoTime();
        return (double) (finishTime - startTime) / 1000000000.0;
    }

    protected void recordShapelets(ArrayList<UShapelet> kShapelets, String saveLocation) {
        //just in case the file doesn't exist or the directories.
        File file = new File(saveLocation);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        try (FileWriter out = new FileWriter(file)) {
            writeShapelets(kShapelets, out);
        } catch (IOException ex) {
            Logger.getLogger(ShapeletTransform.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    protected void writeShapelets(ArrayList<UShapelet> kShapelets, OutputStreamWriter out){
        try {
            out.append("informationGain,seriesId,startPos,classVal,numChannels,dimension\n");
            for (UShapelet kShapelet : kShapelets) {
                out.append(kShapelet.qualityValue + "," + kShapelet.seriesId + "," + kShapelet.startPos + "," + kShapelet.classValue + "," + kShapelet.getNumDimensions() + "," + kShapelet.dimension+"\n");
                for (int i = 0; i < kShapelet.numDimensions; i++) {
                    double[] shapeletContent = kShapelet.getContent().getShapeletContent(i);
                    double[] shapeletContentErr = kShapelet.getContent().getShapeletContentErr(i);
                    for (int j = 0; j < shapeletContent.length; j++) {
                        out.append(shapeletContent[j] + ",");
                    }
                    for (int j = 0; j < shapeletContentErr.length; j++) {
                        out.append(shapeletContentErr[j] + ",");
                    }
                    out.append("\n");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ShapeletTransform.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Returns a list of the lengths of the shapelets found by this transform.
     *
     * @return An ArrayList of Integers representing the lengths of the
     * shapelets.
     */
    public ArrayList<Integer> getShapeletLengths() {
        ArrayList<Integer> shapeletLengths = new ArrayList<>();

        if (m_FirstBatchDone) {
            for (UShapelet s : this.shapelets) {
                shapeletLengths.add(s.getLength());
            }
        }

        return shapeletLengths;
    }

    /**
     * A method to read in a FullShapeletTransform log file to reproduce a
     * FullShapeletTransform,
     * <p>
     * NOTE: assumes shapelets from log are Z-NORMALISED
     *
     * @param fileName the name and path of the log file
     * @param maxShapelets
     * @return a duplicate FullShapeletTransform to the object that created the
     * original log file
     * @throws Exception
     */
    public static UShapeletTransform createFilterFromFile(String fileName, int maxShapelets) throws Exception {

        File input = new File(fileName);
        Scanner scan = new Scanner(input);
        scan.useDelimiter("\n");

        UShapeletTransform sf = new UShapeletTransform();
        ArrayList<UShapelet> shapelets = new ArrayList<>();

        String shapeletContentString;
        String shapeletStatsString;
        ArrayList<Double> content;
        ArrayList<Double> contentErr;
        double[] contentArray;
        double[] contentErrArray;
        Scanner lineScan;
        Scanner statScan;
        double qualVal;
        int serID;
        int starPos;

        int shapeletCount = 0;

        while (shapeletCount < maxShapelets && scan.hasNext()) {
            shapeletStatsString = scan.next();
            shapeletContentString = scan.next();

            //Get the shapelet stats
            statScan = new Scanner(shapeletStatsString);
            statScan.useDelimiter(",");

            qualVal = Double.parseDouble(statScan.next().trim());
            serID = Integer.parseInt(statScan.next().trim());
            starPos = Integer.parseInt(statScan.next().trim());
            //End of shapelet stats

            lineScan = new Scanner(shapeletContentString);
            lineScan.useDelimiter(",");

            content = new ArrayList<>();
            contentErr = new ArrayList<>();
            while (lineScan.hasNext()) {
                String next = lineScan.next().trim();
                if (!next.isEmpty()) {
                    content.add(Double.parseDouble(next));
                    contentErr.add(Double.parseDouble(next));
                }
            }

            contentArray = new double[content.size()];
            contentErrArray = new double[contentErr.size()];
            for (int i = 0; i < content.size(); i++) {
                contentArray[i] = content.get(i);
                contentErrArray[i] = contentErr.get(i);
            }

            contentArray = sf.subseqDistance.zNormalise(contentArray, false);
            
            UShapeletCandidate cand = new UShapeletCandidate();
            cand.setShapeletContent(contentArray, contentErrArray);

            UShapelet s = new UShapelet(cand, qualVal, serID, starPos);

            shapelets.add(s);
            shapeletCount++;
        }
        sf.shapelets = shapelets;
        sf.m_FirstBatchDone = true;
        sf.numShapelets = shapelets.size();
        sf.setShapeletMinAndMax(1, 1);

        return sf;
    }

    
    
     /**
     * A method to read in a shapelet csv file and return a shapelet arraylist.

     * @param f
     * @return a duplicate FullShapeletTransform to the object that created the
     * original log file
     * @throws java.io.FileNotFoundException
     */
    public static ArrayList<Shapelet> readShapeletCSV(File f) throws FileNotFoundException{
        ArrayList<Shapelet> shapelets = new ArrayList<>();
        
        Scanner sc = new Scanner(f);
        System.out.println(sc.nextLine());
        
        boolean readHeader = true;
        
        double quality = 0, classVal = 0;
        int series = 0, position = 0, dimension = 0, numDimensions = 1;
        ShapeletCandidate cand = null;
        int currentDim = 0;

        
        while(sc.hasNextLine()){
            String line = sc.nextLine();
            String[] cotentsAsString = line.split(",");

            if(readHeader){
                quality = Double.parseDouble(cotentsAsString[0]);
                series = Integer.parseInt(cotentsAsString[1]);
                position = Integer.parseInt(cotentsAsString[2]);
                classVal = Double.parseDouble(cotentsAsString[3]);
                numDimensions = Integer.parseInt(cotentsAsString[4]);
                dimension = Integer.parseInt(cotentsAsString[5]);
                cand = new ShapeletCandidate(numDimensions);
                currentDim =0;
                readHeader = false;
            }
            else{
                //read dims until we run out.
                double[] content = new double[cotentsAsString.length];
                for (int i = 0; i < content.length; i++) {
                    content[i] = Double.parseDouble(cotentsAsString[i]);
                }
                //set the content for the current channel.
                cand.setShapeletContent(currentDim, content);
                currentDim++;
                
                //if we've evald all the current dim data for a shapelet we can add it to the list, and move on with the next one.
                if(currentDim == numDimensions){
                    Shapelet shapelet = new Shapelet(cand, quality, series, position);
                    shapelet.dimension = dimension;
                    shapelet.classValue = classVal;
                    shapelets.add(shapelet);
                    readHeader = true;
                }
            }
        }
        return shapelets;
        
    }
    
    /**
     * Method to reorder the given Instances in round robin order
     *
     * @param data Instances to be reordered
     * @param sourcePos Pointer to array of ints, where old positions of
     * instances are to be stored.
     * @return Instances in round robin order
     */
    public static Instances roundRobinData(Instances data, int[] sourcePos) {
        //Count number of classes 
        TreeMap<Double, ArrayList<Instance>> instancesByClass = new TreeMap<>();
        TreeMap<Double, ArrayList<Integer>> positionsByClass = new TreeMap<>();

        NormalClassValue ncv = new NormalClassValue();
        ncv.init(data);

        //Get class distributions 
        ClassCounts classDistribution = ncv.getClassDistributions();

        //Allocate arrays for instances of every class
        for (int i = 0; i < classDistribution.size(); i++) {
            int frequency = classDistribution.get(i);
            instancesByClass.put((double) i, new ArrayList<>(frequency));
            positionsByClass.put((double) i, new ArrayList<>(frequency));
        }

        int dataSize = data.numInstances();
        //Split data according to their class memebership
        for (int i = 0; i < dataSize; i++) {
            Instance inst = data.instance(i);
            instancesByClass.get(ncv.getClassValue(inst)).add(inst);
            positionsByClass.get(ncv.getClassValue(inst)).add(i);
        }

        //Merge data into single list in round robin order
        Instances roundRobinData = new Instances(data, dataSize);
        for (int i = 0; i < dataSize;) {
            //Allocate arrays for instances of every class
            for (int j = 0; j < classDistribution.size(); j++) {
                ArrayList<Instance> currentList = instancesByClass.get((double) j);
                ArrayList<Integer> currentPositions = positionsByClass.get((double) j);

                if (!currentList.isEmpty()) {
                    roundRobinData.add(currentList.remove(currentList.size() - 1));
                    if (sourcePos != null && sourcePos.length == dataSize) {
                        sourcePos[i] = currentPositions.remove(currentPositions.size() - 1);
                    }
                    i++;
                }
            }
        }

        return roundRobinData;
    }

    public void outputPrint(String val) {
        if (!this.supressOutput) {
            System.out.println(val);
        }
    }

    @Override
    public String toString() {
        String str = "Shapelets: ";
        for (UShapelet s : shapelets) {
            str += s.toString() + "\n";
        }
        return str;
    }
    
    @Override
    public String getParameters(){
        String str="minShapeletLength,"+searchFunction.getMin()+",maxShapeletLength,"+searchFunction.getMax()+",numShapelets,"+numShapelets+",roundrobin,"+roundRobin
                + ",searchFunction,"+this.searchFunction.getClass().getSimpleName()
                + ",qualityMeasure,"+this.quality.getQualityMeasure().getClass().getSimpleName();
        return str;
    }
    /**
     *
     * @param data
     * @param minShapeletLength
     * @param maxShapeletLength
     * @return
     * @throws Exception
     */
    public long opCountForSingleShapelet(Instances data, Instances dataErr, int minShapeletLength, int maxShapeletLength) throws Exception {
        data = roundRobinData(data, null);
        subseqDistOpCount = 0;
        findBestKShapeletsCache(1, data, dataErr, minShapeletLength, maxShapeletLength);
        return subseqDistOpCount;
    }

    
    public static void main(String[] args){
        try {
            final String resampleLocation = "/home/frankl1/Desktop/dataset/";
            final String dataset = "BirdChicken";
            final int fold = 1;
            final String filePath = resampleLocation + File.separator + dataset + File.separator + dataset;
            Instances test, train;
            test = utilities.ClassifierTools.loadData(filePath + "_TEST");
            train = utilities.ClassifierTools.loadData(filePath + "_TRAIN");
            //use fold as the seed.
            //train = InstanceTools.subSample(train, 100, fold);
            
            
            ShapeletTransform transform = new ShapeletTransform();
            transform.setRoundRobin(true);
            //construct shapelet classifiers.
            transform.setClassValue(new BinaryClassValue());
            transform.setSubSeqDistance(new ImprovedOnlineSubSeqDistance());
            transform.setShapeletMinAndMax(3, train.numAttributes() - 1);
            transform.useCandidatePruning();
            transform.setNumberOfShapelets(train.numInstances() * 10);
            transform.setQualityMeasure(ShapeletQualityChoice.INFORMATION_GAIN);
            transform.supressOutput();
            
            long startTime = System.nanoTime();
           
            Instances tranTrain = transform.process(train);
            Instances tranTest = transform.process(test);
            
            long endTime = System.nanoTime();
            
            RotationForest rot1 = new RotationForest();
            rot1.buildClassifier(tranTrain);
            double accuracy = ClassifierTools.accuracy(tranTest, rot1);
            
            System.out.println("Shapelet transform "+ accuracy + " time " + (endTime-startTime));
            

            ShapeletSearchOptions searchOptions = new ShapeletSearchOptions.Builder()
                                                .setMin(3)
                                                .setMax(train.numAttributes()-1)
                                                .setSearchType(ShapeletSearch.SearchType.FULL)
                                                .build();
                                                
            
            ShapeletTransformFactoryOptions options = new ShapeletTransformFactoryOptions.Builder()
                                                .setDistanceType(SubSeqDistance.DistanceType.IMP_ONLINE)
                                                .setKShapelets(train.numInstances()*10)
                                                .useBinaryClassValue()
                                                .useClassBalancing()
                                                .useCandidatePruning()
                                                .useRoundRobin()
                                                .setSearchOptions(searchOptions)
                                                .build();
            
            ShapeletTransform transform1 = new ShapeletTransformFactory(options).getTransform();
            transform1.supressOutput();
            
            long startTime1 = System.nanoTime();
            
            Instances tranTrain1 = transform.process(train);
            Instances tranTest1 = transform.process(test);
            
            long endTime1 = System.nanoTime();
            
            
            RotationForest rot2 = new RotationForest();
            rot2.buildClassifier(tranTrain1);
            double accuracy1 = ClassifierTools.accuracy(tranTest1, rot2);
            
            System.out.println("Fast shapelet transform "+ accuracy1 + " time " + (endTime1-startTime1));
        } catch (Exception ex) {
            Logger.getLogger(ShapeletTransform.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the removeSelfSimilar
     */
    public boolean isRemoveSelfSimilar() {
        return removeSelfSimilar;
    }

    /**
     * @param removeSelfSimilar the removeSelfSimilar to set
     */
        public void setRemoveSelfSimilar(boolean removeSelfSimilar) {
        this.removeSelfSimilar = removeSelfSimilar;
    }
    
        /**
         * 
         * @param lenghtIncrement the increment to set
         */
	public void setLengthIncrement(int lenghtIncrement) {
		searchFunction.setLengthIncrement(lenghtIncrement);
	}

		@Override
		protected Instances process(Instances instances) throws Exception {
			// TODO Auto-generated method stub
			return null;
		}
}
