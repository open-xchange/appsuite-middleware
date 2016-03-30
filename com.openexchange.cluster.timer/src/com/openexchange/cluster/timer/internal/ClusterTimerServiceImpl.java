/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.cluster.timer.internal;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IAtomicLong;
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
        return services.getService(TimerService.class).scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                runIfDue(id, task, delay, true);
            }
        }, getEffectiveInitialDelay(id, initialDelay, delay), delay);
    }

    @Override
    public ScheduledTimerTask scheduleAtFixedRate(final String id, final Runnable task, long initialDelay, final long period) {
        return services.getService(TimerService.class).scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                runIfDue(id, task, period, false);
            }
        }, getEffectiveInitialDelay(id, initialDelay, period), period);
    }

    private long getEffectiveInitialDelay(String id, long initialDelay, long interval) {
        HazelcastInstance hazelcastInstance = services.getOptionalService(HazelcastInstance.class);
        if (null == hazelcastInstance) {
            LOG.warn("No {} available, unable to determine effective initial delay for task {} on this node.",
                HazelcastInstance.class.getName(), id);
            return initialDelay;
        }
        IAtomicLong clusterExecutionTime = hazelcastInstance.getAtomicLong(id);
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
            IAtomicLong clusterExecutionTime = hazelcastInstance.getAtomicLong(id);
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
