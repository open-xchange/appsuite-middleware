/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.requesthandler.jobqueue.impl;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.jobqueue.EnqueuedException;
import com.openexchange.ajax.requesthandler.jobqueue.Job;
import com.openexchange.ajax.requesthandler.jobqueue.JobInfo;
import com.openexchange.ajax.requesthandler.jobqueue.JobKey;
import com.openexchange.ajax.requesthandler.jobqueue.JobQueueExceptionCodes;
import com.openexchange.ajax.requesthandler.jobqueue.JobQueueService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.UserAndContext;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.TrackableTask;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;
import com.openexchange.tools.servlet.AjaxExceptionCodes;


/**
 * {@link ThreadPoolJobQueueService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class ThreadPoolJobQueueService implements JobQueueService {

    /** The logger constant */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ThreadPoolJobQueueService.class);

    private static JobTask jobTaskFor(Job job) {
        return jobTaskFor(job, null, null);
    }

    private static JobTask jobTaskFor(Job job, JobKey optionalKey, Cache<String, UUID> jobsByKey) {
        return job.isTrackable() ? new TrackableJobTask(job, optionalKey, jobsByKey) : new JobTask(job, optionalKey, jobsByKey);
    }

    private static class JobTask extends AbstractTask<AJAXRequestResult> {

        private final Job job;
        private boolean executed;
        private JobKey optionalKey;
        private Cache<String, UUID> jobsByKey;

        JobTask(Job job, JobKey optionalKey, Cache<String, UUID> jobsByKey) {
            super();
            this.job = job;
            executed = false;
            apply(optionalKey, jobsByKey);
        }

        @Override
        public AJAXRequestResult call() throws OXException {
            return job.perform();
        }

        @Override
        public void afterExecute(Throwable throwable) {
            if (throwable == null) {
                LOG.debug("Job for action \"{}\" of module \"{}\" successfully executed for user {} in context {}.", job.getRequestData().getAction(), job.getRequestData().getModule(), I(job.getSession().getUserId()), I(job.getSession().getContextId()));
            } else {
                LOG.debug("Job for action \"{}\" of module \"{}\" failed for user {} in context {}.", job.getRequestData().getAction(), job.getRequestData().getModule(), I(job.getSession().getUserId()), I(job.getSession().getContextId()), throwable);
            }
            synchronized (job) {
                if (null != optionalKey) {
                    jobsByKey.invalidate(optionalKey.getIdentifier());
                }
                executed = true;
            }
            super.afterExecute(throwable);
        }

        public boolean apply(JobKey optionalKey, Cache<String, UUID> jobsByKey) {
            synchronized (job) {
                if (false == executed) {
                    this.optionalKey = optionalKey;
                    this.jobsByKey = jobsByKey;
                    return true;
                }

                return false;
            }
        }

    } // End of class JobTask

    private static final class TrackableJobTask extends JobTask implements TrackableTask<AJAXRequestResult> {

        TrackableJobTask(Job job, JobKey optionalKey, Cache<String, UUID> jobsByKey) {
            super(job, optionalKey, jobsByKey);
        }
    }

    // ---------------------------------------------------------------------------------------------------------------

    private final LoadingCache<UserAndContext, Cache<UUID, FutureJobInfo>> allJobs;
    private final LoadingCache<UserAndContext, Cache<String, UUID>> allKeys;
    private final int maxRequestAgeMillis;

    /**
     * Initializes a new {@link ThreadPoolJobQueueService}.
     */
    public ThreadPoolJobQueueService(ConfigurationService configService) {
        super();
        int defaultMaxRequestAgeMillis = 10000;
        maxRequestAgeMillis = configService.getIntProperty("com.openexchange.jobqueue.waitMillis", defaultMaxRequestAgeMillis);

        RemovalListener<UUID, FutureJobInfo> listener = new RemovalListener<UUID, FutureJobInfo>() {

            @Override
            public void onRemoval(RemovalNotification<UUID, FutureJobInfo> notification) {
                notification.getValue().cancel(true);
            }
        };

        CacheLoader<UserAndContext, Cache<UUID, FutureJobInfo>> loader = new CacheLoader<UserAndContext, Cache<UUID,FutureJobInfo>>() {

            @Override
            public Cache<UUID, FutureJobInfo> load(UserAndContext key) {
                return CacheBuilder.newBuilder().initialCapacity(128).expireAfterAccess(30, TimeUnit.MINUTES).removalListener(listener).build();
            }
        };

        allJobs = CacheBuilder.newBuilder().initialCapacity(65536).expireAfterAccess(2, TimeUnit.HOURS).<UserAndContext, Cache<UUID, FutureJobInfo>> build(loader);

        CacheLoader<UserAndContext, Cache<String, UUID>> keysLoader = new CacheLoader<UserAndContext, Cache<String, UUID>>() {

            @Override
            public Cache<String, UUID> load(UserAndContext key) {
                return CacheBuilder.newBuilder().initialCapacity(128).expireAfterAccess(30, TimeUnit.MINUTES).build();
            }
        };

        allKeys = CacheBuilder.newBuilder().initialCapacity(65536).expireAfterAccess(2, TimeUnit.HOURS).<UserAndContext, Cache<String, UUID>> build(keysLoader);
    }

    @Override
    public long getMaxRequestAgeMillis() throws OXException {
        return maxRequestAgeMillis;
    }

    @Override
    public UUID contains(JobKey key) throws OXException {
        if (null == key) {
            return null;
        }

        UserAndContext userAndContext = UserAndContext.newInstance(key.getUserId(), key.getContextId());
        Cache<String, UUID> jobsByKey = allKeys.getIfPresent(userAndContext);
        return null == jobsByKey ? null : jobsByKey.getIfPresent(key.getIdentifier());
    }

    @Override
    public JobInfo enqueue(Job job) throws OXException {
        ThreadPoolService threadPool = ServerServiceRegistry.getServize(ThreadPoolService.class);
        if (null == threadPool) {
            throw ServiceExceptionCode.absentService(ThreadPoolService.class);
        }

        // Generate unique ID for the job
        UUID id = UUID.randomUUID();
        UserAndContext userAndContext = UserAndContext.newInstance(job.getSession());
        Cache<UUID, FutureJobInfo> jobsById = allJobs.getUnchecked(userAndContext);

        // Check for optional job key
        JobKey key = job.getOptionalKey();
        if (null == key) {
            // No key given
            Future<AJAXRequestResult> f = threadPool.submit(jobTaskFor(job), CallerRunsBehavior.getInstance());
            FutureJobInfo jobInfo = new FutureJobInfo(id, job, f, jobsById);

            jobsById.put(id, jobInfo);
            return jobInfo;
        }

        Cache<String, UUID> jobsByKey = allKeys.getUnchecked(userAndContext);
        jobsByKey.put(key.getIdentifier(), id);

        Future<AJAXRequestResult> f = threadPool.submit(jobTaskFor(job, key, jobsByKey), CallerRunsBehavior.getInstance());
        FutureJobInfo jobInfo = new FutureJobInfo(id, job, f, jobsById);

        jobsById.put(id, jobInfo);
        return jobInfo;
    }

    @Override
    public JobInfo enqueueAndWait(Job job, long timeout, TimeUnit unit) throws EnqueuedException, InterruptedException, OXException {
        ThreadPoolService threadPool = ServerServiceRegistry.getServize(ThreadPoolService.class);
        if (null == threadPool) {
            throw ServiceExceptionCode.absentService(ThreadPoolService.class);
        }

        // Sumbit for execution
        JobTask jobTask = jobTaskFor(job);
        Future<AJAXRequestResult> f = threadPool.submit(jobTask, CallerRunsBehavior.getInstance());
        try {
            AJAXRequestResult result = f.get(timeout, unit);
            return new ExecutedJobInfo(result, job);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof OXException) {
                throw (OXException) cause;
            }
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(cause, cause.getMessage());
        } catch (TimeoutException e) {
            // Not computed in time; enqueue job info
            LOG.debug("Action \"{}\" of module \"{}\" could not be executed in time for user {} in context {}.", job.getRequestData().getAction(), job.getRequestData().getModule(), I(job.getSession().getUserId()), I(job.getSession().getContextId()), e);

            // Generate unique ID for the job
            UUID id = UUID.randomUUID();
            UserAndContext userAndContext = UserAndContext.newInstance(job.getSession());
            Cache<UUID, FutureJobInfo> jobs = allJobs.getUnchecked(userAndContext);

            // Check for optional job key
            JobKey key = job.getOptionalKey();
            if (null == key) {
                FutureJobInfo jobInfo = new FutureJobInfo(id, job, f, jobs);
                jobs.put(id, jobInfo);
                throw new EnqueuedException(jobInfo, e);
            }

            Cache<String, UUID> jobsByKey = allKeys.getUnchecked(userAndContext);
            jobsByKey.put(key.getIdentifier(), id);
            if (false == jobTask.apply(key, jobsByKey)) {
                jobsByKey.invalidate(key.getIdentifier());
            }

            FutureJobInfo jobInfo = new FutureJobInfo(id, job, f, jobs);
            jobs.put(id, jobInfo);
            throw new EnqueuedException(jobInfo, e);
        }
    }

    @Override
    public JobInfo get(UUID id, int userId, int contextId) throws OXException {
        if (null == id) {
            return null;
        }

        Cache<UUID, FutureJobInfo> userJobs = allJobs.getIfPresent(UserAndContext.newInstance(userId, contextId));
        if (null == userJobs) {
            return null;
        }

        return userJobs.getIfPresent(id);
    }

    @Override
    public JobInfo require(UUID id, int userId, int contextId) throws OXException {
        JobInfo jobInfo = get(id, userId, contextId);
        if (null == jobInfo) {
            throw JobQueueExceptionCodes.NO_SUCH_JOB.create(UUIDs.getUnformattedString(id), I(userId), I(contextId));
        }
        return jobInfo;
    }

    @Override
    public List<JobInfo> getAllFor(int userId, int contextId) throws OXException {
        Cache<UUID, FutureJobInfo> userJobs = allJobs.getIfPresent(UserAndContext.newInstance(userId, contextId));
        if (null == userJobs) {
            return Collections.emptyList();
        }

        return new ArrayList<JobInfo>(userJobs.asMap().values());
    }

    @Override
    public JobInfo getAndRemoveIfDone(UUID id, int userId, int contextId) throws OXException {
        if (null == id) {
            return null;
        }

        Cache<UUID, FutureJobInfo> userJobs = allJobs.getIfPresent(UserAndContext.newInstance(userId, contextId));
        if (null == userJobs) {
            return null;
        }

        FutureJobInfo jobInfo = userJobs.getIfPresent(id);
        if (null != jobInfo) {
            synchronized (jobInfo) {
                jobInfo = userJobs.getIfPresent(id);
                if (null != jobInfo && jobInfo.isDone()) {
                    userJobs.invalidate(id);
                }
            }
        }
        return jobInfo;
    }

    @Override
    public void clear() {
        for (Cache<UUID,FutureJobInfo> userJobs : allJobs.asMap().values()) {
            userJobs.invalidateAll();
        }
        allJobs.invalidateAll();
    }

}
