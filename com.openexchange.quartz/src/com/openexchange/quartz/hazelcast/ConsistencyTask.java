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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.quartz.hazelcast;

import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentMap;
import org.quartz.Calendar;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.quartz.spi.OperableTrigger;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ISet;
import com.openexchange.quartz.hazelcast.predicates.AcquiredAndExecutingTriggersPredicate;

/**
 *
 * {@link ConsistencyTask}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public final class ConsistencyTask extends TimerTask {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConsistencyTask.class);

    private final ConcurrentMap<TriggerKey, Boolean> locallyAcquiredTriggers;

    private final ConcurrentMap<TriggerKey, Boolean> locallyExecutingTriggers;

    private final ImprovedHazelcastJobStore jobStore;

    public ConsistencyTask(final ImprovedHazelcastJobStore jobStore, final ConcurrentMap<TriggerKey, Boolean> locallyAcquiredTriggers, final ConcurrentMap<TriggerKey, Boolean> locallyExecutingTriggers) {
        super();
        this.jobStore = jobStore;
        this.locallyAcquiredTriggers = locallyAcquiredTriggers;
        this.locallyExecutingTriggers = locallyExecutingTriggers;
    }

    @Override
    public void run() {
        int restored = 0;
        try {
            LOG.debug("Started consistency task run.");

            final String nodeIp = jobStore.getNodeIp();
            ILock lock = jobStore.getClusterLock();
            IMap<TriggerKey, TriggerStateWrapper> triggersByKey = jobStore.getTriggerMap();
            Set<TriggerKey> clusterKeys = null;
            lock.lock();
            try {
                Set<TriggerKey> hazelcastKeys = triggersByKey.keySet(new AcquiredAndExecutingTriggersPredicate(nodeIp));
                clusterKeys = new HashSet<TriggerKey>(hazelcastKeys);
                clusterKeys.removeAll(locallyAcquiredTriggers.keySet());
                clusterKeys.removeAll(locallyExecutingTriggers.keySet());
            } finally {
                lock.unlock();
            }

            if (clusterKeys == null || clusterKeys.isEmpty()) {
                return;
            }

            for (TriggerKey key : clusterKeys) {
                TriggerStateWrapper stateWrapper = triggersByKey.get(key);
                Calendar calendar = null;
                String calendarName = stateWrapper.getTrigger().getCalendarName();
                if (calendarName != null) {
                    calendar = jobStore.retrieveCalendar(calendarName);
                }

                jobStore.getSignaler().notifyTriggerListenersMisfired(stateWrapper.getTrigger());
                ((OperableTrigger) stateWrapper.getTrigger()).updateAfterMisfire(calendar);

                if (stateWrapper.getTrigger().getNextFireTime() == null) {
                    stateWrapper.setState(TriggerStateWrapper.STATE_COMPLETE);
                    stateWrapper.resetOwner();
                    triggersByKey.replace(stateWrapper.getTrigger().getKey(), stateWrapper);
                    jobStore.getSignaler().notifySchedulerListenersFinalized(stateWrapper.getTrigger());
                } else {
                    stateWrapper.setState(TriggerStateWrapper.STATE_WAITING);
                    stateWrapper.resetOwner();
                    triggersByKey.replace(stateWrapper.getTrigger().getKey(), stateWrapper);
                }

                ISet<JobKey> blockedJobs = jobStore.getBlockedJobs();
                blockedJobs.remove(stateWrapper.getTrigger().getJobKey());

                ++restored;
            }
        } catch (Throwable t) {
            LOG.warn("Error during consistency task run.", t);
        }
    }
}