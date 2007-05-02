package com.openexchange.admin.taskmanagement;

import java.util.concurrent.Callable;

/**
 * An interface which extends the standard Callable interface by an ability
 * to get the progress of the job
 * 
 * @author d7
 *
 */
public interface ProgressCallable<V> extends Callable {

    public int getProgressPercentage();
}
