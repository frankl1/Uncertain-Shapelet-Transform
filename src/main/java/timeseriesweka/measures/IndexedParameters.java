package timeseriesweka.measures;

public interface IndexedParameters {
    void setParameter(int index, double value);
    int getNumParameters();
    static void setParameters(IndexedParameters indexedParameters,
                              double parameterIndex,
                              double[]... parameterRanges) {
        if(parameterRanges.length != indexedParameters.getNumParameters()) {
            throw new IllegalArgumentException("Num params mismatch");
        }
        int num = 1;
        for(double[] range : parameterRanges) {
            num *= range.length;
        }
        int numParameterCombinations = num;
        double parameterCombinationIncrement = (double) 1 / numParameterCombinations;
        int combinationIndex = (int) Math.round(parameterIndex / parameterCombinationIncrement);
        for(int i = 0; i < indexedParameters.getNumParameters(); i++) {
            int index = combinationIndex % parameterRanges[i].length;
            indexedParameters.setParameter(i, parameterRanges[i][index]);
            combinationIndex /= parameterRanges[i].length;
        }
    }
}
