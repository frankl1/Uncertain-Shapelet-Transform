package timeseriesweka.classifiers;

/**
 * The new version of ParameterSplittable. An interface defining behaviour of something that can be split over multiple concurrent processes, whether that's different threads, processes, a cluster, etc.
 *
 * Behaviour is: some task (y) can be broken into x independent tasks. Upon completion of these x subtasks, parent task y can be completed successfully by 'post-processing' the output of the x subtasks.
 *
 * These x subtasks may be run distributed of non-distributed, hence the flag to run parent task y in distributed or non-distributed mode.
 */
public interface Distributable {
    /**
     * set to run in distributed mode
     */
    default void setRunDistributed() {
        setRunDistributed(true);
    }

    /**
     * set to run locally (non-distributed)
     */
    default void setRunLocal() {
        setRunDistributed(false);
    }

    /**
     * set distributed run
     * @param on true to turn on distributed run
     */
    void setRunDistributed(boolean on);

    /**
     * how many sub tasks
     * @return number of sub tasks
     */
    int size();

    /**
     * set the sub task to run
     * @param index index of the sub task, integer between 0 ... size - 1. Set to <0 to post process
     */
    void setSubTaskIndex(int index);

    /**
     * rebuild parent task from result of sub tasks
     */
    default void setPostProcess() {
        setSubTaskIndex(-1);
    }

    /**
     * set place to store outputs from sub tasks
     */
    void setSavePath(String path);
}
