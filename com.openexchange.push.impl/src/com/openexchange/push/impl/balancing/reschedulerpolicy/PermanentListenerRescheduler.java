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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.push.impl.balancing.reschedulerpolicy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.BufferingQueue;
import com.openexchange.push.PushManagerExtendedService;
import com.openexchange.push.PushUser;
import com.openexchange.push.impl.PushManagerRegistry;
import com.openexchange.push.impl.balancing.reschedulerpolicy.portable.PortableCheckForExtendedServiceCallable;
import com.openexchange.push.impl.balancing.reschedulerpolicy.portable.PortableDropPermanentListenerCallable;
import com.openexchange.push.impl.balancing.reschedulerpolicy.portable.PortablePlanRescheduleCallable;
import com.openexchange.push.impl.osgi.Services;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;


/**
 * {@link PermanentListenerRescheduler} - Reschedules permanent listeners equally among cluster nodes, whenever cluster members do change.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
public class PermanentListenerRescheduler implements ServiceTrackerCustomizer<HazelcastInstance, HazelcastInstance>, MembershipListener {

    /** The logger constant */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PermanentListenerRescheduler.class);

    // -------------------------------------------------------------------------------------------------------------------------------

    private static volatile Long delayDuration;

    private static long delayDuration() {
        Long tmp = delayDuration;
        if (null == tmp) {
            synchronized (PermanentListenerRescheduler.class) {
                tmp = delayDuration;
                if (null == tmp) {
                    int defaultValue = 5000;
                    ConfigurationService service = Services.optService(ConfigurationService.class);
                    if (null == service) {
                        return defaultValue;
                    }
                    tmp = Long.valueOf(service.getIntProperty("com.openexchange.push.reschedule.delayDuration", defaultValue));
                    delayDuration = tmp;
                }
            }
        }
        return tmp.longValue();
    }

    private static volatile Long timerFrequency;

    private static long timerFrequency() {
        Long tmp = timerFrequency;
        if (null == tmp) {
            synchronized (PermanentListenerRescheduler.class) {
                tmp = timerFrequency;
                if (null == tmp) {
                    int defaultValue = 2000;
                    ConfigurationService service = Services.optService(ConfigurationService.class);
                    if (null == service) {
                        return defaultValue;
                    }
                    tmp = Long.valueOf(service.getIntProperty("com.openexchange.push.reschedule.timerFrequency", defaultValue));
                    timerFrequency = tmp;
                }
            }
        }
        return tmp.longValue();
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    private final PushManagerRegistry pushManagerRegistry;
    private final BundleContext context;
    private final AtomicReference<String> registrationIdRef;
    private final AtomicReference<HazelcastInstance> hzInstancerRef;
    private final BufferingQueue<ReschedulePlan> rescheduleQueue;
    private ScheduledTimerTask scheduledTimerTask; // Accessed synchronized
    private boolean stopped; // Accessed synchronized

    /**
     * Initializes a new {@link PermanentListenerRescheduler}.
     *
     * @param pushManagerRegistry The associated push manager registry
     * @param context The bundle context
     */
    public PermanentListenerRescheduler(PushManagerRegistry pushManagerRegistry, BundleContext context) {
        super();
        this.rescheduleQueue = new BufferingQueue<ReschedulePlan>(delayDuration()); // 5sec default delay;
        this.hzInstancerRef = new AtomicReference<HazelcastInstance>();
        registrationIdRef = new AtomicReference<String>();
        this.pushManagerRegistry = pushManagerRegistry;
        this.context = context;
    }

    /**
     * Stops this rescheduler.
     */
    public void stop() {
        synchronized (this) {
            rescheduleQueue.clear();
            cancelTimerTask();
            stopped = true;
        }
    }

    /**
     * Cancels the currently running timer task (if any).
     */
    protected void cancelTimerTask() {
        ScheduledTimerTask scheduledTimerTask = this.scheduledTimerTask;
        if (null != scheduledTimerTask) {
            scheduledTimerTask.cancel();
            this.scheduledTimerTask = null;
            LOG.info("Canceled timer task for rescheduling checks");
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    @Override
    public void memberAdded(MembershipEvent membershipEvent) {
        try {
            planReschedule(false);
        } catch (Exception e) {
            LOG.error("Failed to plan rescheduling", e);
        }
    }

    @Override
    public void memberRemoved(MembershipEvent membershipEvent) {
        try {
            planReschedule(false);
        } catch (Exception e) {
            LOG.error("Failed to plan rescheduling", e);
        }
    }

    @Override
    public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
        // Don't care
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    /**
     * Plan to reschedule.
     *
     * @param remotePlan <code>true</code> for remote rescheduling; otherwise <code>false</code>
     * @throws OXException If timer service is absent
     */
    public void planReschedule(boolean remotePlan) throws OXException {
        synchronized (this) {
            // Stopped
            if (stopped) {
                return;
            }

            // Check time task is alive
            ScheduledTimerTask scheduledTimerTask = this.scheduledTimerTask;
            if (null == scheduledTimerTask) {
                // Initialize timer task
                TimerService timerService = Services.requireService(TimerService.class);
                Runnable timerTask = new Runnable() {

                    @Override
                    public void run() {
                        checkReschedule();
                    }
                };
                long delay = timerFrequency(); // 2sec delay
                this.scheduledTimerTask = timerService.scheduleWithFixedDelay(timerTask, delay, delay);
                LOG.info("Initialized new timer task for rescheduling checks");
            }

            // Plan rescheduling
            if (remotePlan) {
                rescheduleQueue.offerOrReplaceAndReset(ReschedulePlan.getInstance(true));
                LOG.info("Planned rescheduling including remote plan");
            } else {
                boolean added = rescheduleQueue.offerIfAbsentElseReset(ReschedulePlan.getInstance(false));
                if (added) {
                    LOG.info("Planned rescheduling with local-only plan");
                }
            }
        }
    }

    /**
     * Checks for an available rescheduling.
     */
    protected void checkReschedule() {
        synchronized (this) {
            // Stopped
            if (stopped) {
                return;
            }

            ReschedulePlan plan = rescheduleQueue.poll();
            if (null != plan) {
                boolean remotePlan = plan.isRemotePlan();
                doReschedule(remotePlan);
                LOG.info("Triggered rescheduling of permanent listeners {}", remotePlan ? "incl. remote rescheduling" : "local-only");
            }

            if (rescheduleQueue.isEmpty()) {
                // No more planned rescheduling operations
                cancelTimerTask();
            }
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    /**
     * Reschedules permanent listeners among available cluster members.
     *
     * @param remotePlan Whether to plan rescheduling on remote members or not
     */
    private void doReschedule(boolean remotePlan) {
        HazelcastInstance hzInstance = hzInstancerRef.get();
        if (null != hzInstance) {
            Cluster cluster = hzInstance.getCluster();
            reschedule(cluster.getMembers(), hzInstance, remotePlan);
        }
    }

    /**
     * Reschedules permanent listeners among available cluster members.
     *
     * @param allMembers All available cluster members
     * @param hzInstance The associated Hazelcast instance
     * @param remotePlan Whether to plan rescheduling on remote members or not
     */
    private void reschedule(Set<Member> allMembers, HazelcastInstance hzInstance, boolean remotePlan) {
        if (null == hzInstance) {
            LOG.warn("Aborted rescheduling of permanent listeners as passed HazelcastInstance is null.");
            return;
        }

        // Determine push users to distribute among cluster members
        List<PushUser> allPushUsers;
        try {
            allPushUsers = pushManagerRegistry.getUsersWithPermanentListeners();
            if (allPushUsers.isEmpty()) {
                return;
            }
        } catch (Exception e) {
            LOG.warn("Failed to distribute permanent listeners among cluster nodes", e);
            return;
        }

        // Acquire optional thread pool
        ThreadPoolService threadPool = Services.optService(ThreadPoolService.class);
        if (null == threadPool) {
            // Perform with current thread
            ThreadPools.execute(new ReschedulerTask(allMembers, allPushUsers, hzInstance, this, pushManagerRegistry, remotePlan));
        } else {
            // Submit task to thread pool
            threadPool.submit(new ReschedulerTask(allMembers, allPushUsers, hzInstance, this, pushManagerRegistry, remotePlan));
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    @Override
    public HazelcastInstance addingService(ServiceReference<HazelcastInstance> reference) {
        HazelcastInstance hzInstance = context.getService(reference);
        try {
            Cluster cluster = hzInstance.getCluster();
            String registrationId = cluster.addMembershipListener(this);
            registrationIdRef.set(registrationId);

            hzInstancerRef.set(hzInstance);

            planReschedule(false);

            return hzInstance;
        } catch (Exception e) {
            LOG.warn("Failed to distribute permanent listeners among cluster nodes", e);
        }
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance hzInstance) {
        // Nothing
    }

    @Override
    public void removedService(ServiceReference<HazelcastInstance> reference, HazelcastInstance hzInstance) {
        String registrationId = registrationIdRef.get();
        if (null != registrationId) {
            hzInstance.getCluster().removeMembershipListener(registrationId);
            registrationIdRef.set(null);
        }

        hzInstancerRef.set(null);
        context.ungetService(reference);
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    private static class ReschedulerTask extends AbstractTask<Void> {

        private final Set<Member> allMembers;
        private final List<PushUser> allPushUsers;
        private final HazelcastInstance hzInstance;
        private final Object monitor;
        private final PushManagerRegistry pushManagerRegistry;
        private final boolean remotePlan;

        /**
         * Initializes a new {@link ReschedulerTask}.
         */
        ReschedulerTask(Set<Member> allMembers, List<PushUser> allPushUsers, HazelcastInstance hzInstance, Object monitor, PushManagerRegistry pushManagerRegistry, boolean remotePlan) {
            super();
            this.allMembers = allMembers;
            this.allPushUsers = allPushUsers;
            this.hzInstance = hzInstance;
            this.monitor = monitor;
            this.pushManagerRegistry = pushManagerRegistry;
            this.remotePlan = remotePlan;
        }

        @Override
        public Void call() {
            synchronized (monitor) {
                try {
                    LOG.info("Rescheduling for the following push users: {}", allPushUsers);

                    // Get local member
                    Member localMember = hzInstance.getCluster().getLocalMember();

                    // Determine other cluster members
                    Set<Member> otherMembers = getOtherMembers(allMembers, localMember);

                    if (otherMembers.isEmpty()) {
                        // No other cluster members - assign all available permanent listeners to this node
                        pushManagerRegistry.applyInitialListeners(allPushUsers);
                        LOG.info("Applied all push user to local member (no other members available): {}", localMember);
                        return null;
                    }

                    // Identify those members having at least one "PushManagerExtendedService" instance
                    List<Member> candidates = new LinkedList<Member>();
                    candidates.add(localMember);
                    boolean memberAdded = false;
                    {
                        IExecutorService executor = hzInstance.getExecutorService("default");
                        Map<Member, Future<Boolean>> futureMap = executor.submitToMembers(new PortableCheckForExtendedServiceCallable(localMember.getUuid()), otherMembers);
                        for (Map.Entry<Member, Future<Boolean>> entry : futureMap.entrySet()) {
                            Member member = entry.getKey();
                            Future<Boolean> future = entry.getValue();
                            // Check Future's return value
                            int retryCount = 3;
                            while (retryCount-- > 0) {
                                try {
                                    boolean isCapable = future.get().booleanValue();
                                    retryCount = 0;
                                    if (isCapable) {
                                        candidates.add(member);
                                        memberAdded = true;
                                        LOG.info("Allowed {} on cluster member \"{}\", hence considered for rescheduling computation.", PushManagerExtendedService.class.getSimpleName(), member);
                                    } else {
                                        LOG.info("Disallowed {} on cluster member \"{}\", hence ignored for rescheduling computation.", PushManagerExtendedService.class.getSimpleName(), member);
                                    }
                                } catch (InterruptedException e) {
                                    // Interrupted - Keep interrupted state
                                    Thread.currentThread().interrupt();
                                    LOG.warn("Interrupted while distributing permanent listeners among cluster nodes", e);
                                    return null;
                                } catch (CancellationException e) {
                                    // Canceled
                                    LOG.warn("Canceled while distributing permanent listeners among cluster nodes", e);
                                    return null;
                                } catch (ExecutionException e) {
                                    Throwable cause = e.getCause();

                                    // Check for Hazelcast timeout
                                    if (!(cause instanceof com.hazelcast.core.OperationTimeoutException)) {
                                        if (cause instanceof IOException) {
                                            throw ((IOException) cause);
                                        }
                                        if (cause instanceof RuntimeException) {
                                            throw ((RuntimeException) cause);
                                        }
                                        if (cause instanceof Error) {
                                            throw (Error) cause;
                                        }
                                        throw new IllegalStateException("Not unchecked", cause);
                                    }

                                    // Timeout while awaiting remote result
                                    if (retryCount > 0) {
                                        LOG.info("Timeout while awaiting remote result from cluster member \"{}\". Retry...", member);
                                    } else {
                                        // No further retry
                                        LOG.info("Giving up awaiting remote result from cluster member \"{}\", hence ignored for rescheduling computation.", member);
                                        cancelFutureSafe(future);
                                    }
                                }
                            }
                        }
                    }

                    // Check capable members
                    if (!memberAdded) {
                        // No other cluster members - assign all available permanent listeners to this node
                        pushManagerRegistry.applyInitialListeners(allPushUsers);
                        LOG.info("Applied all push user to local member (no other capable members): {}", localMember);
                        return null;
                    }

                    // First, sort by UUID
                    Collections.sort(candidates, new Comparator<Member>() {

                        @Override
                        public int compare(Member m1, Member m2) {
                            return toString(m1).compareTo(toString(m2));
                        }

                        private String toString(Member m) {
                            InetSocketAddress addr = m.getSocketAddress();
                            return new StringBuilder(24).append(addr.getHostString()).append(':').append(addr.getPort()).toString();
                        }
                    });

                    LOG.info("Going to distribute permanent listeners among cluster nodes: {}", candidates);

                    // Check if required to also plan a rescheduling at remote members
                    if (remotePlan) {
                        IExecutorService executor = hzInstance.getExecutorService("default");
                        executor.submitToMembers(new PortablePlanRescheduleCallable(localMember.getUuid()), otherMembers);
                    }

                    // Determine the position of this cluster node
                    int pos = 0;
                    while (!localMember.getUuid().equals(candidates.get(pos).getUuid())) {
                        pos = pos + 1;
                    }

                    // Determine the permanent listeners for this node
                    List<PushUser> ps = new LinkedList<PushUser>();
                    int numMembers = candidates.size();
                    int numPushUsers = allPushUsers.size();
                    for (int i = 0; i < numPushUsers; i++) {
                        if ((i % numMembers) == pos) {
                            ps.add(allPushUsers.get(i));
                        }
                    }

                    // Apply newly calculated initial permanent listeners
                    pushManagerRegistry.applyInitialListeners(ps);

                    LOG.info("{} now runs permanent listeners for: {}", localMember, ps);

                    if (!remotePlan) {
                        // For safety reason, request explicit drop on other nodes for push users started on this node
                        new DropPushUserTask(ps, otherMembers, hzInstance, monitor).run();
                    }
                } catch (Exception e) {
                    LOG.warn("Failed to distribute permanent listeners among cluster nodes", e);
                }
            } // End of synchronized block
            return null;
        }
    } // End of class ReschedulerTask

    static Set<Member> getOtherMembers(Set<Member> allMembers, Member localMember) {
        Set<Member> otherMembers = new LinkedHashSet<Member>(allMembers);
        if (!otherMembers.remove(localMember)) {
            LOG.warn("Couldn't remove local member from cluster members.");
        }
        return otherMembers;
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    private static class DropPushUserTask implements Runnable {

        private final HazelcastInstance hzInstance;
        private final Object monitor;
        private final Set<Member> otherMembers;
        private final List<PushUser> pushUsers;

        DropPushUserTask(List<PushUser> pushUsers, Set<Member> otherMembers, HazelcastInstance hzInstance, Object monitor) {
            super();
            this.pushUsers = pushUsers;
            this.otherMembers = otherMembers;
            this.hzInstance = hzInstance;
            this.monitor = monitor;
        }

        @Override
        public void run() {
            synchronized (monitor) {
                try {
                    IExecutorService executor = hzInstance.getExecutorService("default");
                    Map<Member, Future<Boolean>> futureMap = executor.submitToMembers(new PortableDropPermanentListenerCallable(pushUsers), otherMembers);
                    for (Map.Entry<Member, Future<Boolean>> entry : futureMap.entrySet()) {
                        Member member = entry.getKey();
                        Future<Boolean> future = entry.getValue();
                        // Check Future's return value
                        int retryCount = 3;
                        while (retryCount-- > 0) {
                            try {
                                boolean dropped = future.get().booleanValue();
                                retryCount = 0;
                                if (dropped) {
                                    LOG.info("Successfully requested to drop locally running push users on cluster member \"{}\".", member);
                                } else {
                                    LOG.info("Failed to drop locally running push users on cluster member \"{}\".", member);
                                }
                            } catch (InterruptedException e) {
                                // Interrupted - Keep interrupted state
                                Thread.currentThread().interrupt();
                                LOG.warn("Interrupted while dropping locally running push users on cluster nodes", e);
                                return;
                            } catch (CancellationException e) {
                                // Canceled
                                LOG.warn("Canceled while dropping locally running push users on cluster nodes", e);
                                return;
                            } catch (ExecutionException e) {
                                Throwable cause = e.getCause();

                                // Check for Hazelcast timeout
                                if (!(cause instanceof com.hazelcast.core.OperationTimeoutException)) {
                                    if (cause instanceof IOException) {
                                        throw ((IOException) cause);
                                    }
                                    if (cause instanceof RuntimeException) {
                                        throw ((RuntimeException) cause);
                                    }
                                    if (cause instanceof Error) {
                                        throw (Error) cause;
                                    }
                                    throw new IllegalStateException("Not unchecked", cause);
                                }

                                // Timeout while awaiting remote result
                                if (retryCount > 0) {
                                    LOG.info("Timeout while awaiting remote result from cluster member \"{}\". Retry...", member);
                                } else {
                                    // No further retry
                                    LOG.info("Giving up awaiting remote result from cluster member \"{}\".", member);
                                    cancelFutureSafe(future);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.warn("Failed to stop permanent listener for locally running push users on other cluster nodes", e);
                }
            }
        } // End of run() method

    } // End of class DropPushUserTask

    static void cancelFutureSafe(Future<Boolean> future) {
        if (null != future) {
            try { future.cancel(true); } catch (Exception e) {/*Ignore*/}
        }
    }

}
