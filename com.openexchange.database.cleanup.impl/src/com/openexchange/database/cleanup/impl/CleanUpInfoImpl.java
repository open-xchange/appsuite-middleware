package com.openexchange.database.cleanup.impl;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import com.openexchange.database.cleanup.CleanUpInfo;
import com.openexchange.database.cleanup.CleanUpJobId;
import com.openexchange.timer.ScheduledTimerTask;

/**
 * {@link CleanUpInfoImpl} - The clean-up info implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public class CleanUpInfoImpl implements CleanUpInfo {

    private final ScheduledTimerTask timerTask;
    private final CleanUpJobId jobId;
    private final ConcurrentMap<CleanUpJobId, Future<ScheduledTimerTask>> submittedJobs;

    /**
     * Initializes a new {@link CleanUpInfoImpl}.
     *
     * @param jobId The cleanup job's identifier
     * @param timerTask The wrapped timer task
     * @param submittedJobs The in-memory registry for submitted jobs
     */
    CleanUpInfoImpl(CleanUpJobId jobId, ScheduledTimerTask timerTask, ConcurrentMap<CleanUpJobId, Future<ScheduledTimerTask>> submittedJobs) {
        super();
        this.jobId = jobId;
        this.timerTask = timerTask;
        this.submittedJobs = submittedJobs;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean canceled = timerTask.cancel(mayInterruptIfRunning);
        submittedJobs.remove(jobId);
        return canceled;
    }

    @Override
    public CleanUpJobId getJobId() {
        return jobId;
    }

}