package com.openexchange.admin.taskmanagement;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.mail.MethodNotSupportedException;

/**
 * A FutureTask extented by Progress
 * 
 * @author d7
 * 
 * @param <V>
 */
public class ExtendedFutureTask<V> extends FutureTask<V> {

    private final Callable<V> callable;
    
    private final String typeofjob;
    
    private final String furtherinformation;
    
    public ExtendedFutureTask(final Callable<V> callable, final String typeofjob, final String furtherinformation) {
        super(callable);
        this.callable = callable;
        this.typeofjob = typeofjob;
        this.furtherinformation = furtherinformation;
    }

    /**
     * Convenience method for detecting if a job runs
     * 
     * @return
     */
    public boolean isRunning() {
        return (!isCancelled() && !isDone());
    }

    /**
     * Convenience method for detecting if a job failed
     * 
     * @return
     */
    public boolean isFailed() {
        if (isDone()) {
            try {
                get();
            } catch (final InterruptedException e) {
                return true;
            } catch (final ExecutionException e) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gets the progress percentage of the underlying job
     * 
     * @return The progress in percent
     * @throws MethodNotSupportedException If the job doesn't support this feature
     */
    public int getProgressPercentage() throws MethodNotSupportedException {
        if (this.callable instanceof ProgressCallable) {
            final ProgressCallable<?> progcall = (ProgressCallable<?>) this.callable;
            return progcall.getProgressPercentage();
        } else {
            throw new MethodNotSupportedException();
        }
        
    }

    public final String getFurtherinformation() {
        return this.furtherinformation;
    }

    public final String getTypeofjob() {
        return this.typeofjob;
    }
}
