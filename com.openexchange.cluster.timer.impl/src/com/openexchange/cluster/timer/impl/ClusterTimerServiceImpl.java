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

package com.openexchange.cluster.timer.impl;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.cp.IAtomicLong;
import com.openexchange.cluster.timer.ClusterTimerService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link ClusterTimerServiceImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ClusterTimerServiceImpl implements ClusterTimerService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ClusterTimerServiceImpl.class);

    private final Unregisterer unregisterer;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link ClusterTimerServiceImpl}.
     *
     * @param services A service lookup reference
     * @param unregisterer The unregisterer callback
     */
    public ClusterTimerServiceImpl(ServiceLookup services, Unregisterer unregisterer) {
        super();
        this.services = services;
        this.unregisterer = unregisterer;
    }

    @Override
    public ScheduledTimerTask scheduleAtFixedRate(String id, Runnable task, long initialDelay, long period, TimeUnit unit) {
        return scheduleAtFixedRate(id, task, unit.toMillis(initialDelay), unit.toMillis(period));
    }

    @Override
    public ScheduledTimerTask scheduleWithFixedDelay(String id, Runnable task, long initialDelay, long delay, TimeUnit unit) {
        return scheduleWithFixedDelay(id, task, unit.toMillis(initialDelay), unit.toMillis(delay));
    }

    @Override
    public ScheduledTimerTask scheduleWithFixedDelay(final String id, final Runnable task, final long initialDelay, final long delay) {
        return services.getService(TimerService.class).scheduleWithFixedDelay(() -> runIfDue(id, task, delay, true), getEffectiveInitialDelay(id, initialDelay, delay), delay);
    }

    @Override
    public ScheduledTimerTask scheduleAtFixedRate(final String id, final Runnable task, long initialDelay, final long period) {
        return services.getService(TimerService.class).scheduleAtFixedRate(() -> runIfDue(id, task, period, false), getEffectiveInitialDelay(id, initialDelay, period), period);
    }

    private long getEffectiveInitialDelay(String id, long initialDelay, long interval) {
        HazelcastInstance hazelcastInstance = services.getOptionalService(HazelcastInstance.class);
        if (null == hazelcastInstance) {
            LOG.debug("No {} available yet, using {}ms as initial delay for task {} on this node.",
                HazelcastInstance.class.getName(), Long.valueOf(initialDelay), id);
            return initialDelay;
        }
        IAtomicLong clusterExecutionTime = hazelcastInstance.getCPSubsystem().getAtomicLong(id);
        long lastExecuted = clusterExecutionTime.get();
        if (0 == lastExecuted) {
            // no last execution time known
            return initialDelay;
        }
        long nextExecutionTime = lastExecuted + interval;
        long now = hazelcastInstance.getCluster().getClusterTime();
        if (nextExecutionTime <= now) {
            // task is over-due, stick to initial delay to minimize impact
            return initialDelay;
        }
        // schedule to run on next regular cluster-wide execution
        return nextExecutionTime - now;
    }

    private void runIfDue(String id, Runnable task, long interval, boolean resetAfterCompletion) {
        try {
            HazelcastInstance hazelcastInstance = services.getOptionalService(HazelcastInstance.class);
            if (null == hazelcastInstance) {
                LOG.warn("No {} available, skipping execution of task {} on this node.", HazelcastInstance.class.getName(), id);
                return;
            }
            IAtomicLong clusterExecutionTime = hazelcastInstance.getCPSubsystem().getAtomicLong(id);
            long lastExecuted = clusterExecutionTime.get();
            long now = hazelcastInstance.getCluster().getClusterTime();
            if (lastExecuted + interval > now) {
                LOG.debug("Task {} already executed at {}, skipping execution on this node.", id, new Date(lastExecuted));
                return;
            }
            if (false == clusterExecutionTime.compareAndSet(lastExecuted, now)) {
                LOG.debug("Execution of task {} already started somewhere else, skipping execution on this node.", id);
                return;
            }
            LOG.debug("Executing task {} on this node.", id);
            try {
                task.run();
            } finally {
                if (resetAfterCompletion) {
                    clusterExecutionTime.set(hazelcastInstance.getCluster().getClusterTime());
                }
            }
        } catch (HazelcastInstanceNotActiveException e) {
            LOG.warn("Encountered a {} error. {} will be shut-down!",
                HazelcastInstanceNotActiveException.class.getSimpleName(), ClusterTimerServiceImpl.class, e);
            unregisterer.propagateNotActive(e);
            unregisterer.unregister();
        }
    }
}
